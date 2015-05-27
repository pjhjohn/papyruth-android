package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-04-17.
 * Used for passing data during sign up process.
 */
public class User {
    private static User instance = null;

    public static synchronized User getInstance () {
        if ( User.instance == null ) User.instance = new User();
        return User.instance;
    }

    private String access_token;
    private String realname;
    private String nickname;
    private String email;
    private Boolean is_boy;
    private Integer university_id;
    private String university_name;
    private Integer entrance_year;

    private User () {
        this.clear();
    }

    public void clear() {
        this.access_token  = null;
        this.realname      = null;
        this.nickname      = null;
        this.email         = null;
        this.is_boy        = null;
        this.university_id = null;
        this.entrance_year = null;
    }

    public String getAccessToken () {
        return this.access_token;
    }
    public void setAccessToken (String token) {
        if (token != null && !token.isEmpty()) this.access_token = String.format("Token token=\"%s\"", token);
    }
    public String getRealname () {
        return this.realname;
    }
    public void setRealname (String realname) {
        this.realname = realname;
    }
    public String getNickname() {
        return this.nickname;
    }
    public void setNickName(String nickname) {
        this.nickname = nickname;
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Integer getUniversityId() {
        return this.university_id;
    }
    public void setUniversityId (Integer university_id) {
        this.university_id = university_id;
    }
    public Integer getAdmissionYear() {
        return this.entrance_year;
    }
    public void setEntranceYear (Integer admission_year) {
        this.entrance_year = admission_year;
    }
    public boolean getGenderIsBoy() {
        return this.is_boy;
    }
    public void setGenderIsBoy (boolean is_boy) {
        this.is_boy = is_boy;
    }
    public void setUniversityName(String university_name) {
        this.university_name = university_name;
    }
    public String getUniversityName () {
        return this.university_name;
    }

    public void update(UserData user) {
        this.update(user, null);
    }
    public void update(UserData user, String access_token) {
        if(user.email != null) this.setEmail(user.email);
        if(user.realname != null) this.setRealname(user.realname);
        if(user.nickname != null) this.setNickName(user.nickname);
        if(user.is_boy != null) this.setGenderIsBoy(user.is_boy);
        if(user.university_id!=null) this.setUniversityId(user.university_id);
        if(user.entrance_year!=null) this.setEntranceYear(user.entrance_year);
        if(access_token!=null) this.setAccessToken(access_token);
    }

    @Override
    public String toString() {
        return String.format("<access_token : %s>\n <realname : %s>\n <nickname : %s>\n <email : %s>\n <university_id : %d>\n <entrance_year : %d>\n <is_boy : %b>\n", this.access_token, this.realname, this.nickname, this.email, this.university_id, this.entrance_year, this.is_boy);
    }

    public static class ResponseData {
        public UserData user;
        public String access_token;
        public Boolean success;
    }

    private static class UserData {
        public Integer id;
        public Boolean is_boy;
        public Integer university_id;
        public Boolean confirmed;
        public String realname;
        public String nickname;
        public String email;
        public Integer entrance_year;

        public String confirmed_at;
        public String confirmation_token;
        public String confirmation_sent_at;
        public Boolean confirmation_is_needed;
        public String reset_password_token;
        public String reset_password_sent_at;
        public Boolean allowed;
        public String current_sign_in_at;
        public String current_sign_in_ip;
        public String last_sign_in_ip;
        public Integer sign_in_count;
        public String created_at;
        public String updated_at;
        public String pwd_salt;
        public String pwd_hash;
        public String dropped_out;
        public String dropped_out_at;
    }
}
