package com.montserrat.utils.etc;

import com.montserrat.app.model.Autocomplete;
import com.montserrat.app.model.Dummy_lecture;
import com.montserrat.app.model.Evaluation;
import com.montserrat.app.model.EvaluationForm;
import com.montserrat.app.model.Evaluations;
import com.montserrat.app.model.Lectures;
import com.montserrat.app.model.Statistics;
import com.montserrat.app.model.Universities;
import com.montserrat.app.model.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.RxVolley;

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

        @GET ("/universities/{univ_id}")
        Observable<Statistics> statistics(@Header ("Authorization") String authorization,@Path ("univ_id") Integer university_id);

        @GET ("/info")
        Observable<Statistics> statistics();

        @GET ("/users/me")
        Observable<User.ResponseData> userinfo(
            @Header ("Authorization") String authorization
        );
        @GET("/lectures/dummy_autocomplete")
        Observable<Dummy_lecture.lectures> eAuto(
            @Header("Authorization") String authorization
//          ,@Query("query") String query //unused yet.
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
                @Query("point_overall") Integer score_overall,
                @Query("point_gpa_satisfaction") Integer score_satisfaction,
                @Query("point_easiness") Integer score_easiness,
                @Query("point_clarity") Integer score_lecture_quality,
                @Query("comment") String description
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
