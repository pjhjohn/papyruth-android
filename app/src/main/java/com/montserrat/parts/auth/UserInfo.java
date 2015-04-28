package com.montserrat.parts.auth;

import org.json.JSONException;
import org.json.JSONObject;

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

    private CharSequence realname;
    private CharSequence nickname;
    private CharSequence email;
    private Integer university;
    private Integer admission;
    private Boolean isBoy;
    private CharSequence accessToken;

    private UserInfo () {
        this.clear();
    }

    public void clear() {
        this.realname = null;
        this.nickname   = null;
        this.email      = null;
        this.university = null;
        this.isBoy      = null;
        this.admission  = null;
        this.accessToken= null;
    }

    public CharSequence getAccessToken() { return this.accessToken; }
    public void setAccessToken(CharSequence token) { this.accessToken = token; }
    public void setRealname (CharSequence realname) {
        this.realname = realname;
    }
    public void setNickName(CharSequence nickname) {
        this.nickname = nickname;
    }
    public void setEmail(CharSequence email) {
        this.email = email;
    }
    public void setUniversityId (int universityId) {
        this.university = universityId;
    }
    public void setAdmissionYear(int admissionYear) {
        this.admission = admissionYear;
    }
    public void setGender(boolean isBoy) {
        this.isBoy = isBoy;
    }

    public int getCompletionLevel() {
        /* STEP 1 */
        if(this.university == null) return 0;
        /* STEP 2 */
        if(this.realname == null || this.nickname == null || this.email == null || this.isBoy == null || this.admission == null ) return 1;
        return 2;
    }

    public JSONObject toJSONObject () {
        try {
            return new JSONObject()
                    .put("is_boy", this.isBoy)
                    .put("realname", this.realname)
                    .put("nickname", this.nickname)
                    .put("email", this.email)
                    .put("university_id", this.university)
                    .put("entrance_year", this.admission);
        } catch (JSONException e) {
            return null;
        }
    }
}
