package com.papyruth.android.model;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class UserData {
    // Fields given from the API
    public Integer id;
    public String realname;
    public String nickname;
    public String email;
    public Integer entrance_year;
    public Integer university_id;
    public Boolean is_boy;
    public Boolean confirmed;
    public String university_email;
    public Boolean university_confirmed;

    // Fields not given from the API
    public Integer mandatory_evaluation_count;  // TODO : api should response this field
    public String university_name;              // TODO : Check if this field is required
    public String university_image_url;         // TODO : Check if this field is required
    public String avatar_url;                   // TODO : Check if this field is required
}