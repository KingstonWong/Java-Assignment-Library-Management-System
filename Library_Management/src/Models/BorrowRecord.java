package Models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowRecord {
    private int borrow_id;
    private int member_id;
    private int book_id;
    private LocalDate request_date;
    private LocalDate approve_date;
    private LocalDate pickup_date;
    private LocalDate due_date;
    private LocalDate return_date;
    private boolean isWalkIn;

    // Default constructor
    public BorrowRecord() {}

    // Full constructor
    public BorrowRecord(int borrow_id, int member_id, int book_id,
                        LocalDate request_date, LocalDate approve_date, LocalDate pickup_date,
                        LocalDate due_date, LocalDate return_date, boolean isWalkIn) {
        this.borrow_id = borrow_id;
        this.member_id = member_id;
        this.book_id = book_id;
        this.request_date = request_date;
        this.approve_date = approve_date;
        this.pickup_date = pickup_date;
        this.due_date = due_date;
        this.return_date = return_date;
        this.isWalkIn = isWalkIn;
    }

    // ================= GETTERS & SETTERS =================
    public int getBorrow_id() { return borrow_id; }
    public void setBorrow_id(int borrow_id) { this.borrow_id = borrow_id; }

    public int getMember_id() { return member_id; }
    public void setMember_id(int member_id) { this.member_id = member_id; }

    public int getBook_id() { return book_id; }
    public void setBook_id(int book_id) { this.book_id = book_id; }

    public LocalDate getRequest_date() { return request_date; }
    public void setRequest_date(LocalDate request_date) { this.request_date = request_date; }

    public LocalDate getApprove_date() { return approve_date; }
    public void setApprove_date(LocalDate approve_date) { this.approve_date = approve_date; }

    public LocalDate getPickup_date() { return pickup_date; }
    public void setPickup_date(LocalDate pickup_date) { this.pickup_date = pickup_date; }

    public LocalDate getDue_date() { return due_date; }
    public void setDue_date(LocalDate due_date) { this.due_date = due_date; }

    public LocalDate getReturn_date() { return return_date; }
    public void setReturn_date(LocalDate return_date) { this.return_date = return_date; }

    public boolean isWalkIn() { return isWalkIn; }
    public void setWalkIn(boolean walkIn) { isWalkIn = walkIn; }

    // ================= HELPER METHODS =================

    /**
     * Calculate fine in days. Returns 0 if not overdue.
     */
    public long calculateFine() {
        LocalDate effectiveReturn = return_date != null ? return_date : LocalDate.now();
        long overdue = ChronoUnit.DAYS.between(due_date, effectiveReturn);
        return Math.max(overdue, 0);
    }

    /**
     * Returns computed status based on dates.
     */
    public String getComputedStatus() {
        if (return_date != null) return "Returned";
        if (pickup_date != null) return "Picked Up";
        if (approve_date != null) return "Approved";
        return "Pending";
    }

    /**
     * Checks if the borrow is overdue.
     */
    public boolean isOverdue() {
        LocalDate checkDate = return_date != null ? return_date : (pickup_date != null ? LocalDate.now() : null);
        return checkDate != null && checkDate.isAfter(due_date);
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "borrow_id=" + borrow_id +
                ", member_id=" + member_id +
                ", book_id=" + book_id +
                ", status=" + getComputedStatus() +
                ", due_date=" + due_date +
                ", return_date=" + return_date +
                ", isWalkIn=" + isWalkIn +
                '}';
    }
}
