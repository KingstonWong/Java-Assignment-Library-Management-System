import Services.*;
import Models.Book;
import Models.Member;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.util.List;
import java.util.Scanner;

public class MemberPortal {

    private static final BookServices bookService = new BookServices();
    private static final MemberLoginServices memberLoginServices = new MemberLoginServices();
    private static MemberBorrowServices memberBorrowServices;
    private static Member loggedInMember;

    // ======= START METHOD =======
    public static void start() {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("          MEMBER PORTAL");
            System.out.println("==================================");

            if (loggedInMember == null) {
                running = showLoginMenu(sc); // returns false if user wants to exit
            } else {
                showMemberMenu(sc);
            }
        }

        // When running becomes false (logout or exit), return control to Main.java
        System.out.println("✅ Exiting Member Portal. Returning to Main Menu...");
    }

    // ================= LOGIN MENU =================
    private static boolean showLoginMenu(Scanner sc) {
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Forgot Password");
        System.out.println("0. Back to Main Menu");
        System.out.print("Choice: ");

        int choice = inputInt(sc);

        try {
            switch (choice) {
                case 1 -> login(sc);
                case 2 -> register(sc);
                case 3 -> forgotPassword(sc);
                case 0 -> {
                    System.out.println("👋 Returning to Main Menu...");
                    return false; // Exit portal loop
                }
                default -> System.out.println("❌ Invalid choice");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
        return true;
    }

    // ================= MEMBER MENU =================
    private static void showMemberMenu(Scanner sc) {
        System.out.println("\nWelcome, " + loggedInMember.getMember_name());
        System.out.println("1. View All Books");
        System.out.println("2. View Book Details");
        System.out.println("3. Search Books");
        System.out.println("4. Borrow Book (Send Borrow Request)");
        System.out.println("5. My Borrowed Books (Not Returned)");
        System.out.println("6. Borrow History");
        System.out.println("7. Pending Borrow Requests");
        System.out.println("8. View Overdue Fines");
        System.out.println("9. Review The Book");
        System.out.println("10. View Profile");
        System.out.println("11. Update Profile");
        System.out.println("12. Change Password");
        System.out.println("0. Logout");
        System.out.print("Choice: ");

        int choice = inputInt(sc);

        try {
            switch (choice) {
                case 1 -> displayTable.displayBooks(bookService.getAll());
                case 2 -> viewBookDetails(sc);
                case 3 -> searchBooks(sc);
                case 4 -> borrowBook(sc);
                case 5 -> viewActiveBorrowRecords();
                case 6 -> viewBorrowHistory();
                case 7 -> viewPendingBorrowRequests();
                case 8 -> viewOverdueFines();
                case 9 -> {
                    try {
                        // Initialize ReviewServices
                        ReviewServices reviewService = new ReviewServices(
                                memberLoginServices.getToken(),
                                loggedInMember.getMember_id()
                        );

                        // Fetch returned books for this member
                        JSONArray returnedBooks = memberBorrowServices.getReturnedBooks();

                        // Call reviewBook method
                        reviewBook(sc, reviewService, returnedBooks);

                    } catch (Exception e) {
                        System.out.println("❌ Failed to load returned books: " + e.getMessage());
                    }
                }
                case 10 -> viewProfile();
                case 11 -> updateProfile(sc);
                case 12 -> changePassword(sc);
                case 0 -> logout(); // Will reset loggedInMember
                default -> System.out.println("❌ Invalid choice");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + (e.getMessage() != null ? e.getMessage() : "Invalid server response"));
        }
    }

    // ================= MEMBER BORROW METHODS =================
    private static void borrowBook(Scanner sc) {
        try {
            displayTable.displayBooks(bookService.getAll());
            int bookId = inputInt(sc, "Book ID");
            JSONObject resp = memberBorrowServices.borrowBook(bookId);
            printJsonMessage(resp);
        } catch (Exception e) {
            System.out.println("❌ Failed to borrow book: " + e.getMessage());
        }
    }

    private static void viewActiveBorrowRecords() {
        try {
            JSONArray records = memberBorrowServices.getActiveBorrowRecords();
            displayTable.displayCurrentBorrowedBooks(records);
        } catch (Exception e) {
            System.out.println("❌ Error fetching active borrow records: " + e.getMessage());
        }
    }

    private static void viewBorrowHistory() {
        try {
            JSONArray records = memberBorrowServices.getBorrowHistory();
            displayTable.displayBorrowHistory(records);
        } catch (Exception e) {
            System.out.println("❌ Error fetching borrow history: " + e.getMessage());
        }
    }

    private static void viewPendingBorrowRequests() {
        try {
            JSONArray records = memberBorrowServices.getBorrowHistory();
            JSONArray pending = new JSONArray();

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                if ("PENDING".equalsIgnoreCase(record.optString("status", ""))) {
                    pending.put(record);
                }
            }

            if (pending.length() == 0) {
                System.out.println("📭 No pending borrow requests.");
            } else {
                displayTable.displayBorrowRecords(pending);
            }
        } catch (Exception e) {
            System.out.println("❌ Error fetching pending borrow requests: " + e.getMessage());
        }
    }

    private static void viewOverdueFines() {
        try {
            FineServices fineService = new FineServices(memberLoginServices.getToken());
            JSONArray allFines = fineService.getAllFines();
            JSONArray overdueFines = new JSONArray();

            for (int i = 0; i < allFines.length(); i++) {
                JSONObject f = allFines.getJSONObject(i);
                boolean isPaid = f.optString("status", "Pending ❌").equalsIgnoreCase("Paid ✅");
                int overdueDays = f.optInt("overdueDays", 0);

                // Only include unpaid fines with overdue days > 0
                if (!isPaid && overdueDays > 0) {
                    overdueFines.put(f);
                }
            }

            if (overdueFines.length() == 0) {
                System.out.println("✅ You have no overdue fines.");
            } else {
                displayTable.displayFines(overdueFines);
            }

        } catch (Exception e) {
            System.out.println("❌ Error fetching overdue fines: " + e.getMessage());
        }
    }


    // ================= AUTH METHODS =================
    private static void login(Scanner sc) {
        try {
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            loggedInMember = memberLoginServices.login(email, password);
            System.out.println("✅ Logged in as " + loggedInMember.getMember_name());
            initBorrowService();
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());
        }
    }

    private static void register(Scanner sc) {
        try {
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            System.out.print("Phone Number: ");
            String phone = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine().trim();

            Member m = new Member();
            m.setMember_name(name);
            m.setEmail(email);
            m.setPhone_number(phone);

            loggedInMember = memberLoginServices.register(m, password);
            System.out.println("✅ Registration successful. Logged in as " + loggedInMember.getMember_name());
            initBorrowService();
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }

    private static void forgotPassword(Scanner sc) {
        try {
            System.out.print("Enter your registered email: ");
            String email = sc.nextLine().trim();
            memberLoginServices.forgotPassword(email);
            System.out.println("✅ Password reset email sent (if registered).");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void initBorrowService() {
        if (loggedInMember != null && memberBorrowServices == null) {
            memberBorrowServices = new MemberBorrowServices(
                    memberLoginServices.getToken(),
                    loggedInMember.getMember_id()
            );
        }
    }

    // ================= PROFILE METHODS =================
    private static void viewProfile() {
        try {
            Member m = memberLoginServices.getProfile();
            System.out.println("\n📋 My Profile");
            System.out.println("ID            : " + m.getMember_id());
            System.out.println("Membership ID : " + m.getMembership_id());
            System.out.println("Name          : " + m.getMember_name());
            System.out.println("Email         : " + m.getEmail());
            System.out.println("Phone         : " + m.getPhone_number());
        } catch (Exception e) {
            System.out.println("❌ Failed to fetch profile: " + e.getMessage());
        }
    }

    private static void updateProfile(Scanner sc) {
        try {
            Member m = memberLoginServices.getProfile();

            System.out.print("Name [" + m.getMember_name() + "]: ");
            String name = sc.nextLine().trim();
            if (!name.isEmpty()) m.setMember_name(name);

            System.out.print("Phone [" + m.getPhone_number() + "]: ");
            String phone = sc.nextLine().trim();
            if (!phone.isEmpty()) m.setPhone_number(phone);

            System.out.print("Email [" + m.getEmail() + "]: ");
            String email = sc.nextLine().trim();
            if (!email.isEmpty()) m.setEmail(email);

            memberLoginServices.updateProfile(m);
            System.out.println("✅ Profile updated successfully");
        } catch (Exception e) {
            System.out.println("❌ Failed to update profile: " + e.getMessage());
        }
    }

    private static void changePassword(Scanner sc) {
        try {
            System.out.print("Old Password: ");
            String oldPass = sc.nextLine().trim();
            System.out.print("New Password: ");
            String newPass = sc.nextLine().trim();

            memberLoginServices.changePassword(oldPass, newPass);
            System.out.println("✅ Password changed successfully");
        } catch (Exception e) {
            System.out.println("❌ Failed to change password: " + e.getMessage());
        }
    }

    // ================= LOGOUT =================
    private static void logout() {
        memberLoginServices.logout();
        memberBorrowServices = null;
        loggedInMember = null;
        System.out.println("✅ Logged out successfully");
    }

    // ================= BOOK METHODS =================
    private static void viewBookDetails(Scanner sc) {
        try {
            int bookId = inputInt(sc, "Book ID");
            Book book = bookService.getById(bookId);
            displayTable.displayBookDetails(book);

            // Display reviews
            ReviewServices reviewService = new ReviewServices(
                    memberLoginServices.getToken(),
                    loggedInMember.getMember_id()
            );
            JSONArray reviews = reviewService.getReviewsByBook(bookId);
            displayTable.displayReviewTimeline(reviews);

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch book details or reviews: " + e.getMessage());
        }
    }


    private static void searchBooks(Scanner sc) {
        try {
            System.out.print("Enter keyword (Title, ISBN, Author): ");
            String keyword = sc.nextLine().trim();
            List<Book> books = bookService.searchBook(keyword);
            if (books.isEmpty()) System.out.println("❌ No books found");
            else displayTable.displayBooks(books);
        } catch (Exception e) {
            System.out.println("❌ Failed to search books: " + e.getMessage());
        }
    }

    // ======================= REVIEW BOOK =======================
    public static void reviewBook(Scanner sc, ReviewServices reviewService, JSONArray returnedBooks) {
        if (returnedBooks == null || returnedBooks.length() == 0) {
            System.out.println("❌ You have no returned books to review.");
            return;
        }

        // Use a set to track unique book IDs
        java.util.Set<Integer> seenBookIds = new java.util.HashSet<>();

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.center);
        Table table = new Table(3, BorderStyle.CLASSIC_WIDE, ShownBorders.ALL);

        table.addCell("Book ID", center);
        table.addCell("Title", center);
        table.addCell("Return Date", center);

        for (int i = 0; i < returnedBooks.length(); i++) {
            try {
                JSONObject r = returnedBooks.getJSONObject(i);
                int bookId = r.optInt("book_id", 0);

                if (seenBookIds.contains(bookId)) continue; // skip duplicates
                seenBookIds.add(bookId);

                table.addCell(String.valueOf(bookId), center);
                table.addCell(r.optString("book_title", "-"), center);
                table.addCell(r.optString("return_date", "-"), center);

            } catch (JSONException e) {
                System.out.println("❌ Error reading returned book: " + e.getMessage());
            }
        }

        System.out.println("\n📖 RETURNED BOOKS YOU CAN REVIEW");
        System.out.println(table.render());

        // Ask user to select book
        System.out.print("Enter Book ID to review: ");
        int bookId = Integer.parseInt(sc.nextLine().trim());

        try {
            // Show existing reviews for this book
            JSONArray reviews = reviewService.getReviewsByBook(bookId);
            displayTable.displayReviewTimeline(reviews);

            // Ask if user wants to add a review
            System.out.print("Do you want to add a review? (Y/N): ");
            String choice = sc.nextLine().trim();
            if (!choice.equalsIgnoreCase("Y")) return;

            // Rating input
            int rating;
            do {
                System.out.print("Enter Rating (1–5): ");
                rating = Integer.parseInt(sc.nextLine().trim());
            } while (rating < 1 || rating > 5);

            // Comment input
            System.out.print("Enter Comment: ");
            String comment = sc.nextLine().trim();

            // Add review via service
            reviewService.addReview(bookId, rating, comment);
            System.out.println("✅ Review submitted successfully.");

        } catch (Exception e) {
            System.out.println("❌ Failed to process review: " + e.getMessage());
        }
    }

    // ================= HELPERS =================
    private static int inputInt(Scanner sc) {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Enter a valid number: ");
            }
        }
    }

    private static int inputInt(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Enter a valid number");
            }
        }
    }

    private static void printJsonMessage(JSONObject obj) {
        try {
            System.out.println(obj.getString("message"));
        } catch (Exception e) {
            System.out.println(obj.toString());
        }
    }
}
