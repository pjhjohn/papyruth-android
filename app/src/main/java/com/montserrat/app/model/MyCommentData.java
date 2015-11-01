package com.montserrat.app.model;

import android.content.Context;

import com.montserrat.app.R;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.apis.Api;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by SSS on 2015-05-22.
 */
public class MyCommentData{
    public Integer id;
    public Integer evaluation_id;
    public Integer user_id;
    public String body;
    public String created_at;
    public String updated_at;
    public String user_nickname;
    public Integer up_vote_count;
    public Integer down_vote_count;
    public Integer request_user_vote; // 1 for up-vote, 0 for down-vote, null for neither.
    public String avatar_url;

    public String lecture_name;
    public String professor_name;
    public String category;

    private CompositeSubscription subscription;

    public rx.Observable<com.montserrat.app.model.response.EvaluationResponse> setCourseData(Context context){
        return Api.papyruth().get_evaluation(User.getInstance().getAccessToken(), evaluation_id)
            .doOnNext(response -> {
                this.lecture_name = response.evaluation.lecture_name;
                this.professor_name = response.evaluation.professor_name;
                this.category = context.getResources().getString(R.string.category_major);
            });
    }
}
