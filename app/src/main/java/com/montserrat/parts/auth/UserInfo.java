package com.montserrat.parts.auth;

/**
 * Created by pjhjohn on 2015-04-17.
 * Used for passing data during sign up process.
 */
public class UserInfo {
    private static UserInfo ourInstance = new UserInfo();

    public static synchronized UserInfo getInstance () {
        return ourInstance;
    }

    private CharSequence name;
    private CharSequence nickname;
    private CharSequence email;
    private Integer school;
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
        this.school     = null;
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
    public void setSchool(int schoolCode) {
        this.school = schoolCode;
    }
    public void setAdmissionYear(int admissionYear) {
        this.admission = admissionYear;
    }
    public void setGender(boolean isBoy) {
        this.isBoy = isBoy;
    }

    public boolean isReadyToSubmitForFirstStep() {
        return this.school != null;
    }

    public boolean isReadyToSubmitForSecondStep() {
        return this.school != null
                && this.name != null
                && this.nickname != null
                && this.email != null
                && this.isBoy != null
                && this.admission != null;
    }
}
