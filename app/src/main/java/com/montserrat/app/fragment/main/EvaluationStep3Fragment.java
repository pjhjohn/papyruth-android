package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.response.VoidResponse;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.materialdialog.HashtagDeleteDialog;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep3Fragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
    }

    @InjectView(R.id.evaluation_body_icon) protected ImageView bodyIcon;
    @InjectView(R.id.evaluation_body_label) protected TextView bodyLabel;
    @InjectView(R.id.evaluation_body_text) protected EditText bodyText;
    @InjectView(R.id.evaluation_hashtags_icon) protected ImageView hashtagsIcon;
    @InjectView(R.id.evaluation_hashtags_label) protected TextView hashtagsLabel;
    @InjectView(R.id.evaluation_hashtags_container) protected LinearLayout hashtagsContainer;
    @InjectView(R.id.evaluation_hashtags_text) protected EditText hashtagsText; // TODO : ==> Recipants Android HashtagChips
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_EASINESS).start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = this.getActivity();
        if(EvaluationForm.getInstance().getBody() != null) {
            this.bodyText.setText(EvaluationForm.getInstance().getBody());
        }
        if(EvaluationForm.getInstance().getHashtag().size() > 0){
            for(String str : EvaluationForm.getInstance().getHashtag()) {
                TextView hashtag = (TextView) LayoutInflater.from(context).inflate(R.layout.button_hashtag, hashtagsContainer, false);
                hashtag.setText(str);
                hashtag.setOnClickListener(view -> HashtagDeleteDialog.show(context, hashtagsContainer, hashtag));
                hashtagsContainer.addView(hashtag);
            }
        }
        Timber.d("on Loading %s", hashtagsContainer.getChildCount());
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);
        FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> {
            new MaterialDialog.Builder(context)
                .title(R.string.new_evaluation_submit_title)
                .content(R.string.new_evaluation_submit_content)
                .positiveText(R.string.confirm_positive)
                .negativeText(R.string.confirm_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (EvaluationForm.getInstance().isCompleted()) submitNewEvaluation();
                        else {
                            Toast.makeText(context, R.string.new_evaluation_incomplete, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
        });
        Picasso.with(context).load(R.drawable.ic_light_edit).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.bodyIcon);
        Picasso.with(context).load(R.drawable.ic_light_tag).transform(new ColorFilterTransformation(AppConst.COLOR_NEUTRAL)).into(this.hashtagsIcon);
        this.bodyLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | this.bodyLabel.getPaintFlags());
        this.hashtagsLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | this.hashtagsLabel.getPaintFlags());

        this.subscriptions.add(
            WidgetObservable.text(this.bodyText)
                .map(RxValidator.toString)
                .map(body -> {
                    EvaluationForm.getInstance().setBody(body);
                    return body;
                })
                .map(RxValidator.isValidEvaluationBody)
                .map(e -> {
                        Timber.d("EvaluationForm : %s", EvaluationForm.getInstance());
                        return EvaluationForm.getInstance().isCompleted();
                    })
                .startWith(EvaluationForm.getInstance().isCompleted())
                .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(valid -> {
                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                    if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                    else if (!visible && valid) FloatingActionControl.getInstance().show(true);
                }, error -> Timber.d("error : %s", error))
        );

        this.subscriptions.add(WidgetObservable.text(this.hashtagsText)
            .filter(event -> event.text().length() > 0 && event.text().charAt(event.text().length() - 1) == ' ')
            .subscribe(event -> {
                final String str = event.text().subSequence(0, event.text().length() - 1).toString();
                TextView hashtag = (TextView) LayoutInflater.from(context).inflate(R.layout.button_hashtag, hashtagsContainer, false);
                hashtag.setText(str);
                hashtag.setOnClickListener(view -> HashtagDeleteDialog.show(context, hashtagsContainer, hashtag));
                EvaluationForm.getInstance().addHashtag(str);
                hashtagsContainer.addView(hashtag);
                this.hashtagsText.setText("");
            })
        );
    }

    private Integer evaluationID = null;
    private void submitNewEvaluation() {
        RetrofitApi.getInstance().post_evaluation(
            User.getInstance().getAccessToken(),
            EvaluationForm.getInstance().getCourseId(),
            EvaluationForm.getInstance().getPointOverall(),
            EvaluationForm.getInstance().getPointGpaSatisfaction(),
            EvaluationForm.getInstance().getPointEasiness(),
            EvaluationForm.getInstance().getPointClarity(),
            EvaluationForm.getInstance().getBody()
        )
        .filter(response -> response.success)
        .map(response -> {
            evaluationID = response.evaluation_id;
            if (hashtagsContainer.getChildCount() > 0) {
                RetrofitApi.getInstance().post_evaluation_hashtag(
                    User.getInstance().getAccessToken(),
                    evaluationID,
                    EvaluationForm.getInstance().getHashtag()
                ).subscribe();
            }
            return true;
        })
        .flatMap(unused -> RetrofitApi.getInstance().get_evaluation(
            User.getInstance().getAccessToken(),
            evaluationID
        ))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            response -> {
                if (response.success) {
                    EvaluationForm.getInstance().free();
                    Evaluation.getInstance().update(response.evaluation);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("STANDALONE", true);
                    this.navigator.back();
                    this.navigator.navigate(EvaluationFragment.class, bundle, false, Navigator.AnimatorType.SLIDE_TO_RIGHT);
                } else {
                    Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_fail), Toast.LENGTH_LONG).show();
                }
            },
            error -> {
                if (error instanceof RetrofitError) {
                    switch (((RetrofitError) error).getResponse().getStatus()) {
                        default:
                            Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                    }
                }else{
                    error.printStackTrace();
                }
            }
        );
    }
}