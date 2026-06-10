package Services;

import Models.Member;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MemberLoginServices {
    private static final String BASE_URL = "https://localhost:7127/api/members";

    private String token = null;
    private int memberId = -1;

    public int getMemberId() { return memberId; }
    public String getToken() { return token; }

    private HttpURLConnection setupConnection(String endpoint, String method) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (token != null) con.setRequestProperty("Authorization", "Bearer " + token);
        con.setDoInput(true);
        con.setDoOutput(!method.equals("GET"));
        return con;
    }

    // ================= REGISTER =================
    public Member register(Member m, String password) throws Exception {
        // Trim inputs
        String name = m.getMember_name() != null ? m.getMember_name().trim() : "";
        String email = m.getEmail() != null ? m.getEmail().trim() : "";
        String phone = m.getPhone_number() != null ? m.getPhone_number().trim() : "";
        password = password != null ? password.trim() : "";

        if (name.isEmpty()) throw new Exception("❌ Name cannot be empty.");
        if (email.isEmpty()) throw new Exception("❌ Email cannot be empty.");
        if (phone.isEmpty()) throw new Exception("❌ Phone number cannot be empty.");
        if (password.isEmpty()) throw new Exception("❌ Password cannot be empty.");

        HttpURLConnection con = setupConnection("/register", "POST");

        JSONObject obj = new JSONObject();
        obj.put("member_name", name);
        obj.put("email", email);
        obj.put("phone_number", phone);
        obj.put("password", password);

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (response.startsWith("{")) {
            JSONObject res = new JSONObject(response);
            if (code >= 200 && code < 300) {
                this.token = res.getString("token");
                this.memberId = res.getInt("member_id");
                return new Member(
                        memberId,
                        res.getString("membership_id"),
                        res.getString("member_name"),
                        res.getString("phone_number"),
                        res.getString("email")
                );
            } else {
                // Friendly error handling
                String msg = res.has("message") ? res.getString("message") : "Registration failed.";
                if (msg.toLowerCase().contains("email")) {
                    throw new Exception("❌ The email '" + email + "' is already registered.");
                } else {
                    throw new Exception("❌ " + msg);
                }
            }
        } else {
            throw new Exception("❌ Unexpected server response: " + response);
        }
    }

    // ================= LOGIN =================
    public Member login(String email, String password) throws Exception {
        email = email != null ? email.trim() : "";
        password = password != null ? password.trim() : "";

        if (email.isEmpty() || password.isEmpty())
            throw new Exception("❌ Email and password cannot be empty.");

        HttpURLConnection con = setupConnection("/login", "POST");

        JSONObject obj = new JSONObject();
        obj.put("email", email);
        obj.put("password", password);

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (response.startsWith("{")) {
            JSONObject res = new JSONObject(response);
            if (code >= 200 && code < 300) {
                this.token = res.getString("token");
                this.memberId = res.getInt("member_id");
                return new Member(
                        memberId,
                        res.getString("membership_id"),
                        res.getString("member_name"),
                        res.getString("phone_number"),
                        res.getString("email")
                );
            } else {
                String msg = res.has("message") ? res.getString("message") : "❌ Email or password is incorrect.";
                throw new Exception(msg);
            }
        } else {
            throw new Exception("❌ Unexpected server response: " + response);
        }
    }

    // ================= VIEW PROFILE =================
    public Member getProfile() throws Exception {
        if (token == null || memberId == -1)
            throw new Exception("❌ Not logged in (token missing)");

        HttpURLConnection con = setupConnection("/profile/" + memberId, "GET");
        int code = con.getResponseCode();

        String response = readStream((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code >= 200 && code < 300) {
            JSONObject res = new JSONObject(response);
            return new Member(
                    res.getInt("member_id"),
                    res.getString("membership_id"),
                    res.getString("member_name"),
                    res.getString("phone_number"),
                    res.getString("email")
            );
        } else {
            throw new Exception("❌ Profile fetch failed (" + code + "): " + response);
        }
    }

    // ================= UPDATE PROFILE =================
    public void updateProfile(Member m) throws Exception {
        if (token == null || memberId == -1)
            throw new Exception("❌ Not logged in");

        HttpURLConnection con = setupConnection("/profile/" + memberId, "PUT");

        JSONObject obj = new JSONObject();
        obj.put("member_name", m.getMember_name());
        obj.put("phone_number", m.getPhone_number());
        obj.put("email", m.getEmail());

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream((code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code >= 200 && code < 300) {
            System.out.println("✅ Profile updated successfully");
        } else {
            throw new Exception("❌ Failed to update profile: " + response);
        }
    }

    // ================= CHANGE PASSWORD =================
    public void changePassword(String oldPassword, String newPassword) throws Exception {
        if (token == null || memberId == -1)
            throw new Exception("❌ Not logged in");

        // Trim inputs
        oldPassword = oldPassword != null ? oldPassword.trim() : "";
        newPassword = newPassword != null ? newPassword.trim() : "";

        if (oldPassword.isEmpty())
            throw new Exception("❌ Old password cannot be empty.");
        if (newPassword.isEmpty())
            throw new Exception("❌ New password cannot be empty.");

        HttpURLConnection con = setupConnection("/change-password/" + memberId, "PUT");

        JSONObject obj = new JSONObject();
        obj.put("OldPassword", oldPassword); // match your C# DTO property
        obj.put("NewPassword", newPassword);

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code >= 200 && code < 300) {
            System.out.println("✅ Password changed successfully");
        } else {
            // Backend returns plain string errors, so just use it
            if (response != null && !response.isEmpty()) {
                throw new Exception("❌ Failed to change password: " + response);
            } else {
                throw new Exception("❌ Failed to change password: Unknown error");
            }
        }
    }

    // ================= LOGOUT =================
    public void logout() {
        this.token = null;
        this.memberId = -1;
    }

    // ================= FORGET PASSWORD =================
    public void forgotPassword(String email) throws Exception {
        email = email.trim();
        if (email.isEmpty())
            throw new Exception("❌ Email cannot be empty.");

        HttpURLConnection con = setupConnection("/forgot-password", "POST");

        JSONObject obj = new JSONObject();
        obj.put("email", email);

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        String response = readStream(code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream());
        con.disconnect();

        if (code == 404) {
            throw new Exception("❌ This email is not registered.");
        }

        if (code >= 200 && code < 300) {
            JSONObject res = new JSONObject(response);
            String tempPassword = res.getString("tempPassword");

            // 📧 Send email from Java
            EmailServices.sendTemporaryPassword(email, tempPassword);

            System.out.println("✅ Temporary password sent to email.");
        } else {
            throw new Exception("❌ Failed to reset password: " + response);
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
