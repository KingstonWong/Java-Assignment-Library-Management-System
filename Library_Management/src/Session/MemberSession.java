package Session;

public class MemberSession {
    private static Integer memberId = null;

    public static void login(int id) {
        memberId = id;
    }

    public static int getMemberId() {
        return memberId;
    }

    public static boolean isLoggedIn() {
        return memberId != null;
    }

    public static void logout() {
        memberId = null;
    }
}
