import Services.BookServices;
import Services.MemberServices;
import Services.BorrowRecordServices;
import Services.FineServices;
import Services.ReviewServices;
import Models.Book;
import Models.Member;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Scanner;

public class AdminPortal {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("==================================");
        System.out.println("           ADMIN LOGIN");
        System.out.println("==================================");

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        if (username.equals("admin") && password.equals("123")) {
            System.out.println("\n✅ Login successful! Welcome, Admin.");
            adminMenu(sc);
        } else {
            System.out.println("\n❌ Invalid username or password. Returning to main menu...");
        }
    }

    // ================= ADMIN MENU =================
    private static void adminMenu(Scanner sc) {
        BookServices bookService = new BookServices();
        MemberServices memberService = new MemberServices();
        BorrowRecordServices borrowService = new BorrowRecordServices();
        FineServices fineService = new FineServices();

        while (true) {
            System.out.println("\n==================================");
            System.out.println("      LIBRARY ADMIN PORTAL");
            System.out.println("==================================");
            System.out.println("1. Manage Books");
            System.out.println("2. Manage Members");
            System.out.println("3. Manage Borrow Records");
            System.out.println("4. Dashboard");
            System.out.println("5. Manage Fines");
            System.out.println("0. Log Out");
            System.out.print("Choice: ");

            int choice = inputInt(sc);

            switch (choice) {
                case 1 -> manageBooks(bookService, sc);
                case 2 -> manageMembers(memberService, sc);
                case 3 -> manageBorrowRecords(borrowService, bookService, memberService, fineService, sc);
                case 4 -> showDashboard(borrowService);
                case 5 -> manageFines(fineService, borrowService, sc);
                case 0 -> {
                    System.out.println("Returning to main menu 👋");
                    return;
                }
                default -> System.out.println("❌ Invalid choice!");
            }
        }
    }

    // ================= DASHBOARD =================
    private static void showDashboard(BorrowRecordServices borrowService) {
        try {
            JSONArray records = borrowService.getAll();
            displayTable.showDashboard(records);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ================= BOOK MENU =================
    private static void manageBooks(BookServices bookService, Scanner sc) {
        while (true) {
            System.out.println("\n--- Book Management ---");
            System.out.println("1. View All Books");
            System.out.println("2. View Book Details (By ID)");
            System.out.println("3. Search Books");
            System.out.println("4. Add Book");
            System.out.println("5. Update Book");
            System.out.println("6. Delete Book");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            int choice = inputInt(sc);

            try {
                switch (choice) {
                    case 1 -> displayTable.displayBooks(bookService.getAll());
                    case 2 -> viewBookDetails(bookService, sc);
                    case 3 -> searchBooks(bookService, sc);
                    case 4 -> addBook(bookService, sc);
                    case 5 -> updateBook(bookService, sc);
                    case 6 -> deleteBook(bookService, sc);
                    case 0 -> { return; }
                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static void viewBookDetails(BookServices bookService, Scanner sc) throws Exception {
        int id = inputInt(sc, "Enter Book ID");
        Book book = bookService.getById(id);

        if (book != null) {
            // Display book info
            displayTable.displayBookDetails(book);

            // Display book reviews
            try {
                ReviewServices reviewService = new ReviewServices(null, -1); // admin doesn't need token/memberId
                JSONArray reviews = reviewService.getReviewsByBook(id);

                if (reviews.length() == 0) {
                    System.out.println("\n📭 No reviews yet for this book.");
                } else {
                    displayTable.displayBookReviews(reviews);
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to load reviews: " + e.getMessage());
            }
        } else {
            System.out.println("❌ Book not found");
        }
    }



    private static void searchBooks(BookServices bookService, Scanner sc) throws Exception {
        System.out.print("Enter keyword (Title, ISBN, Author): ");
        String keyword = sc.nextLine().trim();
        List<Book> books = bookService.searchBook(keyword);
        if (books.isEmpty()) System.out.println("❌ No books found");
        else displayTable.displayBooks(books);
    }

    private static void addBook(BookServices bookService, Scanner sc) throws Exception {
        System.out.print("ISBN: "); String isbn = sc.nextLine().trim();
        System.out.print("Title: "); String title = sc.nextLine().trim();
        System.out.print("Description: "); String desc = sc.nextLine().trim();
        System.out.print("Author: "); String author = sc.nextLine().trim();
        System.out.print("Publisher: "); String publisher = sc.nextLine().trim();
        int year = inputInt(sc, "Publication Year");
        System.out.print("Category: "); String category = sc.nextLine().trim();
        System.out.print("Location: "); String location = sc.nextLine().trim();
        int total = inputInt(sc, "Total Copies");
        int available = total;

        Book book = new Book(0, isbn, title, desc, author, publisher, year, category, location, total, available);
        bookService.create(book);
        System.out.println("✅ Book added successfully");
    }

    private static void updateBook(BookServices bookService, Scanner sc) throws Exception {
        List<Book> books = bookService.getAll();
        displayTable.displayBooks(books);
        int id = inputInt(sc, "Book ID to update");
        Book existing = books.stream().filter(b -> b.getBook_id() == id).findFirst().orElse(null);
        if (existing == null) { System.out.println("❌ Book not found"); return; }

        String isbn = inputOrKeep(sc, "ISBN", existing.getIsbn());
        String title = inputOrKeep(sc, "Title", existing.getTitle());
        String desc = inputOrKeep(sc, "Description", existing.getDescription());
        String author = inputOrKeep(sc, "Author", existing.getAuthor());
        String publisher = inputOrKeep(sc, "Publisher", existing.getPublisher());
        int year = inputIntOrKeep(sc, "Publication Year", existing.getPublication_year());
        String category = inputOrKeep(sc, "Category", existing.getCategory());
        String location = inputOrKeep(sc, "Location", existing.getLocation());
        int total = inputIntOrKeep(sc, "Total Copies", existing.getTotal_copies());
        int available = Math.min(existing.getAvailable_copies(), total);

        Book updated = new Book(id, isbn, title, desc, author, publisher, year, category, location, total, available);
        bookService.update(id, updated);
        System.out.println("✅ Book updated successfully");
    }

    private static void deleteBook(BookServices bookService, Scanner sc) throws Exception {
        int id = inputInt(sc, "Book ID to delete");
        List<Book> books = bookService.getAll();
        Book book = books.stream().filter(b -> b.getBook_id() == id).findFirst().orElse(null);
        if (book == null) { System.out.println("❌ Book not found"); return; }

        boolean isBorrowed = book.getTotal_copies() != book.getAvailable_copies();
        if (confirmAction(sc, "Delete this book?")) {
            boolean deleted = bookService.delete(id, isBorrowed);

            if (deleted) {
                System.out.println("🗑 Book deleted successfully");
            }
        }
    }

    // ================= MEMBER MENU =================
    private static void manageMembers(MemberServices service, Scanner sc) {
        while (true) {
            System.out.println("\n--- Member Management ---");
            System.out.println("1. View All Members");
            System.out.println("2. Add Member");
            System.out.println("3. Update Member");
            System.out.println("4. Delete Member");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            int choice = inputInt(sc);

            try {
                switch (choice) {
                    case 1 -> displayTable.displayMembers(service.getAll());
                    case 2 -> addMember(service, sc);
                    case 3 -> updateMember(service, sc);
                    case 4 -> deleteMember(service, sc);
                    case 0 -> { return; }
                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
        }
    }

    private static void addMember(MemberServices service, Scanner sc) throws Exception {
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Phone: "); String phone = sc.nextLine().trim();
        System.out.print("Password: "); String password = sc.nextLine().trim();

        service.create(new Member(0, "", name, phone, email, password));
        System.out.println("✅ Member added successfully");
    }

    private static void updateMember(MemberServices service, Scanner sc) throws Exception {
        List<Member> members = service.getAll();
        displayTable.displayMembers(members);
        int id = inputInt(sc, "Member ID to update");
        Member old = members.stream().filter(m -> m.getMember_id() == id).findFirst().orElse(null);
        if (old == null) { System.out.println("❌ Member not found"); return; }

        String name = inputOrKeep(sc, "Name", old.getMember_name());
        String email = inputOrKeep(sc, "Email", old.getEmail());
        String phone = inputOrKeep(sc, "Phone", old.getPhone_number());

        service.update(id, new Member(id, old.getMembership_id(), name, phone, email, ""));
        System.out.println("✅ Member updated successfully");
    }

    private static void deleteMember(MemberServices service, Scanner sc) throws Exception {
        int id = inputInt(sc, "Member ID to delete");
        if (confirmAction(sc, "Delete this member?")) {
            service.delete(id, sc);
            System.out.println("🗑 Member deleted successfully");
        }
    }

    // ================= BORROW MENU =================
    private static void manageBorrowRecords(
            BorrowRecordServices borrowService,
            BookServices bookService,
            MemberServices memberService,
            FineServices fineService,
            Scanner sc) {

        while (true) {
            System.out.println("\n--- Borrow Records ---");
            System.out.println("1. View All Borrow Records");
            System.out.println("2. View Pending Borrow Records");
            System.out.println("3. Search Pending Borrow Records");
            System.out.println("4. Approve Pending Borrow (Sends Pickup Email)");
            System.out.println("5. Borrow Book (Admin Walk-In)");
            System.out.println("6. Return Book");
            System.out.println("7. Delete Borrow Record");
            System.out.println("8. Confirm Book Pickup");
            System.out.println("9. View Overdue Books");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            int choice = inputInt(sc);

            try {
                switch (choice) {
                    case 1 -> displayTable.displayBorrowRecords(borrowService.getAll());
                    case 2 -> viewPendingBorrowRecords(borrowService);
                    case 3 -> searchPendingBorrowRecords(borrowService, sc);
                    case 4 -> approvePendingBorrow(borrowService, memberService, sc);
                    case 5 -> borrowBookFlow(borrowService, bookService, memberService, sc);
                    case 6 -> returnBorrowBook(borrowService, fineService, sc);
                    case 7 -> deleteBorrowRecord(borrowService, sc);
                    case 8 -> confirmPickupBorrow(borrowService, sc);
                    case 9 -> {
                        JSONArray records = borrowService.getAll();
                        displayTable.showOverdueBooks(records);
                    }
                    case 0 -> { return; }
                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    // =================== APPROVE PENDING BORROW & SEND EMAIL ===================
    private static void approvePendingBorrow(
            BorrowRecordServices borrowService,
            MemberServices memberService,
            Scanner sc) {

        try {
            JSONArray pending = borrowService.getPending();

            if (pending.length() == 0) {
                System.out.println("❌ No pending borrow requests.");
                return;
            }

            displayTable.displayBorrowRecords(pending);

            int borrowId = inputInt(sc, "Enter Borrow ID to approve");

            JSONObject selected = null;
            for (int i = 0; i < pending.length(); i++) {
                JSONObject r = pending.getJSONObject(i);
                if (r.getInt("borrow_id") == borrowId) {
                    selected = r;
                    break;
                }
            }

            if (selected == null) {
                System.out.println("❌ Invalid Borrow ID.");
                return;
            }

            if (!confirmAction(sc, "Approve this borrow request?")) return;

            borrowService.approveBorrow(borrowId);
            System.out.println("✅ Borrow approved successfully!");

            // ================= EMAIL NOTIFICATION =================
            int memberId = selected.getInt("member_id");
            Member member = memberService.getAll().stream()
                    .filter(m -> m.getMember_id() == memberId)
                    .findFirst()
                    .orElse(null);

            if (member != null) {
                String memberEmail = member.getEmail();
                String memberName = member.getMember_name();

                String bookTitle = selected.optString(
                        "book_title",
                        "Book"
                );

                // ✅ FIX HERE: use "location", not "book_location"
                String bookLocation = selected.optString(
                        "location",
                        "Main Library Counter"
                );

                try {
                    Services.EmailPickUpServices.sendPickupNotification(
                            memberEmail,
                            memberName,
                            bookTitle,
                            bookLocation
                    );
                    System.out.println("📧 Email notification sent to member (" + memberEmail + ")");
                } catch (Exception e) {
                    System.out.println("❌ Failed to send email: " + e.getMessage());
                }

            } else {
                System.out.println("❌ Member not found. Cannot send email notification.");
            }

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }


    // ================= CONFIRM PICKUP =================
    private static void confirmPickupBorrow(BorrowRecordServices borrowService, Scanner sc) {
        try {
            JSONArray approved = borrowService.getApproved();
            if (approved.length() == 0) {
                System.out.println("❌ No approved borrow records to confirm pickup.");
                return;
            }

            displayTable.displayBorrowRecords(approved);

            int borrowId = inputInt(sc, "Enter Borrow ID to confirm pickup");

            JSONObject selected = null;
            for (int i = 0; i < approved.length(); i++) {
                JSONObject r = approved.getJSONObject(i);
                if (r.getInt("borrow_id") == borrowId) {
                    selected = r;
                    break;
                }
            }

            if (selected == null) {
                System.out.println("❌ Invalid Borrow ID.");
                return;
            }

            if (!confirmAction(sc, "Confirm pickup for this borrow?")) return;

            borrowService.confirmPickup(borrowId);
            System.out.println("✅ Pickup confirmed successfully!");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // =================== BORROW FLOW ===================
    private static void borrowBookFlow(
            BorrowRecordServices borrowService,
            BookServices bookService,
            MemberServices memberService,
            Scanner sc) throws Exception {

        System.out.println("\n📚 AVAILABLE BOOKS");
        List<Book> books = bookService.getAll();
        displayTable.displayBooks(books);

        System.out.println("\n👤 MEMBERS");
        displayTable.displayMembers(memberService.getAll());

        int memberId = inputInt(sc, "Member ID");
        int bookId = inputInt(sc, "Book ID");

        Book book = books.stream()
                .filter(b -> b.getBook_id() == bookId)
                .findFirst()
                .orElse(null);

        if (book == null) {
            System.out.println("❌ Book not found");
            return;
        }

        if (book.getAvailable_copies() <= 0) {
            System.out.println("❌ Book is not available");
            return;
        }

        boolean isWalkIn = true;

        String response = borrowService.borrowBook(memberId, bookId, isWalkIn);

        if (response.startsWith("{")) {
            JSONObject json = new JSONObject(response);
            System.out.println("✅ " + json.optString("message", "Book borrowed successfully"));
        } else {
            System.out.println("✅ " + response);
        }
    }

    private static void returnBorrowBook(
            BorrowRecordServices borrowService,
            FineServices fineService,
            Scanner sc) throws Exception {

        int borrowId = inputInt(sc, "Borrow ID to return");
        JSONArray records = borrowService.getAll();

        for (int i = 0; i < records.length(); i++) {
            JSONObject r = records.getJSONObject(i);

            if (r.getInt("borrow_id") == borrowId) {

                if (!r.isNull("return_date")) {
                    System.out.println("❌ Book already returned.");
                    return;
                }

                borrowService.returnBook(borrowId);
                System.out.println("✅ Book returned successfully");

                // ================= FINE HANDLING =================
                String fineResp = fineService.calculateFine(borrowId);

                // Check if response is JSON
                if (fineResp.trim().startsWith("{")) {
                    JSONObject fineJson = new JSONObject(fineResp);

                    double fineAmount = fineJson.optDouble("fine_amount", 0);

                    if (fineAmount > 0) {
                        System.out.println("⚠ Fine due: RM " + fineAmount);
                    } else {
                        System.out.println("✅ No fine charged.");
                    }
                } else {
                    // Plain text message from API
                    System.out.println(fineResp);
                }
                return;
            }
        }
        System.out.println("❌ Borrow ID not found.");
    }


    private static void deleteBorrowRecord(BorrowRecordServices borrowService, Scanner sc) throws Exception {
        int borrowId = inputInt(sc, "Borrow ID to delete");
        if (confirmAction(sc, "Delete this borrow record?")) {
            borrowService.deleteRecord(borrowId);
            System.out.println("🗑 Borrow record deleted");
        }
    }

    // ================= VIEW PENDING =================
    private static void viewPendingBorrowRecords(BorrowRecordServices borrowService) {
        try {
            JSONArray pending = borrowService.getPending();
            if (pending.length() == 0) {
                System.out.println("❌ No pending borrow records found.");
            } else {
                displayTable.displayBorrowRecords(pending);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ================= SEARCH PENDING =================
    private static void searchPendingBorrowRecords(BorrowRecordServices borrowService, Scanner sc) {
        try {
            System.out.print("Enter keyword (Book Title or Member Name) to search pending borrow records: ");
            String keyword = sc.nextLine().trim();
            JSONArray results = borrowService.searchPending(keyword);
            if (results.length() == 0) {
                System.out.println("❌ No pending borrow records found for keyword: " + keyword);
            } else {
                displayTable.displayBorrowRecords(results);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ================= FINE MANAGEMENT =================
    private static void manageFines(FineServices fineService, BorrowRecordServices borrowService, Scanner sc) {
        while (true) {
            System.out.println("\n--- Fine Management ---");
            System.out.println("1. View All Fines");
            System.out.println("2. Search Fines (Borrow ID / Member Name)");
            System.out.println("3. Pay Fine");
            System.out.println("4. Delete Fine");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            int choice = inputInt(sc);

            try {
                switch (choice) {
                    case 1 -> displayTable.displayFines(fineService.getAllFines());

                    case 2 -> {
                        System.out.print("Enter Borrow ID or Member Name to search: ");
                        String keyword = sc.nextLine().trim();
                        JSONArray results = fineService.searchFines(keyword);
                        if (results.length() == 0) {
                            System.out.println("❌ No fines found for: " + keyword);
                        } else {
                            displayTable.displayFines(results);
                        }
                    }

                    case 3 -> {
                        int fineId = inputInt(sc, "Fine ID to pay");
                        String result = fineService.payFine(fineId);

                        if (result.trim().startsWith("{")) {
                            JSONObject json = new JSONObject(result);

                            if (json.optBoolean("is_paid", false)) {
                                System.out.println(
                                        "✅ Fine paid successfully! " +
                                                "(Borrow ID: " + json.optInt("borrow_id") +
                                                ", Amount: RM " + json.optDouble("fine_amount") + ")"
                                );
                            } else {
                                System.out.println("⚠ Fine payment failed.");
                            }

                        } else {
                            // plain text fallback
                            System.out.println(result);
                        }

                    }

                    case 4 -> {
                        int fineId = inputInt(sc, "Fine ID to delete");
                        if (confirmAction(sc, "Delete this fine?")) {
                            boolean deleted = fineService.deleteFine(fineId);
                            if (deleted) {
                                System.out.println("🗑 Fine deleted successfully");
                            } else {
                                System.out.println("❌ Fine not found.");
                            }
                        }
                    }


                    case 0 -> { return; }

                    default -> System.out.println("❌ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }


    // ================= HELPERS =================
    private static String inputOrKeep(Scanner sc, String label, String oldVal) {
        System.out.print(label + " [" + oldVal + "]: ");
        String input = sc.nextLine().trim();
        return input.isEmpty() ? oldVal : input;
    }

    private static int inputIntOrKeep(Scanner sc, String label, int oldVal) {
        while (true) {
            System.out.print(label + " [" + oldVal + "]: ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) return oldVal;
            try { return Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("❌ Enter a valid number"); }
        }
    }

    private static int inputInt(Scanner sc) {
        while (true) {
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.print("❌ Enter a valid number: "); }
        }
    }

    private static int inputInt(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("❌ Enter a valid number"); }
        }
    }

    private static boolean confirmAction(Scanner sc, String message) {
        System.out.print(message + " (Y/N): ");
        String input = sc.nextLine().trim().toLowerCase();
        return input.equals("y");
    }
}
