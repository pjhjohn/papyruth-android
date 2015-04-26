package com.montserrat.parts.auth;

/**
 * Created by pjhjohn on 2015-04-17.
 * Used for passing data during sign up process.
 */
public class UserInfo {
    private static UserInfo instance = null;

    public static synchronized UserInfo getInstance () {
        if ( UserInfo.instance == null ) UserInfo.instance = new UserInfo();
        return UserInfo.instance;
    }

    private CharSequence name;
    private CharSequence nickname;
    private CharSequence email;
    private Integer university;
    private Integer admission;
    private Boolean isBoy;
    private boolean validUser;

    private UserInfo () {
        this.clear();
    }

    public void clear() {
        this.name       = null;
        this.nickname   = null;
        this.email      = null;
        this.university = null;
        this.isBoy      = null;
        this.admission  = null;
        this.validUser  = false;
    }
    public boolean isUserValid () {
        return this.validUser;
    }

    public void setNickName(CharSequence nickname) {
        this.nickname = nickname;
    }
    public void setEmail(CharSequence email) {
        this.email = email;
    }
    public void setName(CharSequence name) {
        this.name = name;
    }
    public void setSchool(int universityId) {
        this.university = universityId;
    }
    public void setAdmissionYear(int admissionYear) {
        this.admission = admissionYear;
    }
    public void setGender(boolean isBoy) {
        this.isBoy = isBoy;
    }

    public boolean isDataReadyOnStep1 () {
        return this.university != null;
    }

    public boolean isDataReadyOnStep2 () {
        return this.university != null
                && this.name != null
                && this.nickname != null
                && this.email != null
                && this.isBoy != null
                && this.admission != null;
    }
}
