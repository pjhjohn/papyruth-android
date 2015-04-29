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

    private CharSequence access_token;
    private CharSequence realname;
    private CharSequence nickname;
    private CharSequence email;
    private Integer university_id;
    private Integer entrance_year;
    private Boolean is_boy;

    private UserInfo () {
        this.clear();
    }

    public void clear() {
        this.access_token = null;
        this.realname    = null;
        this.nickname    = null;
        this.email       = null;
        this.university_id = null;
        this.is_boy = null;
        this.entrance_year = null;
    }

    public CharSequence getAccessToken () {
        return this.access_token;
    }
    public void setAccessToken (CharSequence token) { this.access_token = token; }
    public CharSequence getRealname () {
        return this.realname;
    }
    public void setRealname (CharSequence realname) {
        this.realname = realname;
    }
    public CharSequence getNickname() {
        return this.nickname;
    }
    public void setNickName(CharSequence nickname) {
        this.nickname = nickname;
    }
    public CharSequence getEmail() {
        return this.email;
    }
    public void setEmail(CharSequence email) {
        this.email = email;
    }
    public int getUniversityId() {
        return this.university_id;
    }
    public void setUniversityId (int university_id) {
        this.university_id = university_id;
    }
    public int getAdmissionYear() {
        return this.entrance_year;
    }
    public void setAdmissionYear(int admission_year) {
        this.entrance_year = admission_year;
    }
    public boolean getGenderIsBoe() {
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
        try { this.realname      = data.getString("realname"  );   } catch (JSONException e) {}
        try { this.nickname      = data.getString("nickname"  );   } catch (JSONException e) {}
        try { this.email         = data.getString("email"     );   } catch (JSONException e) {}
        try { this.university_id = data.getInt("university_id");   } catch (JSONException e) {}
        try { this.entrance_year = data.getInt("entrance_year");   } catch (JSONException e) {}
        try { this.is_boy        = data.getBoolean("is_boy"   );   } catch (JSONException e) {}
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
}
