package com.papyruth.support.opensource.retrofit.apis;

import com.papyruth.android.model.response.CandidatesResponse;
import com.papyruth.android.model.response.CommentResponse;
import com.papyruth.android.model.response.CommentsResponse;
import com.papyruth.android.model.response.CourseResponse;
import com.papyruth.android.model.response.CoursesResponse;
import com.papyruth.android.model.response.EvaluationPossibleResponse;
import com.papyruth.android.model.response.EvaluationResponse;
import com.papyruth.android.model.response.EvaluationsResponse;
import com.papyruth.android.model.response.FavoriteCoursesResponse;
import com.papyruth.android.model.response.GlobalInfosResponse;
import com.papyruth.android.model.response.HashtagsResponse;
import com.papyruth.android.model.response.MyCommentsResponse;
import com.papyruth.android.model.response.SignUpValidateResponse;
import com.papyruth.android.model.response.StatisticsResponse;
import com.papyruth.android.model.response.SuccessResponse;
import com.papyruth.android.model.response.TermResponse;
import com.papyruth.android.model.response.TermsResponse;
import com.papyruth.android.model.response.UniversitiesResponse;
import com.papyruth.android.model.response.UniversityResponse;
import com.papyruth.android.model.response.UserDataResponse;
import com.papyruth.android.model.response.VoidResponse;
import com.papyruth.android.model.response.VoteCountResponse;
import com.papyruth.android.model.response.VotersResponse;

import java.util.List;

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
 * Created by pjhjohn on 2015-10-28.
 */
public interface Papyruth {
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
    @POST("/comments") // 403 if failed
    Observable<VoidResponse> post_comment(
        @Header("Authorization") String authorization,
        @Query("evaluation_id") Integer evaluation_id,
        @Query("body") String body
    );
    @DELETE("/comments/{id}") // 401 if not owner
    Observable<VoidResponse> delete_comment(
        @Header("Authorization") String authorization,
        @Path("id") Integer id
    );
    @POST("/comments/{id}/vote")
    Observable<VoteCountResponse> post_comment_vote(
        @Header("Authorization") String authorization,
        @Path("id") Integer id,
        @Query("up") Boolean up
    );
    @DELETE("/comments/{id}/vote")
    Observable<VoteCountResponse> delete_comment_vote(
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
    @POST("/courses/{id}/favorite") // 403 failed to (un)register favorite
    Observable<SuccessResponse> post_course_favorite(
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


    /* EMAIL */
    Integer EMAIL_CONFIRMATION_USER = 0;
    Integer EMAIL_CONFIRMATION_UNIVERSITY = 1;
    @POST("/email/confirm")
    Observable<SuccessResponse> post_email_confirm(
        @Header("Authorization") String authorization,
        @Query("type") Integer confirmation_type
    );
    @POST("/email/password")
    Observable<SuccessResponse> post_email_password(
        @Query("email") String email
    );
    @POST("/email/migrate")
    Observable<SuccessResponse> post_email_migrate(
        @Query("email") String email
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
    @POST("/evaluations/possible")
    Observable<EvaluationPossibleResponse> post_evaluation_possible(
        @Header("Authorization") String authorization,
        @Query("course_id") Integer course_id
    );
    @GET("/evaluations/{id}")
    Observable<EvaluationResponse> get_evaluation(
        @Header("Authorization") String authorization,
        @Path("id") Integer id
    );
    @PATCH("/evaluations/{id}")
    Observable<EvaluationResponse> patch_evaluation(
        @Header("Authorization") String authorization,
        @Path("id") Integer id,
        @Query("point_overall") Integer point_overall,
        @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
        @Query("point_easiness") Integer point_easiness,
        @Query("point_clarity") Integer point_clarity,
        @Query("body") String body
    );
    @PUT("/evaluations/{id}")
    Observable<EvaluationResponse> put_evaluation(
        @Header("Authorization") String authorization,
        @Path("id") Integer id,
        @Query("point_overall") Integer point_overall,
        @Query("point_gpa_satisfaction") Integer point_gpa_satisfaction,
        @Query("point_easiness") Integer point_easiness,
        @Query("point_clarity") Integer point_clarity,
        @Query("body") String body
    );
    @DELETE("/evaluations/{id")
    Observable<SuccessResponse> delete_evaluation(
        @Header("Authorization") String authorization,
        @Path("id") Integer id
    );
    @POST("/evaluations/{id}/vote")
    Observable<VoteCountResponse> post_evaluation_vote(
        @Header("Authorization") String authorization,
        @Path("id") Integer id,
        @Query("up") Boolean up
    );
    @DELETE("/evaluations/{id}/vote")
    Observable<VoteCountResponse> delete_evaluation_vote(
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


    /* GLOBAL INFOS : Doesn't require access-token */
    @GET("/global_infos")
    Observable<GlobalInfosResponse> get_global_infos();


    /* HOME */
    @GET("/info")
    Observable<StatisticsResponse> get_info();

    @GET("/hashtag")
    Observable<HashtagsResponse> get_hashtag(
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
    Observable<CandidatesResponse> get_search_autocomplete(
        @Header("Authorization") String authorization,
        @Query("university_id") Integer university_id,
        @Query("query") String query,
        @Query("page") Integer page
    );
    @GET("/search/search")
    Observable<CoursesResponse> get_search_search(
        @Header("Authorization") String authorization,
        @Query("university_id") Integer university_id,
        @Query("lecture_id") Integer lecture_id,
        @Query("professor_id") Integer professor_id,
        @Query("query") String query,
        @Query("page") Integer page,
        @Query("limit") Integer limit
    );


    /* TERMS */
    @GET("/terms")
    Observable<TermsResponse> get_terms();
    @GET("/terms/{id}")
    Observable<TermResponse> get_terms(
        @Path("id") Integer id
    );


    /* UNIVERSITIES */
    @GET("/universities")
    Observable<UniversitiesResponse> get_universities();
    @GET("/universities/{id}")
    Observable<UniversityResponse> get_universities(
        @Header("Authorization") String authorization,
        @Path("id") Integer id
    );


    /* USERS */
    @POST("/users/sign_up")
    Observable<UserDataResponse> post_users_sign_up(
        @Query("email") String email,
        @Query("password") String password,
        @Query("realname") String realname,
        @Query("nickname") String nickname,
        @Query("is_boy") Boolean is_boy,
        @Query("university_id") Integer university_id,
        @Query("entrance_year") Integer entrance_year
    );
    @POST("/users/sign_up/validate") // 400 if attribute not found by name
    Observable<SignUpValidateResponse> post_users_sign_up_validate(
        @Query("name") String name,
        @Query("value") String value
    );
    @POST("/users/sign_in") // 403 if account not found or password not matched
    Observable<UserDataResponse> post_users_sign_in(
        @Query("email") String email,
        @Query("password") String password
    );
    @POST("/users/sign_out")
    Observable<VoidResponse> post_users_sign_out(
        @Header("Authorization") String authorization
    );
    @POST("/users/refresh_token")
    Observable<UserDataResponse> post_users_refresh_token(
        @Header("Authorization") String authorization
    );
    @GET("/users/me")
    Observable<UserDataResponse> get_users_me(
        @Header("Authorization") String authorization
    );
    @POST("/users/me/passwd") // 400 if failed to change password
    Observable<SuccessResponse> post_users_me_passwd(
        @Header("Authorization") String authorization,
        @Query("old_password") String old_password,
        @Query("new_password") String new_password
    );
    @POST("/users/me/edit") // 400 if failed to edit user profile
    Observable<UserDataResponse> post_users_me_edit_nickname(
        @Header("Authorization") String authorization,
        @Query("nickname") String nickname
    );
    @GET("/users/me/favorites")
    Observable<FavoriteCoursesResponse> get_users_me_favorites(
        @Header("Authorization") String authorization,
        @Query("page") Integer page
    );
    @GET("/users/me/evaluations")
    Observable<EvaluationsResponse> get_users_me_evaluations(
        @Header("Authorization") String authorization,
        @Query("page") Integer page
    );
    @GET("/users/me/comments")
    Observable<MyCommentsResponse> get_users_me_comments(
        @Header("Authorization") String authorization,
        @Query("page") Integer page
    );
    @POST("/users/me/university_email")
    Observable<SuccessResponse> post_users_me_university_email(
        @Header("Authorization") String authorization,
        @Query("email") String university_email
    );
}
