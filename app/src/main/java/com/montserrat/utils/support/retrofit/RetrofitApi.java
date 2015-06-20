package com.montserrat.utils.support.retrofit;

import com.montserrat.app.model.response.CandidatesResponse;
import com.montserrat.app.model.response.CommentResponse;
import com.montserrat.app.model.response.CourseResponse;
import com.montserrat.app.model.response.EvaluationResponse;
import com.montserrat.app.model.response.SimpleCoursesResponse;
import com.montserrat.app.model.response.SimpleEvaluationsResponse;
import com.montserrat.app.model.response.SimpleResponse;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.response.UniversitiesResponse;
import com.montserrat.app.model.response.UserInfoResponse;

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
        @GET("/comments")
        Observable<CommentResponse> comments(
            @Header("Authorization") String authorization,
            @Query("evaluation_id") Integer evaluation_id,
            @Query("page") Integer page,
            @Query("limit") Integer limit
        );

        @POST("/comments")
        Observable<CommentResponse> comments(
            @Header("Authorization") String authorization,
            @Query("evaluation_id") Integer evaluation_id,
            @Query("body") String body
        );

        @GET("/courses")
        Observable<CourseResponse> courses(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("recommend") Boolean recommend
        );

        @GET("/courses/{id}")
        Observable<CourseResponse> course(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        @GET("/courses/{id}")
        Observable<CourseResponse> course_favorite(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("favorite") Boolean favorite
        );

        @GET("/evaluations")
        Observable<SimpleEvaluationsResponse> evaluations(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("since_id") Integer since_id,
            @Query("max_id") Integer max_id,
            @Query("limit") Integer limit,
            @Query("course_id") Integer course_id
        );

        @POST("/evaluations")
        Observable<EvaluationResponse> evaluation(
            @Header("Authorization") String authorization,
            @Query("course_id") Integer course_id,
            @Query("point_overall") Integer point_overall,
            @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
            @Query("point_easiness") Integer point_easiness,
            @Query("point_clarity") Integer point_clarity,
            @Query("body") String comment
        );

        @GET("/evaluations/{id}")
        Observable<EvaluationResponse> evaluation(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        @GET("/info")
        Observable<StatisticsResponse> info();

        @GET("/search/autocomplete")
        Observable<CandidatesResponse> search_autocomplete(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("query") String query
        );

        @GET("/search/search")
        Observable<SimpleCoursesResponse> search_search(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("lecture_id") Integer lecture_id,
            @Query("professor_id") Integer professor_id,
            @Query("query") String query
        );

        @GET("/universities")
        Observable<UniversitiesResponse> universities();

        @GET("/universities/{id}")
        Observable<StatisticsResponse> universities(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        @POST("/users/sign_up")
        Observable<UserInfoResponse> users_sign_up(
            @Query("email") String email,
            @Query("password") String password,
            @Query("realname") String realname,
            @Query("nickname") String nickname,
            @Query("is_boy") Boolean is_boy,
            @Query("university_id") Integer university_id,
            @Query("entrance_year") Integer entrance_year
        );

        @POST("/users/sign_in")
        Observable<UserInfoResponse> users_sign_in(
            @Query("email") String email,
            @Query("password") String password
        );

        @GET("/users/me")
        Observable<UserInfoResponse> users_me(
            @Header("Authorization") String authorization
        );

        @POST("/users/me/passwd")
        Observable<SimpleResponse> users_me_passwd(
            @Header("Authorization") String authorization,
            @Query("old_password") String old_password,
            @Query("new_password") String new_password
        );

        @POST("/users/me/edit")
        Observable<UserInfoResponse> users_me_edit(
            @Header("Authorization") String authorization,
            @Query("email") String email,
            @Query("nickname") String nickname,
            @Query("realname") String realname,
            @Query("is_boy") Boolean is_boy
        );

        @GET("/users/me/favorites")
        Observable<SimpleCoursesResponse> users_me_favorites(
            @Header("Authorization") String authorization,
            @Query("page") Integer page
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
