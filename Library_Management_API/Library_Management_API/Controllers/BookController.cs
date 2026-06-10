using Library_Management_API.DTO;
using Library_Management_API.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Library_Management_API.Controllers
{
    [ApiController]
    [Route("api/books")]
    public class BookController : ControllerBase
    {
        private readonly AppDbContext _context;
        public BookController(AppDbContext context) => _context = context;

        // ===============================
        // GET ALL BOOKS
        // ===============================
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var books = await _context.BookTable
                .Select(b => new BookResponseDTO
                {
                    book_id = b.book_id,
                    isbn = b.isbn,
                    title = b.title,
                    description = b.description,
                    author = b.author,
                    publisher = b.publisher,
                    publication_year = b.publication_year,
                    category = b.category,
                    location = b.location,
                    total_copies = b.total_copies,
                    available_copies = b.available_copies,
                    borrowed_count = b.total_copies - b.available_copies // calculate dynamically
                })
                .ToListAsync();

            return Ok(books);
        }

        // ===============================
        // GET BOOK BY ID
        // ===============================
        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var book = await _context.BookTable
                .Where(b => b.book_id == id)
                .Select(b => new BookResponseDTO
                {
                    book_id = b.book_id,
                    isbn = b.isbn,
                    title = b.title,
                    description = b.description,
                    author = b.author,
                    publisher = b.publisher,
                    publication_year = b.publication_year,
                    category = b.category,
                    location = b.location,
                    total_copies = b.total_copies,
                    available_copies = b.available_copies,
                    borrowed_count = b.total_copies - b.available_copies // dynamic
                })
                .FirstOrDefaultAsync();

            if (book == null) return NotFound();
            return Ok(book);
        }

        // =======================================
        // SEARCH BOOKS BY TITLE, ISBN, AUTHOR
        // =======================================

        // ================= SEARCH BOOKS =================
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] string keyword)
        {
            if (string.IsNullOrWhiteSpace(keyword))
                return BadRequest("Keyword cannot be empty");

            var books = await _context.BookTable
                .Where(b => b.title.Contains(keyword)
                         || b.isbn.Contains(keyword)
                         || b.author.Contains(keyword))
                .Select(b => new BookResponseDTO
                {
                    book_id = b.book_id,
                    isbn = b.isbn,
                    title = b.title,
                    description = b.description,
                    author = b.author,
                    publisher = b.publisher,
                    publication_year = b.publication_year,
                    category = b.category,
                    location = b.location,
                    total_copies = b.total_copies,
                    available_copies = b.available_copies
                })
                .ToListAsync();

            return Ok(books);
        }


        // ===============================
        // CREATE BOOK
        // ===============================
        [HttpPost]
        public async Task<IActionResult> Create(Book book)
        {
            if (await _context.BookTable.AnyAsync(b => b.isbn == book.isbn))
                return BadRequest("ISBN already exists");

            if (book.total_copies < 0)
                return BadRequest("Total copies cannot be negative");

            // Initialize available copies if not provided
            if (book.available_copies > book.total_copies)
                book.available_copies = book.total_copies;

            _context.BookTable.Add(book);
            await _context.SaveChangesAsync();

            var response = new BookResponseDTO
            {
                book_id = book.book_id,
                isbn = book.isbn,
                title = book.title,
                description = book.description,
                author = book.author,
                publisher = book.publisher,
                publication_year = book.publication_year,
                category = book.category,
                location = book.location,
                total_copies = book.total_copies,
                available_copies = book.available_copies,
                borrowed_count = book.total_copies - book.available_copies
            };

            return CreatedAtAction(nameof(GetById), new { id = book.book_id }, response);
        }

        // ===============================
        // UPDATE BOOK
        // ===============================
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, Book book)
        {
            var existing = await _context.BookTable
                .Include(b => b.Borrow_Records)
                .FirstOrDefaultAsync(b => b.book_id == id);

            if (existing == null) return NotFound();

            if (await _context.BookTable.AnyAsync(b => b.isbn == book.isbn && b.book_id != id))
                return BadRequest("ISBN already exists");

            int borrowedCount = existing.total_copies - existing.available_copies;

            if (book.total_copies < borrowedCount)
                return BadRequest($"Total copies cannot be less than borrowed count ({borrowedCount})");

            existing.isbn = book.isbn;
            existing.title = book.title;
            existing.description = book.description;
            existing.author = book.author;
            existing.publisher = book.publisher;
            existing.publication_year = book.publication_year;
            existing.category = book.category;
            existing.location = book.location;
            existing.total_copies = book.total_copies;
            existing.available_copies = Math.Max(book.total_copies - borrowedCount, 0);

            await _context.SaveChangesAsync();

            var response = new BookResponseDTO
            {
                book_id = existing.book_id,
                isbn = existing.isbn,
                title = existing.title,
                description = existing.description,
                author = existing.author,
                publisher = existing.publisher,
                publication_year = existing.publication_year,
                category = existing.category,
                location = existing.location,
                total_copies = existing.total_copies,
                available_copies = existing.available_copies,
                borrowed_count = borrowedCount
            };

            return Ok(response);
        }

        // ===============================
        // DELETE BOOK
        // ===============================
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var book = await _context.BookTable
                .Include(b => b.Borrow_Records)
                .FirstOrDefaultAsync(b => b.book_id == id);

            if (book == null) return NotFound();

            if (book.Borrow_Records.Any(br => br.return_date == null))
                return BadRequest("Cannot delete book that is currently borrowed");

            _context.BookTable.Remove(book);
            await _context.SaveChangesAsync();

            var response = new BookResponseDTO
            {
                book_id = book.book_id,
                isbn = book.isbn,
                title = book.title,
                description = book.description,
                author = book.author,
                publisher = book.publisher,
                publication_year = book.publication_year,
                category = book.category,
                location = book.location,
                total_copies = book.total_copies,
                available_copies = book.available_copies,
                borrowed_count = book.total_copies - book.available_copies
            };

            return Ok(response);
        }
    }
}
