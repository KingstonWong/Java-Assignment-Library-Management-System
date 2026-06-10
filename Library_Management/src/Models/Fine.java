package Models;

import java.math.BigDecimal;

public class Fine {
    private int fine_id;
    private int borrow_id;
    private int overdue_days;
    private BigDecimal fine_amount;
    private boolean is_paid;

    // Constructors
    public Fine() {}

    public Fine(int borrow_id, int overdue_days, BigDecimal fine_amount, boolean is_paid) {
        this.borrow_id = borrow_id;
        this.overdue_days = overdue_days;
        this.fine_amount = fine_amount;
        this.is_paid = is_paid;
    }

    // Getters & Setters
    public int getFine_id() { return fine_id; }
    public void setFine_id(int fine_id) { this.fine_id = fine_id; }

    public int getBorrow_id() { return borrow_id; }
    public void setBorrow_id(int borrow_id) { this.borrow_id = borrow_id; }

    public int getOverdue_days() { return overdue_days; }
    public void setOverdue_days(int overdue_days) { this.overdue_days = overdue_days; }

    public BigDecimal getFine_amount() { return fine_amount; }
    public void setFine_amount(BigDecimal fine_amount) { this.fine_amount = fine_amount; }

    public boolean isIs_paid() { return is_paid; }
    public void setIs_paid(boolean is_paid) { this.is_paid = is_paid; }
}
