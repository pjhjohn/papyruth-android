package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.response.EvaluationResponse;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep3Fragment extends TrackerFragment {
    private Navigator navigator;
    private Context context;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
        this.context   = activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
        this.context   = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Bind(R.id.evaluation_body_icon) protected ImageView bodyIcon;
    @Bind(R.id.evaluation_body_label) protected TextView bodyLabel;
    @Bind(R.id.evaluation_body_text) protected EditText bodyText;
    @Bind(R.id.evaluation_hashtags_icon) protected ImageView hashtagsIcon;
    @Bind(R.id.evaluation_hashtags_label) protected TextView hashtagsLabel;
    @Bind(R.id.evaluation_hashtags_container) protected TextView hashtagsContainer;
    @Bind(R.id.evaluation_hashtags_text) protected EditText hashtagsText; // TODO : ==> Recipants Android HashtagChips
    @Bind(R.id.evaluation_recommend_hashtag_list) protected LinearLayout recommendHashtag;
//    @Bind(R.id.lecture) protected TextView lecture;
//    @Bind(R.id.professor) protected TextView professor;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        ButterKnife.bind(this, view);
        this.subscriptions = new CompositeSubscription();
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        if(EvaluationForm.getInstance().getBody() != null) {
            this.bodyText.setText(EvaluationForm.getInstance().getBody());
        }
        if(EvaluationForm.getInstance().getHashtag().size() > 0){
            drawHashtag();
        }
//        this.lecture.setText(EvaluationForm.getInstance().getLectureName());
//        this.professor.setText(EvaluationForm.getInstance().getProfessorName());

        /**
         * Get recommended hashtag list from server
         */
        this.subscriptions.add(
            Api.papyruth()
                .get_hashtag_preset(User.getInstance().getAccessToken())
                .map(response -> response.hashtags)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hashtags -> {
                    for (String string : hashtags) {
                        TextView hashtag = (TextView) LayoutInflater.from(getActivity().getBaseContext()).inflate(R.layout.button_hashtag, recommendHashtag, false);
                        hashtag.setText(string);
                        hashtag.setOnClickListener(event -> {
                            addNewHashtag(string);
                        });
                        recommendHashtag.addView(hashtag);
                    }
                }, error -> {
                    ErrorHandler.handle(error, this);
                })
        );

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        final Context context = this.getActivity();
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarHelper.getColorTransitionAnimator(toolbar, R.color.toolbar_green).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_green);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_green);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
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
        }, error->ErrorHandler.handle(error, this));
        Picasso.with(context).load(R.drawable.ic_light_edit).transform(new ColorFilterTransformation(this.context.getResources().getColor(R.color.icon_material))).into(this.bodyIcon);
        Picasso.with(context).load(R.drawable.ic_light_tag).transform(new ColorFilterTransformation(this.context.getResources().getColor(R.color.icon_material))).into(this.hashtagsIcon);
        this.bodyLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | this.bodyLabel.getPaintFlags());
        this.hashtagsLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | this.hashtagsLabel.getPaintFlags());

        this.subscriptions.add(
            WidgetObservable.text(this.bodyText)
                .map(RxValidator.toString)
                .map(body -> {
                    if(EvaluationForm.getInstance().isEditMode())
                        EvaluationForm.getInstance().setEdited(true);
                    EvaluationForm.getInstance().setBody(body);
                    return body;
                })
                .map(RxValidator.isValidEvaluationBody)
                .map(e -> EvaluationForm.getInstance().isCompleted())
                .startWith(EvaluationForm.getInstance().isCompleted())
                .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(valid -> {
                    this.showFAB(valid);
                }, error -> ErrorHandler.handle(error, this))
        );

        /**
         * Type new hashtag.
         */
        this.subscriptions.add(
            WidgetObservable.text(this.hashtagsText)
                .filter(event -> {
                    if (event.text().length() > 0 &&  event.text().charAt(0) == '#')
                        this.recommendHashtag.setVisibility(View.VISIBLE);
                    else
                        this.recommendHashtag.setVisibility(View.INVISIBLE);
                    return event.text().length() > 1 && (event.text().charAt(event.text().length() - 1) == ' ' || event.text().charAt(event.text().length() - 1) == '#');
                })
                .subscribe(event -> {
                    final String str = event.text().subSequence(0, event.text().length() - 1).toString();
                    if (str.equals(" ") || str.equals("#")) {
                        this.hashtagsText.setText("");
                        return;
                    }
                    this.addNewHashtag(str);
                    this.recommendHashtag.setVisibility(View.INVISIBLE);
                    if (event.text().charAt(event.text().length() - 1) == '#') {
                        this.hashtagsText.setText("#");
                        this.hashtagsText.setSelection(hashtagsText.getText().length());
                    } else
                        this.hashtagsText.setText("");
                }, error -> ErrorHandler.handle(error, this))
        );
    }

    private boolean showFAB(boolean valid){
        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        if (visible && !valid){
            FloatingActionControl.getInstance().hide(true);
        }else if (!visible && valid) {
            FloatingActionControl.getInstance().show(true);
            return true;
        }
        return false;
    }

    private boolean addNewHashtag(String text){
        if(EvaluationForm.getInstance().getHashtag().contains(text))
            return false;

        EvaluationForm.getInstance().addHashtag(Hashtag.removeHashPrefix(text));
        drawHashtag();

        if(EvaluationForm.getInstance().isEditMode())
            EvaluationForm.getInstance().setEdited(true);
        this.showFAB(EvaluationForm.getInstance().isCompleted());
        return true;
    }

    private void drawHashtag(){
        hashtagsContainer.setText("");
        hashtagsContainer.setMovementMethod(LinkMovementMethod.getInstance());
        hashtagsContainer.setText(
            Hashtag.getClickableHashtag(
                getActivity(), EvaluationForm.getInstance().getHashtag(), () -> {
                    drawHashtag();
                    return true;
                })
        );
    }

    private Observable<EvaluationResponse> submitEvaluation(boolean isModifyMode){
        if(isModifyMode){
            return Api.papyruth().put_update_evaluation(
                User.getInstance().getAccessToken(),
                EvaluationForm.getInstance().getEvaluationId(),
                EvaluationForm.getInstance().getPointOverall(),
                EvaluationForm.getInstance().getPointGpaSatisfaction(),
                EvaluationForm.getInstance().getPointEasiness(),
                EvaluationForm.getInstance().getPointClarity(),
                EvaluationForm.getInstance().getBody()
            );
        }
        return Api.papyruth().post_evaluation(
            User.getInstance().getAccessToken(),
            EvaluationForm.getInstance().getCourseId(),
            EvaluationForm.getInstance().getPointOverall(),
            EvaluationForm.getInstance().getPointGpaSatisfaction(),
            EvaluationForm.getInstance().getPointEasiness(),
            EvaluationForm.getInstance().getPointClarity(),
            EvaluationForm.getInstance().getBody()
        );
    }

    private void submitNewEvaluation() {
        this.submitEvaluation(EvaluationForm.getInstance().isEditMode())
            .filter(response -> response.success)
            .map(response -> {
                EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                if (EvaluationForm.getInstance().getHashtag().size() > 0) {
                    Api.papyruth().post_evaluation_hashtag(
                        User.getInstance().getAccessToken(),
                        EvaluationForm.getInstance().getEvaluationId(),
                        EvaluationForm.getInstance().getHashtag()
                    ).subscribe();
                }
                return true;
            })
            .flatMap(unused -> Api.papyruth().get_evaluation(
                User.getInstance().getAccessToken(),
                EvaluationForm.getInstance().getEvaluationId()
            ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    if (response.success) {
                        EvaluationForm.getInstance().free();
                        Evaluation.getInstance().update(response.evaluation);
                        updateUserData();
                    } else {
                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_fail), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    ErrorHandler.handle(error, this);
                }
            );
    }

    public void updateUserData(){
        if(User.getInstance().getMandatoryEvaluationCount() < 1){
            Bundle bundle = new Bundle();
            bundle.putBoolean("STANDALONE", true);
            this.navigator.navigate(HomeFragment.class, false, Navigator.AnimatorType.SLIDE_TO_RIGHT, true);
        }else {
            Api.papyruth().users_me(User.getInstance().getAccessToken())
                .map(response -> response.user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(user -> {
                    User.getInstance().update(user);
                    this.navigator.navigate(HomeFragment.class, false, Navigator.AnimatorType.SLIDE_TO_RIGHT, true);
                }, error -> ErrorHandler.handle(error, this));
        }
    }
}