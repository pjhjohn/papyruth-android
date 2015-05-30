package com.montserrat.utils.support.retrofit;

import com.montserrat.app.model.Comment;
import com.montserrat.app.model.response.CandidatesResponse;
import com.montserrat.app.model.response.EvaluationResponse;
import com.montserrat.app.model.response.PartialCoursesResponse;
import com.montserrat.app.model.response.PartialEvaluationsResponse;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.response.UniversitiesResponse;
import com.montserrat.app.model.response.UserInfoResponse;

import retrofit.RestAdapter;
import retrofit.http.DELETE;
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
        Observable<PartialEvaluationsResponse> evaluations(
            @Header ("Authorization") String authorization,
            @Query ("university_id") Integer university_id,
            @Query ("since_id") Integer since_id,
            @Query ("max_id") Integer max_id,
            @Query ("limit") Integer limit,
            @Query ("course_id") Integer course_id
        );

        @GET ("/lectures")
        Observable<PartialCoursesResponse> lectures(
            @Header ("Authorization") String authorization,
            @Query ("university_id") Integer university_id,
            @Query ("query") String query
        );

        @GET ("/search/autocomplete")
        Observable<CandidatesResponse> search_autocomplete(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("query") String query
        );

        @GET ("/search/search")
        Observable<PartialCoursesResponse> search(
                @Query("university_id") Integer university_id,
                @Query("lecture_id") Integer lecture_id,
                @Query("professor_id") Integer professor_id,
                @Query("query") String query
        );

        @GET ("/universities")
        Observable<UniversitiesResponse> universities();

        @GET ("/universities/{univ_id}")
        Observable<StatisticsResponse> statistics(@Header ("Authorization") String authorization,@Path ("univ_id") Integer university_id);

        @GET ("/info")
        Observable<StatisticsResponse> statistics();

        @GET ("/comments")
        Observable<Comment> comments(
                @Query("evaluation_id") Integer evaluation_id,
                @Query("page") Integer page,
                @Query("limit") Integer limit
        );

        @POST ("/comments")
        Observable<Comment> comments(
                @Header ("Authorization") String authorization,
                @Query("evaluation_id") Integer evaluation_id,
                @Query("body") String body
        );

        @DELETE ("/comments/{id}")
        Observable<Comment> comments(
                @Header ("Authorization") String authorization,
                @Path("id") Integer comment_id
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

        @POST("/users/sign_in")
        Observable<UserInfoResponse> user_sign_in(
            @Query("email") String email,
            @Query("password") String password
        );

        @GET ("/users/me")
        Observable<UserInfoResponse> user_me(
            @Header("Authorization") String authorization
        );

        @POST ("/users/sign_up")
        Observable<UserInfoResponse> user_sign_up(
            @Query("email") String email,
            @Query("password") String password,
            @Query("realname") String realname,
            @Query("nickname") String nickname,
            @Query("is_boy") Boolean is_boy,
            @Query("university_id") Integer university_id,
            @Query("entrance_year") Integer entrance_year
        );

        @POST ("/users/update")
        Observable<UserInfoResponse> user_update(
            @Header("Authorization") String authorization,
            @Query("realname") String realname,
            @Query("nickname") String nickname,
            @Query("is_boy") Boolean is_boy
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
