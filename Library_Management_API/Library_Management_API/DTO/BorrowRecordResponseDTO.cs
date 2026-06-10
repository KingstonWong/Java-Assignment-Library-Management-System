namespace Library_Management_API.DTO
{
    // Used when returning borrow record details to frontend/admin/member
    public class BorrowRecordResponseDTO
    {
        public int borrow_id { get; set; }

        public int member_id { get; set; }
        public string member_name { get; set; }

        public int book_id { get; set; }
        public string book_title { get; set; }
        public string? location { get; set; }

        // Dates
        public DateOnly request_date { get; set; }         // When borrow requested (or admin added)
        public DateOnly? approval_date { get; set; }       // Admin approves
        public DateOnly? pickup_date { get; set; }         // When member picked up the book
        public DateOnly due_date { get; set; }             // Due date for return
        public DateOnly? return_date { get; set; }         // When returned

        // Borrow status
        public string status { get; set; }                 // PENDING, BORROWED, RETURNED, REJECTED

        // Admin walk-in flag
        public bool isWalkIn { get; set; }
    }
}
