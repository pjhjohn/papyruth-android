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

    public UserDataResponse() {
        this.user = null;
        this.access_token = null;
        this.success = false;
        this.email_success = false;
    }

    public static UserDataResponse ERROR = new UserDataResponse();
}