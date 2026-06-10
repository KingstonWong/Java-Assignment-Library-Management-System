package Services;

import Models.Member;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MemberServices {

    private static final String BASE_URL = "https://localhost:7127/api/members";

    // ================= GET ALL =================
    public List<Member> getAll() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        if (con.getResponseCode() != 200) {
            System.out.println("Failed to fetch members");
            con.disconnect();
            return new ArrayList<>();
        }

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(con.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        JSONArray arr = new JSONArray(sb.toString());
        List<Member> members = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            members.add(new Member(
                    o.getInt("member_id"),
                    o.getString("membership_id"),
                    o.getString("member_name"),
                    o.getString("phone_number"),
                    o.getString("email")
            ));
        }

        con.disconnect();
        return members;
    }

    // ================= CREATE =================
    public void create(Member member) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        // Build JSON for API (omit Borrow_Records)
//        JSONObject obj = new JSONObject();
//        obj.put("membership_id", java.util.UUID.randomUUID().toString()); // <-- Add this
//        obj.put("member_name", member.getMember_name());
//        obj.put("phone_number", member.getPhone_number());
//        obj.put("email", member.getEmail());

        JSONObject obj = new JSONObject();
        obj.put("member_name", member.getMember_name());
        obj.put("phone_number", member.getPhone_number());
        obj.put("email", member.getEmail());
        obj.put("password", member.getPassword()); // 👈 REQUIRED


        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println("Response code: " + code);
        System.out.println("Response body: " + response);

        if (code == 200 || code == 201) {
            System.out.println("Member created successfully");
        } else {
            System.out.println("Failed to create member");
        }

        con.disconnect();
    }

    // ================= UPDATE =================
    public void update(int id, Member member) throws Exception {

        // 1️⃣ Fetch existing member
        URL getUrl = new URL(BASE_URL + "/admin/" + id);
        HttpURLConnection getCon = (HttpURLConnection) getUrl.openConnection();
        getCon.setRequestMethod("GET");

        int getCode = getCon.getResponseCode();
        if (getCode != 200) {
            throw new Exception("Member not found");
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getCon.getInputStream())
        );

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        getCon.disconnect();

        JSONObject existing = new JSONObject(sb.toString());

        // 2️⃣ Keep old values if empty
        String name = member.getMember_name().isEmpty()
                ? existing.getString("member_name")
                : member.getMember_name();

        String email = member.getEmail().isEmpty()
                ? existing.getString("email")
                : member.getEmail();

        String phone = member.getPhone_number().isEmpty()
                ? existing.getString("phone_number")
                : member.getPhone_number();

        String membershipId = existing.getString("membership_id");

        // 3️⃣ Build JSON
        JSONObject obj = new JSONObject();
        obj.put("member_id", id);
        obj.put("membership_id", membershipId);
        obj.put("member_name", name);
        obj.put("phone_number", phone);
        obj.put("email", email);

        // 4️⃣ SEND PUT → CORRECT ENDPOINT
        URL url = new URL(BASE_URL + "/admin/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        int code = con.getResponseCode();
        InputStream is = (code >= 200 && code < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        while ((line = responseReader.readLine()) != null)
            response.append(line);
        responseReader.close();

        con.disconnect();

        if (code >= 200 && code < 300) {
            System.out.println("✅ Member updated successfully");
        } else {
            throw new Exception(response.toString());
        }
    }

    // ================= DELETE =================
    public void delete(int id, Scanner sc) throws Exception {
        System.out.print("Are you sure you want to delete Member ID " + id + "? (y/n): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (!confirm.equals("y")) {
            System.out.println("Delete cancelled.");
            return;
        }

        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");

        int code = con.getResponseCode();
        if (code == 200) {
            System.out.println("Member deleted successfully");
        } else if (code == 404) {
            System.out.println("Member not found");
        } else {
            System.out.println("Failed to delete member");
        }

        con.disconnect();
    }
}
