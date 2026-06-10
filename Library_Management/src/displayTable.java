import Models.Book;
import Models.Member;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class displayTable {

    // ======================= MEMBER TABLE =======================
    public static void displayMembers(List<Member> members) {
        if (members == null || members.isEmpty()) {
            System.out.println("No members found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(5, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("Membership ID", center);
        table.addCell("Name", center);
        table.addCell("Phone", center);
        table.addCell("Email", center);

        for (Member m : members) {
            table.addCell(String.valueOf(m.getMember_id()), center);
            table.addCell(m.getMembership_id(), center);
            table.addCell(m.getMember_name(), center);
            table.addCell(m.getPhone_number(), center);
            table.addCell(m.getEmail(), center);
        }

        System.out.println(table.render());
    }

    // ======================= BOOK TABLE =======================
    public static void displayBooks(List<Book> books) {
        if (books == null || books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(12, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("ISBN", center);
        table.addCell("TITLE", center);
        table.addCell("DESCRIPTION", center);
        table.addCell("AUTHOR", center);
        table.addCell("PUBLISHER", center);
        table.addCell("YEAR", center);
        table.addCell("CATEGORY", center);
        table.addCell("LOCATION", center);
        table.addCell("TOTAL", center);
        table.addCell("AVAILABLE", center);
        table.addCell("BORROWED", center);

        for (Book b : books) {
            int borrowedCount = b.getTotal_copies() - b.getAvailable_copies();

            table.addCell(String.valueOf(b.getBook_id()), center);
            table.addCell(b.getIsbn(), center);
            table.addCell(b.getTitle(), center);
            table.addCell(b.getDescription(), center);
            table.addCell(b.getAuthor(), center);
            table.addCell(b.getPublisher(), center);
            table.addCell(String.valueOf(b.getPublication_year()), center);
            table.addCell(b.getCategory(), center);
            table.addCell(b.getLocation(), center);
            table.addCell(String.valueOf(b.getTotal_copies()), center);
            table.addCell(String.valueOf(b.getAvailable_copies()), center);
            table.addCell(String.valueOf(borrowedCount), center);
        }

        System.out.println(table.render());
    }

    // ======================= SINGLE BOOK DETAILS =======================
    public static void displayBookDetails(Book b) {
        if (b == null) {
            System.out.println("❌ Book not found.");
            return;
        }

        int borrowedCount = b.getTotal_copies() - b.getAvailable_copies();

        CellStyle left = new CellStyle(CellStyle.HorizontalAlign.left);
        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);

        Table table = new Table(2, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Field", center);
        table.addCell("Value", center);

        table.addCell("Book ID", left); table.addCell(String.valueOf(b.getBook_id()), left);
        table.addCell("ISBN", left); table.addCell(b.getIsbn(), left);
        table.addCell("Title", left); table.addCell(b.getTitle(), left);
        table.addCell("Description", left); table.addCell(b.getDescription(), left);
        table.addCell("Author", left); table.addCell(b.getAuthor(), left);
        table.addCell("Publisher", left); table.addCell(b.getPublisher(), left);
        table.addCell("Publication Year", left); table.addCell(String.valueOf(b.getPublication_year()), left);
        table.addCell("Category", left); table.addCell(b.getCategory(), left);
        table.addCell("Location", left); table.addCell(b.getLocation(), left);
        table.addCell("Total Copies", left); table.addCell(String.valueOf(b.getTotal_copies()), left);
        table.addCell("Available Copies", left); table.addCell(String.valueOf(b.getAvailable_copies()), left);
        table.addCell("Borrowed Count", left); table.addCell(String.valueOf(borrowedCount), left);

        System.out.println("\n📖 BOOK DETAILS");
        System.out.println(table.render());
    }

    public static void displayBookReviews(JSONArray reviews) throws JSONException {
        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);

        Table reviewTable = new Table(4, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        reviewTable.addCell("Member Name", center);
        reviewTable.addCell("Rating (1-5)", center);
        reviewTable.addCell("Comment", center);
        reviewTable.addCell("Date", center);

        for (int i = 0; i < reviews.length(); i++) {
            JSONObject r = reviews.getJSONObject(i);
            reviewTable.addCell(r.optString("memberName", "Anonymous"), center);
            reviewTable.addCell(r.optInt("rating", 0) + "/5", center);
            reviewTable.addCell(r.optString("comment", "-"), center);
            reviewTable.addCell(r.optString("created_at", "-"), center);
        }

        System.out.println("\n📚 BOOK REVIEWS");
        System.out.println(reviewTable.render());
    }



    // ======================= BORROW RECORD TABLE =======================
    public static void displayBorrowRecords(JSONArray records) {
        if (records == null || records.length() == 0) {
            System.out.println("No borrow records found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(10, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("Book Title", center);
        table.addCell("Member Name", center);
        table.addCell("Status", center);
        table.addCell("Walk-in", center);
        table.addCell("Borrow Date", center);
        table.addCell("Pickup Date", center);
        table.addCell("Due Date", center);
        table.addCell("Return Date", center);
        table.addCell("Fine (RM)", center);

        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject r = records.getJSONObject(i);

                String bookTitle = r.optString("book_title", "-");
                String memberName = r.optString("member_name", "-");
                String status = r.optString("status", "-");
                boolean isWalkIn = r.optBoolean("isWalkIn", false);
                String borrowDate = r.optString("request_date", "-");
                String pickupDate = r.optString("pickup_date", "-");
                String dueDate = r.optString("due_date", borrowDate.equals("-") ? "-" : LocalDate.parse(borrowDate).plusDays(14).toString());
                String returnDate = r.isNull("return_date")
                        ? "-"
                        : r.optString("return_date");


                long fine = calculateFine(dueDate, returnDate, status);

                table.addCell(String.valueOf(r.optInt("borrow_id", 0)), center);
                table.addCell(bookTitle, center);
                table.addCell(memberName, center);
                table.addCell(status, center);
                table.addCell(isWalkIn ? "Yes" : "No", center);
                table.addCell(borrowDate, center);
                table.addCell(pickupDate, center);
                table.addCell(dueDate, center);
                table.addCell(returnDate, center);
                table.addCell("RM" + fine, center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading borrow record: " + e.getMessage());
            }
        }

        System.out.println(table.render());
    }

    // ======================= FINE CALCULATION HELPER =======================
    private static long calculateFine(String dueDate, String returnDate, String status) {
        try {
            if (dueDate == null || dueDate.equals("-") || dueDate.equals("0001-01-01")) {
                return 0;
            }

            LocalDate due = LocalDate.parse(dueDate);

            // BORROWED & NOT YET RETURNED
            if ((returnDate == null || returnDate.equals("-") || returnDate.equalsIgnoreCase("null"))
                    && status.equalsIgnoreCase("BORROWED")) {

                return Math.max(ChronoUnit.DAYS.between(due, LocalDate.now()), 0);
            }

            // RETURNED
            if (returnDate != null && !returnDate.equals("-") && !returnDate.equalsIgnoreCase("null")) {
                return Math.max(ChronoUnit.DAYS.between(due, LocalDate.parse(returnDate)), 0);
            }

        } catch (Exception e) {
            // safe fallback
        }
        return 0;
    }

    // ================= DASHBOARD: MOST BORROWED BOOKS ===================
    public static void showMostBorrowedBooks(JSONArray records) {
        if (records == null || records.length() == 0) {
            System.out.println("No borrow records found.");
            return;
        }

        Map<String, Long> borrowCount = new java.util.HashMap<>();
        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject r = records.getJSONObject(i);
                String bookTitle = r.optString("book_title", "-");
                borrowCount.put(bookTitle, borrowCount.getOrDefault(bookTitle, 0L) + 1);
            } catch (JSONException e) {
                System.out.println("❌ Error reading record: " + e.getMessage());
            }
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(3, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Rank", center);
        table.addCell("Book Title", center);
        table.addCell("Borrows", center);

        int rank = 1;
        for (Map.Entry<String, Long> entry : borrowCount.entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .toList()) {
            table.addCell(String.valueOf(rank++), center);
            table.addCell(entry.getKey(), center);
            table.addCell(String.valueOf(entry.getValue()), center);
        }

        System.out.println("\n📊 TOP 5 MOST BORROWED BOOKS");
        System.out.println(table.render());
    }

    // ================= DASHBOARD: OVERDUE BOOKS ===================
    public static void showOverdueBooks(JSONArray records) {
        if (records == null || records.length() == 0) {
            System.out.println("No borrow records found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(8, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("Book Title", center);
        table.addCell("Member Name", center);
        table.addCell("Borrow Date", center);
        table.addCell("Pickup Date", center);
        table.addCell("Due Date", center);
        table.addCell("Status", center);
        table.addCell("Fine (RM)", center);

        boolean hasOverdue = false;

        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject r = records.getJSONObject(i);
                String status = r.optString("status", "-");
                if (!status.equals("BORROWED")) continue;

                String borrowDate = r.optString("request_date", "-");
                String pickupDate = r.optString("pickup_date", "-");
                String dueDate = r.optString("due_date", borrowDate.equals("-") ? "-" : LocalDate.parse(borrowDate).plusDays(14).toString());

                long overdue = calculateFine(dueDate, "-", status);
                if (overdue <= 0) continue;

                hasOverdue = true;
                table.addCell(String.valueOf(r.optInt("borrow_id", 0)), center);
                table.addCell(r.optString("book_title", "-"), center);
                table.addCell(r.optString("member_name", "-"), center);
                table.addCell(borrowDate, center);
                table.addCell(pickupDate, center);
                table.addCell(dueDate, center);
                table.addCell(status, center);
                table.addCell("RM" + overdue, center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading record: " + e.getMessage());
            }
        }

        System.out.println("\n⚠️ OVERDUE BORROW RECORDS (RM 1/day)");
        if (hasOverdue) System.out.println(table.render());
        else System.out.println("No overdue records.");
    }

    // ================= DASHBOARD: COMBINED ===================
    public static void showDashboard(JSONArray records) {
        showMostBorrowedBooks(records);
        showOverdueBooks(records);
    }

    // ======================= CURRENT BORROWED BOOKS =======================
    public static void displayCurrentBorrowedBooks(JSONArray records) {
        if (records == null || records.length() == 0) {
            System.out.println("No borrowed books found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(8, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("Book Title", center);
        table.addCell("Borrow Date", center);
        table.addCell("Pickup Date", center);
        table.addCell("Due Date", center);
        table.addCell("Status", center);
        table.addCell("Walk-in", center);
        table.addCell("Fine (RM)", center);

        boolean hasRecord = false;

        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject r = records.getJSONObject(i);
                String status = r.optString("status", "-");
                if (!status.equalsIgnoreCase("BORROWED")) continue;

                hasRecord = true;
                String borrowDate = r.optString("request_date", "-");
                String pickupDate = r.optString("pickup_date", "-");
                String dueDate = r.optString("due_date", borrowDate.equals("-") ? "-" : LocalDate.parse(borrowDate).plusDays(14).toString());
                boolean isWalkIn = r.optBoolean("isWalkIn", false);

                long fine = calculateFine(dueDate, "-", status);

                table.addCell(String.valueOf(r.optInt("borrow_id", 0)), center);
                table.addCell(r.optString("book_title", "-"), center);
                table.addCell(borrowDate, center);
                table.addCell(pickupDate, center);
                table.addCell(dueDate, center);
                table.addCell(status, center);
                table.addCell(isWalkIn ? "Yes" : "No", center);
                table.addCell("RM" + fine, center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading borrowed books: " + e.getMessage());
            }
        }

        System.out.println("\n📖 MY BORROWED BOOKS (NOT RETURNED)");
        if (hasRecord) System.out.println(table.render());
        else System.out.println("No active borrowed books.");
    }

    // ======================= BORROW HISTORY =======================
    public static void displayBorrowHistory(JSONArray records) {
        if (records == null || records.length() == 0) {
            System.out.println("No borrow history found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(9, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("ID", center);
        table.addCell("Book Title", center);
        table.addCell("Status", center);
        table.addCell("Walk-in", center);
        table.addCell("Borrow Date", center);
        table.addCell("Pickup Date", center);
        table.addCell("Due Date", center);
        table.addCell("Return Date", center);
        table.addCell("Fine (RM)", center);

        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject r = records.getJSONObject(i);

                String bookTitle = r.optString("book_title", "-");
                String status = r.optString("status", "-");
                boolean isWalkIn = r.optBoolean("isWalkIn", false);
                String borrowDate = r.optString("request_date", "-");
                String pickupDate = r.optString("pickup_date", "-");
                String dueDate = r.optString("due_date", borrowDate.equals("-") ? "-" : LocalDate.parse(borrowDate).plusDays(14).toString());
                String returnDate = r.isNull("return_date")
                        ? "-"
                        : r.optString("return_date");


                long fine = calculateFine(dueDate, returnDate, status);

                table.addCell(String.valueOf(r.optInt("borrow_id", 0)), center);
                table.addCell(bookTitle, center);
                table.addCell(status, center);
                table.addCell(isWalkIn ? "Yes" : "No", center);
                table.addCell(borrowDate, center);
                table.addCell(pickupDate, center);
                table.addCell(dueDate, center);
                table.addCell(returnDate, center);
                table.addCell("RM" + fine, center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading borrow history: " + e.getMessage());
            }
        }

        System.out.println("\n📚 BORROW HISTORY");
        System.out.println(table.render());
    }

    // ======================= FINE TABLES =======================
    public static void displayFines(JSONArray fines) {
        if (fines == null || fines.length() == 0) {
            System.out.println("❌ No fines found.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(8, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Fine ID", center);
        table.addCell("Borrow ID", center);
        table.addCell("Member Name", center);
        table.addCell("Amount (RM)", center);
        table.addCell("Status", center);
        table.addCell("Due Date", center);
        table.addCell("Return Date", center);
        table.addCell("Overdue Days", center);

        for (int i = 0; i < fines.length(); i++) {
            try {
                JSONObject f = fines.getJSONObject(i);

                int fineId = f.optInt("fine_id", 0);
                int borrowId = f.optInt("borrow_id", 0);
                String memberName = f.optString("memberName", "-");
                double amount = f.optDouble("amount", 0);
                String status = f.optString("status", "Pending");
                String dueDate = f.optString("dueDate", "-");
                String returnDate = f.optString("returnDate", "-");
                int overdueDays = f.optInt("overdueDays", 0);

                table.addCell(String.valueOf(fineId), center);
                table.addCell(String.valueOf(borrowId), center);
                table.addCell(memberName, center);
                table.addCell("RM" + amount, center);
                table.addCell(status, center);
                table.addCell(dueDate, center);
                table.addCell(returnDate, center);
                table.addCell(String.valueOf(overdueDays), center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading fine: " + e.getMessage());
            }
        }

        System.out.println("\n💰 FINES LIST");
        System.out.println(table.render());
    }


    public static void displayFineDetails(JSONObject fine) {
        if (fine == null) {
            System.out.println("❌ Fine not found.");
            return;
        }

        CellStyle left = new CellStyle(CellStyle.HorizontalAlign.left);
        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);

        Table table = new Table(2, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Field", center);
        table.addCell("Value", center);

        table.addCell("Fine ID", left); table.addCell(String.valueOf(fine.optInt("fine_id", 0)), left);
        table.addCell("Borrow ID", left); table.addCell(String.valueOf(fine.optInt("borrow_id", 0)), left);
        table.addCell("Member ID", left); table.addCell(String.valueOf(fine.optInt("member_id", 0)), left);
        table.addCell("Member Name", left); table.addCell(fine.optString("member_name", "-"), left);
        table.addCell("Amount (RM)", left); table.addCell("RM" + fine.optDouble("amount", 0), left);
        table.addCell("Status", left); table.addCell(fine.optBoolean("is_paid", false) ? "Paid ✅" : "Pending ❌", left);
        table.addCell("Created Date", left); table.addCell(fine.optString("created_date", "-"), left);
        table.addCell("Paid Date", left); table.addCell(fine.optString("paid_date", "-"), left);

        System.out.println("\n💵 FINE DETAILS");
        System.out.println(table.render());
    }

    // ======================= REVIEW TIMELINE =======================
    public static void displayReviewTimeline(JSONArray reviews) {
        if (reviews == null || reviews.length() == 0) {
            System.out.println("📭 No reviews yet.");
            return;
        }

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(4, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Member Name", center);
        table.addCell("Rating (1-5)", center); // now numeric
        table.addCell("Comment", center);
        table.addCell("Date", center);

        for (int i = 0; i < reviews.length(); i++) {
            try {
                JSONObject r = reviews.getJSONObject(i);
                String memberName = r.optString("memberName", "Anonymous");
                int rating = r.optInt("rating", 0);
                String comment = r.optString("comment", "");
                String date = r.optString("created_at", "-");

                table.addCell(memberName, center);
                table.addCell(rating + "/5", center); // numeric format
                table.addCell(comment, center);
                table.addCell(date, center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading review: " + e.getMessage());
            }
        }

        System.out.println("\n📚 BOOK REVIEWS");
        System.out.println(table.render());
    }


}
