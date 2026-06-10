using Library_Management_API.DTO;
using Library_Management_API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Library_Management_API.Helper; // for SimpleSession

namespace Library_Management_API.Controllers
{
    [ApiController]
    [Route("api/reviews")]
    public class BookReviewController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BookReviewController(AppDbContext context)
        {
            _context = context;
        }

        // =====================================================
        // ADD REVIEW
        // POST: api/reviews
        // =====================================================
        [HttpPost]
        public async Task<IActionResult> AddReview([FromBody] ReviewDTO dto)
        {
            // ✅ Get token from Authorization header manually
            string authHeader = Request.Headers["Authorization"];
            if (string.IsNullOrEmpty(authHeader) || !authHeader.StartsWith("Bearer "))
                return BadRequest("❌ Missing token");

            string token = authHeader.Substring("Bearer ".Length).Trim();
            int? memberId = SimpleSession.GetEmployeeId(token);

            if (memberId == null)
                return BadRequest("❌ Invalid token");

            // 1️⃣ Check member has borrowed & returned this book
            bool hasBorrowed = await _context.Borrow_RecordTable
                .AnyAsync(b =>
                    b.book_id == dto.book_id &&
                    b.member_id == memberId.Value &&
                    b.return_date != null);

            if (!hasBorrowed)
                return BadRequest("❌ You can only review books you have borrowed.");

            // 2️⃣ Prevent duplicate review
            bool alreadyReviewed = await _context.BookReviewTable
                .AnyAsync(r =>
                    r.book_id == dto.book_id &&
                    r.member_id == memberId.Value);

            if (alreadyReviewed)
                return BadRequest("❌ You have already reviewed this book.");

            // 3️⃣ Save review
            BookReview review = new BookReview
            {
                book_id = dto.book_id,
                member_id = memberId.Value,
                rating = dto.rating,
                comment = dto.comment,
                created_at = DateOnly.FromDateTime(DateTime.Now)
            };

            _context.BookReviewTable.Add(review);
            await _context.SaveChangesAsync();

            return Ok(new { message = "✅ Review added successfully" });
        }

        // =====================================================
        // GET REVIEW TIMELINE BY BOOK
        // GET: api/reviews/book/{bookId}
        // =====================================================
        [HttpGet("book/{bookId}")]
        public async Task<IActionResult> GetReviewsByBook(int bookId)
        {
            var reviews = await _context.BookReviewTable
                .Where(r => r.book_id == bookId)
                .Include(r => r.Member)
                .OrderByDescending(r => r.created_at)
                .Select(r => new
                {
                    r.review_id,
                    memberName = r.Member.member_name,
                    r.rating,
                    r.comment,
                    created_at = r.created_at.ToString("yyyy-MM-dd")
                })
                .ToListAsync();

            return Ok(reviews);
        }
    }
}
