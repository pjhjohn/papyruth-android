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

    private UserInfo () {
        this.name = null;
        this.email = null;
        this.school = null;
        this.isBoy = null;
        this.validUser = false;
    }

    private CharSequence name;
    private CharSequence email;
    private Integer school;
    private Boolean isBoy;
    private boolean validUser;

    public boolean isUserSigningUp() {
        return this.validUser;
    }

    public void clear() {
        this.name = null;
        this.email = null;
        this.school = null;
        this.isBoy = null;
    }

    public void setEmail(CharSequence email) {
        this.email = email;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public boolean isReadyToSubmitForFirstStep() {
        return this.school != null;
    }

    public boolean isReadyToSubmitForSecondStep() {
        return this.school != null
                && this.name != null
                && this.email != null
                && this.isBoy != null;
    }
}
