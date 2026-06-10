using Library_Management_API.DTO;
using Library_Management_API.Helper;
using Library_Management_API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Library_Management_API.Controllers
{
    [ApiController]
    [Route("api/members")]
    public class MemberController : Controller
    {
        private readonly AppDbContext _context;

        public MemberController(AppDbContext context)
        {
            _context = context;
        }

        // ===============================
        // CREATE - Add New Member
        // ===============================
        [HttpPost]
        public async Task<IActionResult> Create(MemberCreateDTO dto)
        {
            var member = new Member
            {
                membership_id = Guid.NewGuid().ToString(),
                member_name = dto.member_name,
                phone_number = dto.phone_number,
                email = dto.email,
                password_hash = PasswordHelper.Hash(dto.password)
            };

            _context.MemberTable.Add(member);
            await _context.SaveChangesAsync();

            return Ok("Member created successfully");
        }

        // ===============================
        // READ - Get All Members
        // ===============================
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Member>>> GetAll()
        {
            return await _context.MemberTable.ToListAsync();
        }

        // ===============================
        // READ - Get Member by ID
        // ===============================
        [HttpGet("admin/{id}")]
        public async Task<ActionResult<Member>> GetById(int id)
        {
            var member = await _context.MemberTable.FindAsync(id);

            if (member == null)
                return NotFound("Member not found");

            return member;
        }

        // ===============================
        // UPDATE - Update Member Info
        // ===============================
        [HttpPut("admin/{id}")]
        public async Task<IActionResult> Update(int id, MemberUpdateAdminDTO dto)
        {
            var member = await _context.MemberTable.FindAsync(id);
            if (member == null)
                return NotFound("Member not found");

            member.member_name = dto.member_name;
            member.phone_number = dto.phone_number;
            member.email = dto.email;

            await _context.SaveChangesAsync();
            return Ok("Member updated successfully");
        }

        // ===============================
        // DELETE - Delete Member
        // ===============================
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var member = await _context.MemberTable.FindAsync(id);
            if (member == null)
                return NotFound("Member not found");

            // Prevent delete if member has unreturned books
            bool hasActiveBorrow = await _context.Borrow_RecordTable
                .AnyAsync(br => br.member_id == id && br.return_date == null);

            if (hasActiveBorrow)
                return BadRequest("Cannot delete member with unreturned books");

            _context.MemberTable.Remove(member);
            await _context.SaveChangesAsync();

            return Ok("Member deleted successfully");
        }

        // ================= REGISTER =================
        [HttpPost("register")]
        public async Task<IActionResult> Register(SignUpDTO dto)
        {
            if (await _context.MemberTable.AnyAsync(m => m.email == dto.email))
                return BadRequest("Email already exists");

            var member = new Member
            {
                member_name = dto.member_name,
                email = dto.email,
                phone_number = dto.phone_number,
                password_hash = PasswordHelper.Hash(dto.password),
                membership_id = Guid.NewGuid().ToString() // ✅ auto-generate GUID
            };

            _context.MemberTable.Add(member);
            await _context.SaveChangesAsync();

            // Return token after registration as well
            string token = SimpleSession.AddSession(member.member_id);

            return Ok(new
            {
                member_id = member.member_id,
                membership_id = member.membership_id,
                member_name = member.member_name,
                email = member.email,
                phone_number = member.phone_number,
                token = token
            });
        }

        // ================= LOGIN =================
        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequestDTO dto)
        {
            var member = await _context.MemberTable
                .FirstOrDefaultAsync(m => m.email == dto.email);

            if (member == null || !PasswordHelper.Verify(dto.password, member.password_hash))
                return Unauthorized("Invalid email or password");

            // Generate token
            string token = SimpleSession.AddSession(member.member_id);

            return Ok(new
            {
                member_id = member.member_id,
                membership_id = member.membership_id,
                member_name = member.member_name,
                email = member.email,
                phone_number = member.phone_number,
                token = token   // <-- Return the token
            });
        }

        // ================= VIEW PROFILE =================
        [HttpGet("profile/{id}")]
        public async Task<IActionResult> GetProfile(int id)
        {
            var member = await _context.MemberTable.FindAsync(id);
            if (member == null) return NotFound();

            return Ok(new
            {
                member.member_id,
                member.membership_id,
                member.member_name,
                member.email,
                member.phone_number
            });
        }

        //// ================= UPDATE PROFILE =================
        [HttpPut("profile/{id:int}")]
        public async Task<IActionResult> UpdateProfile(int id, [FromBody] MemberUpdateDTO dto)
        {
            if (dto == null)
                return BadRequest("Invalid request body");

            var member = await _context.MemberTable.FindAsync(id);
            if (member == null) return NotFound();

            if (!string.IsNullOrEmpty(dto.member_name))
                member.member_name = dto.member_name;

            if (!string.IsNullOrEmpty(dto.phone_number))
                member.phone_number = dto.phone_number;

            if (!string.IsNullOrEmpty(dto.email))
                member.email = dto.email;

            await _context.SaveChangesAsync();
            return Ok("Profile updated successfully");
        }

        //// ================= CHANGE PASSWORD =================
        [HttpPut("change-password/{id:int}")]
        public async Task<IActionResult> ChangePassword(int id, [FromBody] ChangePasswordDTO dto)
        {
            var member = await _context.MemberTable.FindAsync(id);
            if (member == null) return NotFound();

            if (!PasswordHelper.Verify(dto.OldPassword, member.password_hash))
                return BadRequest("Old password is incorrect");

            member.password_hash = PasswordHelper.Hash(dto.NewPassword);
            await _context.SaveChangesAsync();
            return Ok("Password changed successfully");
        }

        // ================= FORGOT PASSWORD =================
        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordDTO dto)
        {
            if (dto == null || string.IsNullOrWhiteSpace(dto.Email))
                return BadRequest(new { message = "Email cannot be empty" });

            var member = await _context.MemberTable
                .FirstOrDefaultAsync(m => m.email == dto.Email.Trim());

            if (member == null)
                return NotFound(new { message = "This email is not registered" });

            string tempPassword = GenerateTempPassword();

            member.password_hash = PasswordHelper.Hash(tempPassword);
            await _context.SaveChangesAsync();

            // ✅ Do NOT send email here
            return Ok(new { tempPassword });
        }

        // Helper method to generate a temporary password
        private string GenerateTempPassword(int length = 8)
        {
            const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
            var random = new Random();
            return new string(Enumerable.Repeat(chars, length)
                .Select(s => s[random.Next(s.Length)]).ToArray());
        }

    }
}

