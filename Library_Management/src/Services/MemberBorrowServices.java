package Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MemberBorrowServices {

    private static final String BASE_URL = "https://localhost:7127/api/borrow-records";
    private final String token;
    private final int memberId;

    public MemberBorrowServices(String token, int memberId) {
        this.token = token;
        this.memberId = memberId;
    }

    // ================= SETUP CONNECTION =================
    private HttpURLConnection setup(String endpoint, String method) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setDoOutput(!method.equalsIgnoreCase("GET"));
        return con;
    }

    // ================= BORROW BOOK =================
    public JSONObject borrowBook(int bookId) throws Exception {
        HttpURLConnection con = setup("/member/request/" + memberId + "/" + bookId, "POST");
        JSONObject resp = getSafeJsonResponse(con);
        con.disconnect();
        return resp;
    }

    // ================= ACTIVE BORROWS =================
    public JSONArray getActiveBorrowRecords() throws Exception {
        HttpURLConnection con = setup("/member/" + memberId + "/active", "GET");
        JSONArray resp = getSafeJsonArrayResponse(con);
        con.disconnect();
        return resp;
    }

    // ================= BORROW HISTORY =================
    public JSONArray getBorrowHistory() throws Exception {
        HttpURLConnection con = setup("/member/" + memberId + "/history", "GET");
        JSONArray resp = getSafeJsonArrayResponse(con);
        con.disconnect();
        return resp;
    }

    // ================= DELETE BORROW RECORD =================
    public JSONObject deleteBorrowRecord(int borrowId) throws Exception {
        HttpURLConnection con = setup("/" + borrowId, "DELETE");
        JSONObject resp = getSafeJsonResponse(con);
        con.disconnect();
        return resp;
    }

    // ================= GET RETURNED BOOK =================
    public JSONArray getReturnedBooks() throws Exception {
        JSONArray history = getBorrowHistory(); // existing method
        JSONArray returned = new JSONArray();

        for (int i = 0; i < history.length(); i++) {
            JSONObject record = history.getJSONObject(i);
            if ("RETURNED".equalsIgnoreCase(record.optString("status", ""))) {
                returned.put(record);
            }
        }

        return returned;
    }


    // ================= WRITE REQUEST =================
    private void writeRequest(HttpURLConnection con, JSONObject body) throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    // ================= READ RESPONSE =================
    private String readResponse(HttpURLConnection con) throws IOException {
        int code = con.getResponseCode();
        InputStream is = code < 300 ? con.getInputStream() : con.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
//        System.out.println("HTTP " + code + " Response: " + sb);
        return sb.toString().trim();
    }

    // ================= SAFE JSON OBJECT RESPONSE =================
    private JSONObject getSafeJsonResponse(HttpURLConnection con) throws IOException, JSONException {
        String resp = readResponse(con);
        if (resp.isEmpty()) return new JSONObject();

        try {
            return new JSONObject(resp);
        } catch (org.json.JSONException e) {
            JSONObject obj = new JSONObject();
            obj.put("message", resp);
            return obj;
        }
    }

    // ================= SAFE JSON ARRAY RESPONSE =================
    private JSONArray getSafeJsonArrayResponse(HttpURLConnection con) throws IOException, JSONException {
        String resp = readResponse(con);
        if (resp.isEmpty()) return new JSONArray();

        try {
            return new JSONArray(resp);
        } catch (org.json.JSONException e) {
            JSONArray arr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("message", resp);
            arr.put(obj);
            return arr;
        }
    }
}
