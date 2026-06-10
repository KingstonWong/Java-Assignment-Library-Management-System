package Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BorrowRecordServices {

    private static final String BASE_URL = "https://localhost:7127/api/borrow-records";
    private final String token;

    public BorrowRecordServices() {
        this.token = null;
    }

    public BorrowRecordServices(String token) {
        this.token = token;
    }

    public String borrowBook(int memberId, int bookId, boolean isWalkIn) throws IOException, JSONException {
        String endpoint = isWalkIn ? "/admin/borrow" : "/member/request/" + memberId + "/" + bookId;
        JSONObject body = isWalkIn ? new JSONObject().put("member_id", memberId).put("book_id", bookId) : null;
        return sendRequest("POST", endpoint, body);
    }

    public JSONArray getAll() throws IOException {
        return getJsonArray("/");
    }

    public JSONArray getApproved() throws IOException {
        return getJsonArray("/approved");
    }

    public JSONArray getPending() throws IOException {
        return getJsonArray("/pending");
    }

    public JSONArray searchPending(String keyword) throws IOException {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        return getJsonArray("/pending/search?keyword=" + encoded);
    }

    public String approveBorrow(int borrowId) throws IOException {
        return sendRequest("PUT", "/admin/approve/" + borrowId, null);
    }

    public String confirmPickup(int borrowId) throws IOException {
        return sendRequest("PUT", "/admin/pickup/" + borrowId, null);
    }

    public String returnBook(int borrowId) throws IOException {
        return sendRequest("PUT", "/return/" + borrowId, null);
    }

    public String deleteRecord(int borrowId) throws IOException {
        return sendRequest("DELETE", "/" + borrowId, null);
    }

    public JSONArray getByMemberId(int memberId) throws IOException {
        return getJsonArray("/member/" + memberId);
    }

    // =================== PRIVATE HELPERS ===================
    private String sendRequest(String method, String endpoint, JSONObject body) throws IOException {
        HttpURLConnection con = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (token != null) con.setRequestProperty("Authorization", "Bearer " + token);
            if (body != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            return readResponse(con);
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private JSONArray getJsonArray(String endpoint) throws IOException {
        String resp = sendRequest("GET", endpoint, null);
        if (resp.isEmpty() || resp.equalsIgnoreCase("null")) return new JSONArray();
        try { return new JSONArray(resp); }
        catch (JSONException e) { System.out.println("❌ Response not a valid JSON array: " + resp); return new JSONArray(); }
    }

    private String readResponse(HttpURLConnection con) throws IOException {
        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        if (code < 200 || code >= 300) System.out.println("❌ HTTP " + code + " Response: " + sb);
        return sb.toString().trim();
    }
}
