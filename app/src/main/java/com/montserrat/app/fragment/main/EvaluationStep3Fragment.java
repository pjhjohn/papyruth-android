package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.Evaluation;
import com.montserrat.app.model.User;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.request.RxVolley;
import com.montserrat.utils.viewpager.ViewPagerController;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class EvaluationStep3Fragment extends Fragment {
    private ViewPagerController pagerController;
    private CompositeSubscription subscriptions;


    @InjectView(R.id.autotext_lecture) protected EditText vLecture;
    @InjectView(R.id.autotext_professor) protected EditText vProfessor;
    @InjectView(R.id.score_overall) protected RatingBar vScoreOverall;
    @InjectView(R.id.score_satisfaction) protected SeekBar vScoreSatisfaction;
    @InjectView(R.id.score_easiness) protected SeekBar vScoreEasiness;
    @InjectView(R.id.score_lecture_quality) protected SeekBar vScoreLectureQuality;
    @InjectView(R.id.description) protected EditText vDescription;
    private Evaluation eval;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        getFragmentManager().beginTransaction().add(this, AppConst.Tag.Evaluation.EVALUATION_STEP3);
        ButterKnife.inject(this, view);
        vLecture.setEnabled(false);
        vProfessor.setEnabled(false);
        vScoreOverall.setEnabled(false);
        vScoreSatisfaction.setEnabled(false);
        vScoreEasiness.setEnabled(false);
        vScoreLectureQuality.setEnabled(false);

        vScoreOverall.setStepSize((float)0.5);
        vScoreSatisfaction.setMax(5);
        vScoreEasiness.setMax(5);
        vScoreLectureQuality.setMax(5);

        JSONObject params = new JSONObject();
        try {
            params.put("course_id", 0)
                  .put("score_overall", 0)
                  .put("score_satisfaction", 0)
                  .put("score_easiness", 0)
                  .put("score_lecture_quality", 0)
                  .put("description", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        subscriptions.add(ViewObservable
            .clicks(view.findViewById(R.id.submit))
            .flatMap(unused -> RxVolley.createObservable(Api.url("evaluations"), Request.Method.POST, User.getInstance().getAccessToken(), params))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> Timber.d("response : %s", response),
                Throwable::printStackTrace
            )
        );
        return view;
    }
    public void update(String lecture, String professor, float ovarall, int satisfaction, int easiness, int quality){
        vLecture.setText(lecture);
        vProfessor.setText(professor);
        vScoreOverall.setRating(ovarall);
        vScoreSatisfaction.setProgress(satisfaction);
        vScoreEasiness.setProgress(easiness);
        vScoreLectureQuality.setProgress(quality);
    }
}
