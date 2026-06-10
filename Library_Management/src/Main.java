import java.util.Scanner;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class Main {

    public static void main(String[] args) {

        // 🔐 Disable SSL verification (DEV ONLY)
        disableSSLVerification();

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("==================================");
            System.out.println("    LIBRARY MANAGEMENT SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Admin Portal");
            System.out.println("2. Member Portal");
            System.out.println("0. Exit");
            System.out.print("Select portal: ");

            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1 -> {
                    System.out.println("\nEntering Admin Portal...");
                    AdminPortal.main(args);
                }
                case 2 -> {
                    System.out.println("\nEntering Member Portal...");
                    MemberPortal.start(); // to be implemented
                }
                case 0 -> {
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ⚠️ DEVELOPMENT ONLY – DO NOT USE IN PRODUCTION
    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
