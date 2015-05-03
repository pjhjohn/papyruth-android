package com.montserrat.parts.auth;

import android.util.Log;

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

    private String access_token;
    private String realname;
    private String nickname;
    private String email;
    private Boolean is_boy;
    private Integer university_id;
    private Integer entrance_year;

    private UserInfo () {
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
    public void setAdmissionYear(Integer admission_year) {
        this.entrance_year = admission_year;
    }
    public boolean getGenderIsBoy() {
        return this.is_boy;
    }
    public void setGenderIsBoy (boolean is_boy) {
        this.is_boy = is_boy;
    }

    public int getCompletionLevel() {
        /* STEP 1 */
        if(this.university_id == null) return 0;
        /* STEP 2 */
        if(this.realname == null || this.nickname == null || this.email == null || this.is_boy == null || this.entrance_year == null ) return 1;
        return 2;
    }

    /**
     * @param data data to assign
     */
    public void setData(JSONObject data) {
        this.setAccessToken(data.optString("access_token", null));
        try { this.realname      = data.getString("realname");   } catch (JSONException e) { e.printStackTrace(); }
        try { this.nickname      = data.getString("nickname");   } catch (JSONException e) { e.printStackTrace(); }
        try { this.email         = data.getString("email");   } catch (JSONException e) { e.printStackTrace(); }
        try { this.university_id = data.getInt("university_id");   } catch (JSONException e) { e.printStackTrace(); }
        try { this.entrance_year = data.getInt("entrance_year");   } catch (JSONException e) { e.printStackTrace(); }
        try { this.is_boy        = data.getBoolean("is_boy");   } catch (JSONException e) { e.printStackTrace(); }
    }

    /**
     * @return null if there exist an exception during assigning values
     */
    public JSONObject getData () {
        JSONObject data = new JSONObject();
        try {
            if(this.realname      != null) data.put("realname"       , this.realname     );
            if(this.nickname      != null) data.put("nickname"       , this.nickname     );
            if(this.email         != null) data.put("email"          , this.email        );
            if(this.university_id != null) data.put("university_id"  , this.university_id);
            if(this.entrance_year != null) data.put("entrance_year"  , this.entrance_year);
            if(this.is_boy        != null) data.put("is_boy"         , this.is_boy       );
        } catch (JSONException e) {
            data = null;
        }
        return data;
    }

    @Override
    public String toString() {
        return String.format("<access_token : %s>\n <realname : %s>\n <nickname : %s>\n <email : %s>\n <university_id : %d>\n <entrance_year : %d>\n <is_boy : %b>\n", this.access_token, this.realname, this.nickname, this.email, this.university_id, this.entrance_year, this.is_boy);
    }
}
