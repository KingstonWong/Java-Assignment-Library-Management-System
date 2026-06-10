using Microsoft.Identity.Client;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Library_Management_API.Models
{
    public class BookReview
    {
        [Key]
        public int review_id { get; set; }

        public int book_id { get; set; }
        [ForeignKey(nameof(book_id))]
        public Book Book { get; set; }

        public int member_id { get; set; }
        [ForeignKey(nameof(member_id))]
        public Member Member { get; set; }

        public int rating { get; set; }
        public string comment { get; set; }
        public DateOnly created_at { get; set; }
    }
}
