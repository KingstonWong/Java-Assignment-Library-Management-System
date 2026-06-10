package Services;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FineServices {

    private static final String BASE_URL = "https://localhost:7127/api/fines";
    private final String token;

    public FineServices() { this.token = null; }
    public FineServices(String token) { this.token = token; }

    // ============================
    // CALCULATE FINE
    // ============================
    public String calculateFine(int borrowId) throws IOException {
        return sendRequest("POST", "/calculate/" + borrowId, null);
    }

    // ============================
    // GET ALL FINES
    // ============================
    public JSONArray getAllFines() throws IOException, JSONException {
        return getJsonArray("/");
    }

    // ============================
    // GET FINE BY ID
    // ============================
    public JSONObject getFineById(int fineId) throws IOException, JSONException {
        String resp = sendRequest("GET", "/" + fineId, null);

        if (resp == null || resp.isEmpty()) {
            return null;
        }

        resp = resp.trim();

        // ❗ IMPORTANT: handle non-JSON responses (404, errors)
        if (!resp.startsWith("{")) {
            return null;
        }

        return new JSONObject(resp);
    }

    // ============================
    // PAY FINE (with check)
    // ============================
    public String payFine(int fineId) throws IOException, JSONException {
        JSONObject fine = getFineById(fineId);
        if (fine == null) return "❌ Fine not found.";

        boolean isPaid = fine.optBoolean("is_paid", false); // <-- check the boolean field
        if (isPaid) return "✅ This fine has already been paid.";

        return sendRequest("PUT", "/pay/" + fineId, null);
    }

    // ============================
    // DELETE FINE
    // ============================
    public boolean deleteFine(int fineId) throws IOException {
        String resp = sendRequest("DELETE", "/" + fineId, null);

        if (resp == null) return false;

        resp = resp.toLowerCase();
        return !resp.contains("not found") && !resp.contains("404");
    }


    // ============================
    // SEARCH FINES (borrow ID or member name)
    // ============================
    public JSONArray searchFines(String keyword) throws IOException, JSONException {
        JSONArray allFines = getAllFines();
        JSONArray results = new JSONArray();

        for (int i = 0; i < allFines.length(); i++) {
            JSONObject f = allFines.getJSONObject(i);
            int borrowId = f.optInt("borrow_id", 0);
            String memberName = f.optString("memberName", "").toLowerCase();
            String key = keyword.toLowerCase();

            // Match borrow ID (numeric) or member name
            if (String.valueOf(borrowId).contains(key) || memberName.contains(key)) {
                results.put(f);
            }
        }

        return results;
    }

    // ============================
    // PRIVATE METHODS
    // ============================
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

    private JSONArray getJsonArray(String endpoint) throws IOException, JSONException {
        String resp = sendRequest("GET", endpoint, null);
        if (resp.isEmpty() || resp.equalsIgnoreCase("null")) return new JSONArray();
        return new JSONArray(resp);
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
