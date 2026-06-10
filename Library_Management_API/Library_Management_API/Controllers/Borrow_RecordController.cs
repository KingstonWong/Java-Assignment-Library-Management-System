using Library_Management_API.DTO;
using Library_Management_API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Library_Management_API.Controllers
{
    [ApiController]
    [Route("api/borrow-records")]
    public class Borrow_RecordController : ControllerBase
    {
        private readonly AppDbContext _context;

        public Borrow_RecordController(AppDbContext context)
        {
            _context = context;
        }

        // ===============================
        // ADMIN BORROW (Walk-in)
        // ===============================
        [HttpPost("admin/borrow")]
        public async Task<IActionResult> AdminBorrowBook(BorrowCreateDTO dto)
        {
            var book = await _context.BookTable.FindAsync(dto.book_id);
            var member = await _context.MemberTable.FindAsync(dto.member_id);

            if (book == null || member == null)
                return BadRequest("Invalid Book or Member");

            if (book.available_copies <= 0)
                return BadRequest("Book not available");

            var now = DateOnly.FromDateTime(DateTime.Now);

            var borrow = new Borrow_Record
            {
                Book = book,
                Member = member,
                request_date = now,
                pickup_date = now,
                approval_date = now,
                due_date = now.AddDays(1),   // 2 weeks borrow period
                status = "BORROWED",
                isWalkIn = true
            };

            book.available_copies--;

            _context.Borrow_RecordTable.Add(borrow);
            await _context.SaveChangesAsync();

            return Ok("Book borrowed successfully (walk-in).");
        }

        // ===============================
        // MEMBER REQUEST BORROW
        // ===============================
        [HttpPost("member/request/{memberId}/{bookId}")]
        public async Task<IActionResult> MemberRequestBorrow(int memberId, int bookId)
        {
            var book = await _context.BookTable.FindAsync(bookId);
            var member = await _context.MemberTable.FindAsync(memberId);

            if (book == null || member == null)
                return BadRequest("Invalid Book or Member");

            var now = DateOnly.FromDateTime(DateTime.Now);

            var borrow = new Borrow_Record
            {
                Book = book,
                Member = member,
                request_date = now,
                status = "PENDING",
                isWalkIn = false
            };

            _context.Borrow_RecordTable.Add(borrow);
            await _context.SaveChangesAsync();

            return Ok("✅ Borrow request submitted. Waiting for admin approval.");
        }

        // ===============================
        // ADMIN APPROVE REQUEST
        // ===============================
        [HttpPut("admin/approve/{borrowId}")]
        public async Task<IActionResult> ApproveRequest(int borrowId)
        {
            var record = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Include(b => b.Member)
                .FirstOrDefaultAsync(b => b.borrow_id == borrowId);

            if (record == null)
                return NotFound("Borrow record not found");

            if (record.status != "PENDING")
                return BadRequest("Borrow request is not pending");

            if (record.Book.available_copies <= 0)
                return BadRequest("Book not available");

            record.status = "APPROVED";
            record.approval_date = DateOnly.FromDateTime(DateTime.Now);

            await _context.SaveChangesAsync();

            return Ok("Borrow request approved. Member notified to pick up the book.");
        }

        // ===============================
        // GET APPROVED BORROWS (ADMIN)
        // ===============================
        [HttpGet("approved")]
        public async Task<ActionResult<IEnumerable<BorrowRecordResponseDTO>>> GetApproved()
        {
            var approved = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Include(b => b.Member)
                .Where(b => b.status == "APPROVED")
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    member_id = b.member_id,
                    member_name = b.Member.member_name,
                    request_date = b.request_date,
                    approval_date = b.approval_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(approved);
        }

        // ===============================
        // ADMIN PICKUP REQUEST
        // ===============================
        [HttpPut("admin/pickup/{borrowId}")]
        public async Task<IActionResult> ConfirmPickup(int borrowId)
        {
            var record = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .FirstOrDefaultAsync(b => b.borrow_id == borrowId);

            if (record == null)
                return NotFound("Borrow record not found");

            if (record.status != "APPROVED")
                return BadRequest("Book is not approved for pickup");

            record.status = "BORROWED";
            record.pickup_date = DateOnly.FromDateTime(DateTime.Now);
            record.due_date = record.pickup_date.Value.AddDays(1); // 2 weeks borrow period

            record.Book.available_copies--;

            await _context.SaveChangesAsync();

            return Ok("Book picked up successfully.");
        }


        // ===============================
        // RETURN BOOK
        // ===============================
        [HttpPut("return/{borrowId}")]
        public async Task<IActionResult> ReturnBook(int borrowId)
        {
            var record = await _context.Borrow_RecordTable.Include(b => b.Book).FirstOrDefaultAsync(b => b.borrow_id == borrowId);

            if (record == null)
                return NotFound("Borrow record not found");

            if (record.status != "BORROWED")
                return BadRequest("Book is not currently borrowed");

            record.return_date = DateOnly.FromDateTime(DateTime.Now);
            record.status = "RETURNED";

            if (record.Book.available_copies < record.Book.total_copies)
                record.Book.available_copies++;

            await _context.SaveChangesAsync();

            return Ok("Book returned successfully.");
        }

        // ===============================
        // DELETE RECORD (ADMIN)
        // ===============================
        [HttpDelete("{borrowId}")]
        public async Task<IActionResult> Delete(int borrowId)
        {
            var record = await _context.Borrow_RecordTable.FindAsync(borrowId);
            if (record == null)
                return NotFound("Borrow record not found");

            if (record.status == "BORROWED")
            {
                var book = await _context.BookTable.FindAsync(record.book_id);
                if (book != null && book.available_copies < book.total_copies)
                    book.available_copies++;
            }

            _context.Borrow_RecordTable.Remove(record);
            await _context.SaveChangesAsync();

            return Ok("Borrow record deleted.");
        }

        // ===============================
        // GET ALL RECORDS (ADMIN)
        // ===============================
        [HttpGet]
        public async Task<ActionResult<IEnumerable<BorrowRecordResponseDTO>>> GetAll()
        {
            var records = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Include(b => b.Member)
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    member_id = b.member_id,
                    member_name = b.Member.member_name,
                    request_date = b.request_date,
                    approval_date = b.approval_date,
                    pickup_date = b.pickup_date,
                    due_date = b.due_date,
                    return_date = b.return_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(records);
        }

        // ===============================
        // VIEW PENDING BORROWS (ADMIN)
        // ===============================
        [HttpGet("pending")]
        public async Task<ActionResult<IEnumerable<BorrowRecordResponseDTO>>> GetPending()
        {
            var pending = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Include(b => b.Member)
                .Where(b => b.status == "PENDING")
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    location = b.Book.location,
                    member_id = b.member_id,
                    member_name = b.Member.member_name,
                    request_date = b.request_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(pending);
        }

        // ===============================
        // SEARCH PENDING BORROWS (ADMIN)
        // ===============================
        [HttpGet("pending/search")]
        public async Task<ActionResult<IEnumerable<BorrowRecordResponseDTO>>> SearchPending([FromQuery] string keyword)
        {
            var results = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Include(b => b.Member)
                .Where(b => b.status == "PENDING" &&
                            (b.Book.title.Contains(keyword) || b.Member.member_name.Contains(keyword)))
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    member_id = b.member_id,
                    member_name = b.Member.member_name,
                    request_date = b.request_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(results);
        }

        // ===============================
        // GET RECORDS BY MEMBER
        // ===============================
        [HttpGet("member/{memberId}")]
        public async Task<ActionResult<IEnumerable<BorrowRecordResponseDTO>>> GetRecordsByMember(int memberId)
        {
            var records = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Where(b => b.member_id == memberId)
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    request_date = b.request_date,
                    approval_date = b.approval_date,
                    pickup_date = b.pickup_date,
                    due_date = b.due_date,
                    return_date = b.return_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(records);
        }

        // ===============================
        // GET ACTIVE BORROWS (MEMBER)
        // ===============================
        [HttpGet("member/{memberId}/active")]
        public async Task<IActionResult> GetActiveBorrows(int memberId)
        {
            var records = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Where(b => b.member_id == memberId && b.status == "BORROWED")
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    request_date = b.request_date,    
                    approval_date = b.approval_date,  
                    pickup_date = b.pickup_date,
                    due_date = b.due_date,
                    return_date = b.return_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(records);
        }

        // ===============================
        // GET BORROW HISTORY (MEMBER)
        // ===============================
        [HttpGet("member/{memberId}/history")]
        public async Task<IActionResult> GetBorrowHistory(int memberId)
        {
            var records = await _context.Borrow_RecordTable
                .Include(b => b.Book)
                .Where(b => b.member_id == memberId)
                .Select(b => new BorrowRecordResponseDTO
                {
                    borrow_id = b.borrow_id,
                    book_id = b.book_id,
                    book_title = b.Book.title,
                    request_date = b.request_date,
                    approval_date = b.approval_date,
                    pickup_date = b.pickup_date,
                    due_date = b.due_date,
                    return_date = b.return_date,
                    status = b.status,
                    isWalkIn = b.isWalkIn
                }).ToListAsync();

            return Ok(records);
        }
    }
}
