using System.ComponentModel.DataAnnotations;

namespace Library_Management_API.Models
{
    public class Member
    {
        [Key]
        public int member_id { get; set; } // same as studentID
        public string membership_id { get; set; } // Auto generate GUID 
        public string member_name { get; set; } // studentName
        public string phone_number { get; set; }
        public string email { get; set; }

        public string password_hash { get; set; }  // store hashed password

        // Navigation collections 
        public ICollection<Borrow_Record>? Borrow_Records { get; set; } = new List<Borrow_Record>();
    }
}
