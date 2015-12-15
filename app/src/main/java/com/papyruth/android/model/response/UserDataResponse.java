package com.papyruth.android.model.response;

import com.papyruth.android.model.UserData;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class UserDataResponse {
    public UserData user;
    public String access_token;
    public Boolean success;
    public Boolean email_success;

    public static UserDataResponse ERROR() {
        UserDataResponse response = new UserDataResponse();
        response.user = null;
        response.access_token = null;
        response.success = false;
        return response;
    }
}