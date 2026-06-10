using Microsoft.EntityFrameworkCore.Metadata.Conventions;

namespace Library_Management_API.DTO
{
    public class BookResponseDTO
    {
        public int book_id { get; set; }
        public string isbn { get; set; }
        public string title { get; set; }
        public string description { get; set; }
        public string author { get; set; }
        public string publisher { get; set; }
        public int publication_year { get; set; }
        public string category { get; set; }
        public string location { get; set; }
        public int total_copies { get; set; }
        public int available_copies { get; set; }
        public int borrowed_count { get; set; }
    }
}
