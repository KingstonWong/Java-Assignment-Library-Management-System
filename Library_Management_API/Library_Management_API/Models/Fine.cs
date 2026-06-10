using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Library_Management_API.Models
{
    public class Fine
    {
        [Key]
        public int fine_id { get; set; }

        public int borrow_id { get; set; }
        [ForeignKey(nameof(borrow_id))]
        public Borrow_Record Borrow_Record { get; set; }

        public int overdue_days { get; set; }
        public decimal fine_amount { get; set; }
        public bool is_paid { get; set; } // true = paid
    }
}
