using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Library_Management_API.Models
{
    public class Borrow_Record
    {
        [Key]
        public int borrow_id { get; set; }
        
        public int member_id { get; set; }
        [ForeignKey(nameof(member_id))]
        public Member Member { get; set; }
        
        public int book_id { get; set; }
        [ForeignKey(nameof(book_id))]
        public Book Book { get; set; }

        // ------------------- Dates -------------------
        public DateOnly request_date { get; set; }      // When member requested (or admin added)
        public DateOnly? approval_date { get; set; }    // Only used for member request
        public DateOnly? pickup_date { get; set; }      // Actual borrow date
        public DateOnly due_date { get; set; }          // Due date for return
        public DateOnly? return_date { get; set; }      // NULL if not returned

        // ------------------- Status -------------------
        public string status { get; set; } = "PENDING";
        // Values: PENDING, APPROVED, BORROWED, RETURNED

        // ------------------- Helper -------------------
        // Could also add a field to know who created the record
        public bool isWalkIn { get; set; } = false; // true if admin created directly
    }
}
