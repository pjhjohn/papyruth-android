package com.montserrat.utils.etc;

import com.montserrat.app.model.Evaluations;
import com.montserrat.app.model.Lectures;
import com.montserrat.app.model.Universities;
import com.montserrat.app.model.User;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by pjhjohn on 2015-05-09.
 */
public class RetrofitApi {
    private static RetrofitApi instance = null;
    private Api api;

    public RetrofitApi (Api api) {
        this.api = api;
    }

    public static Api getInstance() {
        return RetrofitApi.instance.api;
    }

    public interface Api {
        @GET ("/evaluations")
        Observable<Evaluations> evaluations(
            @Header ("Authorization") String authorization,
            @Query ("university_id") Integer university_id,
            @Query ("since_id") Integer since_id,
            @Query ("max_id") Integer max_id,
            @Query ("limit") Integer limit
        );

        @GET ("/lectures")
        Observable<Lectures> lectures(
            @Header ("Authorization") String authorization,
            @Query ("university_id") String university_id,
            @Query ("query") String query
        );

        @GET ("/universities")
        Observable<Universities> universities();

        @POST("/users/sign_in")
        Observable<User.ResponseData> signin(
            @Query("email") String email,
            @Query("password") String password
        );

        @POST ("/users/sign_up")
        Observable<User.ResponseData> signup(
            @Query("email") String email,
            @Query("password") String password,
            @Query("realname") String realname,
            @Query("nickname") String nickname,
            @Query("is_boy") Boolean is_boy,
            @Query("university_id") Integer university_id,
            @Query("entrance_year") Integer entrance_year
        );
    }

    /* Api Builder */
    public static class Builder {
        public Builder () {}
        private String root;
        public Builder setRoot (String root) {
            this.root = root;
            return this;
        }
        private String version;
        public Builder setVersion (String version) {
            this.version = version;
            return this;
        }
        private boolean ssl_enabled;
        public Builder enableSSL (boolean enable) {
            this.ssl_enabled = enable;
            return this;
        }

        public void build () {
            RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(String.format("%s://%s/api/%s/",
                ssl_enabled ? "https" : "http",
                root.isEmpty() ? "" : root.charAt(root.length() - 1) == '/' ? root.substring(0, root.length() - 1) : root,
                version.isEmpty() ? "" : version.charAt(version.length() - 1) == '/' ? version.substring(0, root.length() - 1) : version
            ));
            RetrofitApi.instance = new RetrofitApi(builder.build().create(Api.class));
        }
    }
}
