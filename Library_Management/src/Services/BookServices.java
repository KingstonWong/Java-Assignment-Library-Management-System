package Services;

import Models.Book;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BookServices {

    private static final String BASE_URL = "https://localhost:7127/api/books";

    public List<Book> getAll() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        con.disconnect();

        JSONArray arr = new JSONArray(sb.toString());
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            books.add(new Book(
                    o.getInt("book_id"),
                    o.getString("isbn"),
                    o.getString("title"),
                    o.getString("description"),
                    o.getString("author"),
                    o.getString("publisher"),
                    o.getInt("publication_year"),
                    o.getString("category"),
                    o.getString("location"),
                    o.getInt("total_copies"),
                    o.getInt("available_copies")
            ));
        }
        return books;
    }

    public Book getById(int id) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        con.disconnect();

        JSONObject o = new JSONObject(sb.toString());
        return new Book(
                o.getInt("book_id"),
                o.getString("isbn"),
                o.getString("title"),
                o.getString("description"),
                o.getString("author"),
                o.getString("publisher"),
                o.getInt("publication_year"),
                o.getString("category"),
                o.getString("location"),
                o.getInt("total_copies"),
                o.getInt("available_copies")
        );
    }

    // ================= SEARCH BOOKS =================
    public List<Book> searchBook(String keyword) throws Exception {
        // Encode keyword for URL
        String query = java.net.URLEncoder.encode(keyword, "UTF-8");
        URL url = new URL(BASE_URL + "/search?keyword=" + query);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        con.disconnect();

        JSONArray arr = new JSONArray(sb.toString());
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            books.add(new Book(
                    o.getInt("book_id"),
                    o.getString("isbn"),
                    o.getString("title"),
                    o.getString("description"),
                    o.getString("author"),
                    o.getString("publisher"),
                    o.getInt("publication_year"),
                    o.getString("category"),
                    o.getString("location"),
                    o.getInt("total_copies"),
                    o.getInt("available_copies")
            ));
        }
        return books;
    }


    public void create(Book book) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        JSONObject obj = new JSONObject();
        obj.put("isbn", book.getIsbn());
        obj.put("title", book.getTitle());
        obj.put("description", book.getDescription());
        obj.put("author", book.getAuthor());
        obj.put("publisher", book.getPublisher());
        obj.put("publication_year", book.getPublication_year());
        obj.put("category", book.getCategory());
        obj.put("location", book.getLocation());
        obj.put("total_copies", book.getTotal_copies());
        obj.put("available_copies", book.getAvailable_copies());

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        printResponse(con);
        con.disconnect();
    }

    public void update(int id, Book book) throws Exception {
        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        JSONObject obj = new JSONObject();
        obj.put("isbn", book.getIsbn());
        obj.put("title", book.getTitle());
        obj.put("description", book.getDescription());
        obj.put("author", book.getAuthor());
        obj.put("publisher", book.getPublisher());
        obj.put("publication_year", book.getPublication_year());
        obj.put("category", book.getCategory());
        obj.put("location", book.getLocation());
        obj.put("total_copies", book.getTotal_copies());
        obj.put("available_copies", book.getAvailable_copies());

        try (OutputStream os = con.getOutputStream()) {
            os.write(obj.toString().getBytes("UTF-8"));
        }

        printResponse(con);
        con.disconnect();
    }

    public boolean delete(int id, boolean isBorrowed) throws Exception {
        if (isBorrowed) {
            System.out.println("❌ Cannot delete. Book is currently borrowed.");
            return false;
        }

        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");

        int status = con.getResponseCode();
        con.disconnect();

        return status >= 200 && status < 300;
    }

    private void printResponse(HttpURLConnection con) throws Exception {
        InputStream is = con.getResponseCode() >= 200 && con.getResponseCode() < 300
                ? con.getInputStream() : con.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) System.out.println(line);
        br.close();
    }
}
