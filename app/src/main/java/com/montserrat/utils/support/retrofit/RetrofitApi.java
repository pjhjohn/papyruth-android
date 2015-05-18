package com.montserrat.utils.support.retrofit;

import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.PartialEvaluations;
import com.montserrat.app.model.PartialCourses;
import com.montserrat.app.model.Statistics;
import com.montserrat.app.model.Universities;
import com.montserrat.app.model.User;

import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
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
        Observable<PartialEvaluations> evaluations(
            @Header ("Authorization") String authorization,
            @Query ("university_id") Integer university_id,
            @Query ("since_id") Integer since_id,
            @Query ("max_id") Integer max_id,
            @Query ("limit") Integer limit
        );

        @GET ("/lectures")
        Observable<PartialCourses> lectures(
            @Header ("Authorization") String authorization,
            @Query ("university_id") String university_id,
            @Query ("query") String query
        );

        @GET ("/universities")
        Observable<Universities> universities();

        @GET ("/universities/{univ_id}")
        Observable<Statistics> statistics(@Header ("Authorization") String authorization,@Path ("univ_id") Integer university_id);

        @GET ("/info")
        Observable<Statistics> statistics();

        @GET ("/users/me")
        Observable<User.ResponseData> userinfo(
            @Header ("Authorization") String authorization
        );
        @GET("/lectures/dummy_autocomplete")
        Observable<PartialCourses> lecturelist(
            @Header("Authorization") String authorization,
            @Query("query") String query
        );

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

        @POST("/evaluations")
        Observable<EvaluationForm.ResponseData> evaluation(
            @Header("Authorization") String authorization,
            @Query("course_id") Integer course_id,
            @Query("point_overall") Integer point_overall,
            @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
            @Query("point_easiness") Integer point_easiness,
            @Query("point_clarity") Integer point_clarity,
            @Query("comment") String comment
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
        private RestAdapter.LogLevel logLevel = null;
        public Builder setLogLevel (RestAdapter.LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }
        private RestAdapter.Log log;
        public Builder setLog(RestAdapter.Log log) {
            this.log = log;
            return this;
        }

        public void build () {
            RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(String.format("%s://%s/api/%s/",
                ssl_enabled ? "https" : "http",
                root.isEmpty() ? "" : root.charAt(root.length() - 1) == '/' ? root.substring(0, root.length() - 1) : root,
                version.isEmpty() ? "" : version.charAt(version.length() - 1) == '/' ? version.substring(0, root.length() - 1) : version
            ));
            if(logLevel != null) builder.setLogLevel(logLevel);
            if(log != null) builder.setLog(log);
            RetrofitApi.instance = new RetrofitApi(builder.build().create(Api.class));
        }
    }
}
