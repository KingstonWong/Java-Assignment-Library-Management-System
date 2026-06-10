using Library_Management_API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Library_Management_API.Controllers
{
    [ApiController]
    [Route("api/fines")]
    public class FineController : Controller
    {
        private readonly AppDbContext _context;

        private const int ALLOWED_DAYS = 1;
        private const decimal DAILY_FINE = 1.0m;

        public FineController(AppDbContext context)
        {
            _context = context;
        }

        // ===============================
        // CREATE - Calculate Fine by Borrow ID
        // ===============================
        [HttpPost("calculate/{borrowId}")]
        public async Task<IActionResult> CalculateFine(int borrowId)
        {
            var borrow = await _context.Borrow_RecordTable.FindAsync(borrowId);
            if (borrow == null)
                return NotFound("Borrow record not found");

            // Use return date if exists, otherwise today
            DateOnly endDate = borrow.return_date ?? DateOnly.FromDateTime(DateTime.Now);

            // Calculate overdue days from DUE DATE
            int overdueDays = endDate.DayNumber - borrow.due_date.DayNumber;

            if (overdueDays <= 0)
                return Ok("No fine. Book not overdue.");

            var fine = await _context.FineTable
                .FirstOrDefaultAsync(f => f.borrow_id == borrowId);

            if (fine == null)
            {
                fine = new Fine
                {
                    borrow_id = borrowId,
                    overdue_days = overdueDays,
                    fine_amount = overdueDays * DAILY_FINE,
                    is_paid = false
                };
                _context.FineTable.Add(fine);
            }
            else
            {
                fine.overdue_days = overdueDays;
                fine.fine_amount = overdueDays * DAILY_FINE;
            }

            await _context.SaveChangesAsync();
            return Ok(fine);
        }

        // ===============================
        // READ - Get All Fines
        // ===============================
        [HttpGet]
        public async Task<ActionResult<IEnumerable<object>>> GetAll()
        {
            var fines = await _context.FineTable
                .Include(f => f.Borrow_Record)
                .ThenInclude(b => b.Member)
                .Select(f => new
                {
                    f.fine_id,
                    f.borrow_id,
                    MemberName = f.Borrow_Record.Member != null ? f.Borrow_Record.Member.member_name : "-",
                    Amount = f.fine_amount,
                    Status = f.is_paid ? "Paid" : "Pending",
                    DueDate = f.Borrow_Record.due_date.ToString("yyyy-MM-dd"),
                    ReturnDate = f.Borrow_Record.return_date.HasValue ? f.Borrow_Record.return_date.Value.ToString("yyyy-MM-dd") : "-",
                    OverdueDays = f.overdue_days    // add this line
                })
                .ToListAsync();

            return Ok(fines);
        }



        // ===============================
        // READ - Get Fine by ID
        // ===============================
        [HttpGet("{id}")]
        public async Task<ActionResult<Fine>> GetById(int id)
        {
            var fine = await _context.FineTable.FindAsync(id);
            if (fine == null)
                return NotFound("Fine not found");

            return fine;
        }

        // ===============================
        // UPDATE - Pay Fine
        // ===============================
        [HttpPut("pay/{id}")]
        public async Task<ActionResult<Fine>> PayFine(int id)
        {
            var fine = await _context.FineTable.FindAsync(id);
            if (fine == null)
                return NotFound("Fine not found");

            if (fine.is_paid) // check if already paid
                return Ok(fine); // return existing fine object

            fine.is_paid = true;
            await _context.SaveChangesAsync();

            return Ok(fine); // return updated fine
        }

        // ===============================
        // DELETE - Remove Fine
        // ===============================
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var fine = await _context.FineTable.FindAsync(id);
            if (fine == null)
                return NotFound("Fine not found");

            _context.FineTable.Remove(fine);
            await _context.SaveChangesAsync();

            return Ok("Fine deleted");
        }
    }
}

