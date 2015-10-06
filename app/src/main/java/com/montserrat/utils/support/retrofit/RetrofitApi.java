package com.montserrat.utils.support.retrofit;

import com.montserrat.app.model.response.CandidatesResponse;
import com.montserrat.app.model.response.CommentResponse;
import com.montserrat.app.model.response.CommentsResponse;
import com.montserrat.app.model.response.CourseResponse;
import com.montserrat.app.model.response.CoursesResponse;
import com.montserrat.app.model.response.EvaluationResponse;
import com.montserrat.app.model.response.EvaluationsResponse;
import com.montserrat.app.model.response.FavoriteCoursesResponse;
import com.montserrat.app.model.response.HashtagsResponse;
import com.montserrat.app.model.response.MyWrittenResponse;
import com.montserrat.app.model.response.SignUpValidateResponse;
import com.montserrat.app.model.response.SimpleResponse;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.response.TermResponse;
import com.montserrat.app.model.response.UniversitiesResponse;
import com.montserrat.app.model.response.UserDataResponse;
import com.montserrat.app.model.response.VoidResponse;
import com.montserrat.app.model.response.VoteResponse;
import com.montserrat.app.model.response.VotersResponse;
import com.montserrat.app.model.unique.User;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
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
        /* COMMENTS */
        @GET("/comments")
        Observable<CommentsResponse> get_comments(
            @Header("Authorization") String authorization,
            @Query("evaluation_id") Integer evaluation_id,
            @Query("since_id") Integer since_id,
            @Query("max_id") Integer max_id,
            @Query("limit") Integer limit
        );
        @GET("/comments/{id}")
        Observable<CommentResponse> get_comment(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @POST("/comments")
        Observable<VoidResponse> post_comment(
            @Header("Authorization") String authorization,
            @Query("evaluation_id") Integer evaluation_id,
            @Query("body") String body
        );
        @DELETE("/comments/{id}")
        Observable<VoidResponse> delete_comment(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @POST("/comments/{id}/vote")
        Observable<VoteResponse> post_comment_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("up") Boolean up
        );
        @DELETE("/comments/{id}/vote")
        Observable<VoteResponse> delete_comment_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @GET("/comments/{id}/vote")
        Observable<VotersResponse> get_comment_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        /* COURSES */
        @GET("/courses")
        Observable<CoursesResponse> get_courses(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("page") Integer page,
            @Query("limit") Integer limit,
            @Query("recommend") Boolean recommend
        );
        @GET("/courses/{id}")
        Observable<CourseResponse> get_course(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @POST("/courses/{id}/favorite")
        Observable<SimpleResponse> post_course_favorite(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("favorite") Boolean favorite
        );
        @POST("/courses/random")
        Observable<CoursesResponse> get_course_random(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("number") Integer number
        );

        /* EVALUATIONS */
        @GET("/evaluations")
        Observable<EvaluationsResponse> get_evaluations(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("since_id") Integer since_id,
            @Query("max_id") Integer max_id,
            @Query("limit") Integer limit,
            @Query("course_id") Integer course_id
        );
        @POST("/evaluations")
        Observable<EvaluationResponse> post_evaluation(
            @Header("Authorization") String authorization,
            @Query("course_id") Integer course_id,
            @Query("point_overall") Integer point_overall,
            @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
            @Query("point_easiness") Integer point_easiness,
            @Query("point_clarity") Integer point_clarity,
            @Query("body") String body
        );
        @GET("/evaluations/{id}")
        Observable<EvaluationResponse> get_evaluation(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @PATCH("/evaluations/{id}")
        Observable<EvaluationResponse> patch_update_evaluation(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("point_overall") Integer point_overall,
            @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
            @Query("point_easiness") Integer point_easiness,
            @Query("point_clarity") Integer point_clarity,
            @Query("body") String body
        );
        @PUT("/evaluations/{id}")
        Observable<EvaluationResponse> put_update_evaluation(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("point_overall") Integer point_overall,
            @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
            @Query("point_easiness") Integer point_easiness,
            @Query("point_clarity") Integer point_clarity,
            @Query("body") String body
        );
        @DELETE("/evaluations/{id")
        Observable<SimpleResponse> delete_evaluation(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @POST("/evaluations/{id}/vote")
        Observable<VoteResponse> post_evaluation_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("up") Boolean up
        );
        @DELETE("/evaluations/{id}/vote")
        Observable<VoteResponse> delete_evaluation_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @GET("/evaluations/{id}/vote")
        Observable<VotersResponse> get_evaluation_vote(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );
        @POST("/evaluations/{id}/hashtag")
        Observable<VoidResponse> post_evaluation_hashtag(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("hashtags[]") List<String> hashtags
        );
        @POST("/evaluations/{id}/hashtag/delete")
        Observable<VoidResponse> delete_evaluation_hashtag(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("hashtags[]") List<String> hashtags
        );
        @GET("/evaluations/{id}/hashtag")
        Observable<HashtagsResponse> get_evaluation_hashtag(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        /* HOME */
        @GET("/info")
        Observable<StatisticsResponse> get_info();

        @GET("/hashtag")
        Observable<HashtagsResponse> get_hashtag_preset(
            @Header("Authorization") String authorization
        );

        /* PROFESSORS */
        @PATCH("/professors/{id}")
        Observable<VoidResponse> patch_professor(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("photo_url") String photo_url
        );
        @PUT("/professors/{id}")
        Observable<VoidResponse> put_professor(
            @Header("Authorization") String authorization,
            @Path("id") Integer id,
            @Query("photo_url") String photo_url
        );

        /* SEARCH */
        @GET("/search/autocomplete")
        Observable<CandidatesResponse> search_autocomplete(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("query") String query
        );
        @GET("/search/search")
        Observable<CoursesResponse> search_search(
            @Header("Authorization") String authorization,
            @Query("university_id") Integer university_id,
            @Query("lecture_id") Integer lecture_id,
            @Query("professor_id") Integer professor_id,
            @Query("query") String query
        );

        /* TERMS */
        @GET("/terms/{id}")
        Observable<TermResponse> terms(
            @Path("id") Integer id
        );

        /* UNIVERSITIES */
        @GET("/universities")
        Observable<UniversitiesResponse> universities();
        @GET("/universities/{id}")
        Observable<StatisticsResponse> universities(
            @Header("Authorization") String authorization,
            @Path("id") Integer id
        );

        /* USERS */
        @POST("/users/sign_up")
        Observable<UserDataResponse> users_sign_up(
            @Query("email") String email,
            @Query("password") String password,
            @Query("realname") String realname,
            @Query("nickname") String nickname,
            @Query("is_boy") Boolean is_boy,
            @Query("university_id") Integer university_id,
            @Query("entrance_year") Integer entrance_year
        );
        @POST("/users/sign_in")
        Observable<UserDataResponse> users_sign_in(
            @Query("email") String email,
            @Query("password") String password
        );
        @GET("/users/me")
        Observable<UserDataResponse> users_me(
            @Header("Authorization") String authorization
        );
        @POST("/users/me/passwd")
        Observable<SimpleResponse> users_me_passwd(
            @Header("Authorization") String authorization,
            @Query("old_password") String old_password,
            @Query("new_password") String new_password
        );
        @POST("/users/me/edit")
        Observable<UserDataResponse> users_me_edit(
            @Header("Authorization") String authorization,
            @Query("email") String email,
            @Query("nickname") String nickname,
            @Query("realname") String realname,
            @Query("is_boy") Boolean is_boy
        );
        @GET("/users/me/favorites")
        Observable<FavoriteCoursesResponse> users_me_favorites(
            @Header("Authorization") String authorization,
            @Query("page") Integer page
        );
        @GET("/users/me/evaluations")
        Observable<MyWrittenResponse> users_me_evaluations(
            @Header("Authorization") String authorization,
            @Query("page") Integer page
        );
        @GET("/users/me/comments")
        Observable<MyWrittenResponse> users_me_comments(
            @Header("Authorization") String authorization,
            @Query("page") Integer page
        );
        @POST("/users/refresh_token")
        Observable<UserDataResponse> refresh_token(
            @Header("Authorization") String authorization
        );
        @POST("/users/sign_up/validate")
        Observable<SignUpValidateResponse> validate(
            @Query("name") String name,
            @Query("value") String value
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
