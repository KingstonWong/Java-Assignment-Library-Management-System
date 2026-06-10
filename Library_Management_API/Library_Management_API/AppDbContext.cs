using Library_Management_API.Models;
using Microsoft.EntityFrameworkCore;

namespace Library_Management_API
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions option) : base(option)
        {

        }

        public DbSet<Book> BookTable { get; set; }
        public DbSet<Member> MemberTable { get; set; }
        public DbSet<Borrow_Record> Borrow_RecordTable { get; set; }
        public DbSet<Fine> FineTable { get; set; }
        public DbSet<BookReview> BookReviewTable { get; set; }
    }
}
