package Models;

public class Member {

    private int member_id;
    private String membership_id;
    private String member_name;
    private String phone_number;
    private String email;
    private String password;

    // No-arg constructor
    public Member() {}

    // 5-arg constructor (used by login / profile)
    public Member(int member_id, String membership_id, String member_name,
                  String phone_number, String email) {
        this.member_id = member_id;
        this.membership_id = membership_id;
        this.member_name = member_name;
        this.phone_number = phone_number;
        this.email = email;
    }

    // ✅ 6-arg constructor (ADMIN create / update)
    public Member(int member_id, String membership_id, String member_name,
                  String phone_number, String email, String password) {
        this.member_id = member_id;
        this.membership_id = membership_id;
        this.member_name = member_name;
        this.phone_number = phone_number;
        this.email = email;
        this.password = password;
    }

    // ===== getters & setters =====
    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public String getMembership_id() {
        return membership_id;
    }

    public void setMembership_id(String membership_id) {
        this.membership_id = membership_id;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ⚠ password should ONLY be used for sending to API
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
