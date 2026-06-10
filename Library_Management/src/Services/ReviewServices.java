package Services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReviewServices {

    private static final String BASE_URL = "https://localhost:7127/api/reviews";

    private final String token;
    private final int memberId;

    public ReviewServices(String token, int memberId) {
        this.token = token;
        this.memberId = memberId;
    }

    // ================= CONNECTION SETUP =================
    private HttpURLConnection setupConnection(String endpoint, String method) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (token != null && !token.isEmpty()) {
            con.setRequestProperty("Authorization", "Bearer " + token);
        }
        con.setDoInput(true);
        con.setDoOutput(!method.equals("GET"));
        return con;
    }

    // ================= ADD REVIEW =================
    public void addReview(int bookId, int rating, String comment) throws Exception {

        if (token == null || token.isEmpty() || memberId == -1)
            throw new Exception("❌ Please login before adding a review.");

        if (rating < 1 || rating > 5)
            throw new Exception("❌ Rating must be between 1 and 5.");

        if (comment == null || comment.trim().isEmpty())
            throw new Exception("❌ Comment cannot be empty.");

        HttpURLConnection con = setupConnection("", "POST");

        JSONObject obj = new JSONObject();
        obj.put("book_id", bookId);
        obj.put("rating", rating);
        obj.put("comment", comment.trim());

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream(code >= 200 && code < 300
                ? con.getInputStream()
                : con.getErrorStream());
        con.disconnect();

        if (code >= 200 && code < 300) {
            System.out.println("✅ Review added successfully.");
        } else {
            throw new Exception("❌ Failed to add review: " + response);
        }
    }

    // ================= REVIEW TIMELINE =================
    public JSONArray getReviewsByBook(int bookId) throws Exception {

        HttpURLConnection con = setupConnection("/book/" + bookId, "GET");
        int code = con.getResponseCode();

        String response = readStream(code >= 200 && code < 300
                ? con.getInputStream()
                : con.getErrorStream());
        con.disconnect();

        if (code >= 200 && code < 300) {
            return new JSONArray(response);
        } else {
            throw new Exception("❌ Failed to load reviews: " + response);
        }
    }

    // ================= HELPER =================
    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}
