package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.response.EvaluationResponse;
import com.papyruth.android.model.response.VoidResponse;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.customview.Hashtag;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 */

public class EvaluationStep3Fragment extends Fragment {
    private Navigator mNavigator;
    private Unbinder mUnbinder;
    private MainActivity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mActivity = (MainActivity) activity;
    }

    @BindView(R.id.evaluation_hashtags_icon)            protected ImageView mHashtagsIcon;
    @BindView(R.id.evaluation_hashtags_label)           protected TextView mHashtagsLabel;
    @BindView(R.id.evaluation_hashtags_container)       protected TextView mHashtagsContainer;
    @BindView(R.id.evaluation_hashtags_text)            protected AutoCompleteTextView mHashtagsText;
    @BindView(R.id.evaluation_body_icon)                protected ImageView mBodyIcon;
    @BindView(R.id.evaluation_body_label)               protected TextView mBodyLabel;
    @BindView(R.id.evaluation_body)                     protected EditText mBody;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;
    private List<String> mHashtagPresetData;
    private ArrayAdapter<String> mHashtagPresetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step3, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        if(EvaluationForm.getInstance().getBody() != null) mBody.setText(EvaluationForm.getInstance().getBody());
        if(EvaluationForm.getInstance().getHashtag().size() > 0) renderHashtag();
        mHashtagsText.setAdapter(mHashtagPresetAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_dropdown_item_1line, mHashtagPresetData = new ArrayList<>()));
        mHashtagsText.setThreshold(1);
        Api.papyruth()
            .get_hashtag(User.getInstance().getAccessToken())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    mHashtagPresetData.clear();
                    for (String hashtag : response.hashtags) mHashtagPresetData.add(hashtag);
                    mHashtagPresetAdapter.notifyDataSetChanged();
                }, error -> ErrorHandler.handle(error, this, true)
            );
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_compose_evaluation);
        mCompositeSubscription.clear();
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        StatusBarHelper.changeColorTo(mActivity, R.color.status_bar_green);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_green);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        mCompositeSubscription.add(
            FloatingActionControl.clicks().observeOn(AndroidSchedulers.mainThread()).subscribe(
                unused -> new MaterialDialog.Builder(mActivity)
                    .title(R.string.dialog_title_compose_evaluation_submit)
                    .content(R.string.dialog_content_compose_evaluation_submit)
                    .positiveText(R.string.dialog_positive_submit)
                    .negativeText(R.string.dialog_negative_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            if(EvaluationForm.getInstance().isCompleted()) submitNewEvaluation();
                            else Toast.makeText(mActivity, R.string.toast_compose_evaluation_incomplete_data, Toast.LENGTH_SHORT).show();
                        }
                    }).show(),
                error -> ErrorHandler.handle(error, this)
            )
        );

        Picasso.with(mActivity).load(R.drawable.ic_hashtag_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mHashtagsIcon);
        mHashtagsLabel.setText(R.string.compose_evaluation_label_hashtags);
        Picasso.with(mActivity).load(R.drawable.ic_new_evaluation_24dp).transform(new ColorFilterTransformation(mActivity.getResources().getColor(R.color.icon_material))).into(mBodyIcon);
        mBodyLabel.setText(R.string.compose_evaluation_label_body);
        mBodyLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | mBodyLabel.getPaintFlags());
        mHashtagsLabel.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG | mHashtagsLabel.getPaintFlags());
        mCompositeSubscription.add(RxTextView.textChanges(mBody)
            .map(CharSequence::toString)
            .map(body -> {
                if(EvaluationForm.getInstance().isEditMode())
                    EvaluationForm.getInstance().setEdited(true);
                EvaluationForm.getInstance().setBody(body);
                return body;
            })
            .map(RxValidator.isValidEvaluationBody)
            .map(valid -> EvaluationForm.getInstance().isCompleted())
            .subscribe(this::showFAB, error -> ErrorHandler.handle(error, this))
        );
        if(EvaluationForm.getInstance().isCompleted()) showFAB(true);

        /* Type new hashtag. */
        mCompositeSubscription.add(RxTextView.textChanges(mHashtagsText)
            .map(CharSequence::toString)
            .filter(s -> s.length() > 1 && (s.charAt(s.length() - 1) == ' ' || s.charAt(s.length() - 1) == '#'))
            .subscribe(s -> {
                final String str = s.subSequence(0, s.length() - 1).toString();
                if(str.equals(" ") || str.equals("#")) {
                    mHashtagsText.setText("");
                    return;
                }
                addNewHashtag(str);
                if(s.charAt(s.length() - 1) == '#') {
                    mHashtagsText.setText("#");
                    mHashtagsText.setSelection(mHashtagsText.getText().length());
                } else mHashtagsText.setText("");
            }, error -> ErrorHandler.handle(error, this))
        );
    }

    private void showFAB(boolean valid) {
        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        if(visible && valid) FloatingActionControl.getInstance().show(false);
        else if (visible) FloatingActionControl.getInstance().hide(true);
        else if (valid) FloatingActionControl.getInstance().show(true);
    }

    private boolean addNewHashtag(String text) {
        if(EvaluationForm.getInstance().getHashtag().contains(Hashtag.removePrefix(text))) return false;
        EvaluationForm.getInstance().addHashtag(Hashtag.removePrefix(text));
        renderHashtag();
        if(EvaluationForm.getInstance().isEditMode()) EvaluationForm.getInstance().setEdited(true);
        showFAB(EvaluationForm.getInstance().isCompleted());
        return true;
    }

    private Void renderHashtag() {
        final List<String> hashtags = EvaluationForm.getInstance().getHashtag();
        if(hashtags.isEmpty()) mHashtagsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        else mHashtagsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mHashtagsContainer.setText("");
        mHashtagsContainer.setMovementMethod(LinkMovementMethod.getInstance());
        mHashtagsContainer.setText(Hashtag.clickableSpannableString(mActivity, hashtags, this::renderHashtag));
        return null;
    }

    private void submitNewEvaluation() {
        getApiObservable(EvaluationForm.getInstance().isEditMode())
            .filter(response -> response.success)
            .flatMap(response -> {
                if(response.evaluation_id != null) EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                if(EvaluationForm.getInstance().getHashtag().isEmpty()) return Observable.just(new VoidResponse());
                FloatingActionControl.getButton().setIndeterminate(true);
                return Api.papyruth().post_evaluation_hashtag(
                    User.getInstance().getAccessToken(),
                    EvaluationForm.getInstance().getEvaluationId(),
                    EvaluationForm.getInstance().getHashtag()
                );
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    Evaluation.getInstance().clear();
                    Evaluation.getInstance().setId(EvaluationForm.getInstance().getEvaluationId());
                    EvaluationForm.getInstance().clear();
                    updateUserData();
                },
                error -> {
                    Toast.makeText(mActivity, this.getResources().getString(R.string.toast_compose_evaluation_submission_failed), Toast.LENGTH_SHORT).show();
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    ErrorHandler.handle(error, this);
                }
            );
    }

    private Observable<EvaluationResponse> getApiObservable(boolean isEditMode) {
        FloatingActionControl.getButton().setIndeterminate(true);
        if(isEditMode) return Api.papyruth().put_evaluation(
            User.getInstance().getAccessToken(),
            EvaluationForm.getInstance().getEvaluationId(),
            EvaluationForm.getInstance().getPointOverall(),
            EvaluationForm.getInstance().getPointGpaSatisfaction(),
            EvaluationForm.getInstance().getPointEasiness(),
            EvaluationForm.getInstance().getPointClarity(),
            EvaluationForm.getInstance().getBody()
        );
        else return Api.papyruth().post_evaluation(
            User.getInstance().getAccessToken(),
            EvaluationForm.getInstance().getCourseId(),
            EvaluationForm.getInstance().getPointOverall(),
            EvaluationForm.getInstance().getPointGpaSatisfaction(),
            EvaluationForm.getInstance().getPointEasiness(),
            EvaluationForm.getInstance().getPointClarity(),
            EvaluationForm.getInstance().getBody()
        );
    }

    private void updateUserData() {
        if(User.getInstance().getMandatoryEvaluationCount() > 0) Api.papyruth().get_users_me(User.getInstance().getAccessToken())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(response -> {
                FloatingActionControl.getButton().setIndeterminate(false);
                FloatingActionControl.getButton().setProgress(0, true);
                User.getInstance().update(response.user);
                mNavigator.navigate(HomeFragment.class, true, true);
            }, error -> {
                FloatingActionControl.getButton().setIndeterminate(false);
                FloatingActionControl.getButton().setProgress(0, true);
                ErrorHandler.handle(error, this, true);
            });
        else {
            mNavigator.navigate(HomeFragment.class, true, true);
        }
    }
}
