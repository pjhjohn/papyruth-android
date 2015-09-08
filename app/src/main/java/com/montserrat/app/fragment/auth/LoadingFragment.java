package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.UniversityData;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class LoadingFragment extends Fragment implements OnPageFocus {
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    @InjectView (R.id.loading_university_text) protected TextView vUnivText;
    @InjectView (R.id.loading_users_text) protected TextView vUserText;
    @InjectView (R.id.loading_evaluations_text) protected TextView vEvalText;
    @InjectView (R.id.loading_text_end_word) protected TextView vLoadingEndWord;
    @InjectView (R.id.loading_university_image) protected ImageView vUnivIcon;
    @InjectView (R.id.loading_users_image) protected ImageView vUserIcon;
    @InjectView (R.id.loading_evaluations_image) protected ImageView vEvalIcon;
    private CompositeSubscription subscriptions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    private boolean timerFinished = false, responseActionFinished = false;
    private Boolean validAuthorization = null;

    @Override
    public void onResume() {
        super.onResume();
        if(this.getUserVisibleHint()) onPageFocused();
    }

    @Override
    public void onPageFocused () {
        ((AuthActivity)this.getActivity()).signUpStep(5);
        User.getInstance().setAccessToken(AppManager.getInstance().getString(AppConst.Preference.ACCESS_TOKEN, null));
        this.actionWithStatistics.call(this.getCacheStatistics());

        this.subscriptions.add(RetrofitApi.getInstance().users_me(User.getInstance().getAccessToken()).subscribe(
            response -> {
                User.getInstance().update(response.user);
                this.subscriptions.add(
                    RetrofitApi.getInstance().universities(User.getInstance().getAccessToken(), User.getInstance().getUniversityId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.actionWithStatistics)
                );
            },
            error -> {
                if (error instanceof RetrofitError) {
                    switch (((RetrofitError) error).getResponse().getStatus()) {
                        case 401:
                        case 419:
                            this.subscriptions.add(
                                RetrofitApi.getInstance().get_info()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(this.actionWithStatistics)
                            );
                            break;
                        default:
                            Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                    }
                }
            }
        ));

        subscriptions.add(
            Observable
                .timer(10, TimeUnit.SECONDS)
                .map(unused -> (StatisticsResponse) null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(actionWithStatistics)
        );
    }

    private void cachingStatistics(StatisticsResponse statistics){
        if(statistics.university != null) {
            AppManager.getInstance().putString(AppConst.Preference.UNIVERSITY_NAME, statistics.university.name);
            AppManager.getInstance().putInt(AppConst.Preference.UNIVERSITY_EVALUATION_COUNT, statistics.university.evaluation_count);
            AppManager.getInstance().putInt(AppConst.Preference.UNIVERSITY_STUDENT_COUNT, statistics.university.user_count);
        }else if(statistics.university_count != null){
            AppManager.getInstance().putInt(AppConst.Preference.INFO_UNIVERSITY_COUNT, statistics.university_count);
            AppManager.getInstance().putInt(AppConst.Preference.INFO_EVALUATION_COUNT, statistics.evaluation_count);
            AppManager.getInstance().putInt(AppConst.Preference.INFO_STUREND_COUNT, statistics.user_count);
        }
    }

    private StatisticsResponse getCacheStatistics(){
        StatisticsResponse statistics = new StatisticsResponse();
        if(User.getInstance().getAccessToken() != null && AppManager.getInstance().contains(AppConst.Preference.UNIVERSITY_NAME)) {
            statistics.university = new UniversityData();
            statistics.university.name = AppManager.getInstance().getString( AppConst.Preference.UNIVERSITY_NAME, null);
            statistics.university.evaluation_count = AppManager.getInstance().getInt(AppConst.Preference.UNIVERSITY_EVALUATION_COUNT, 0);
            statistics.university.user_count = AppManager.getInstance().getInt(AppConst.Preference.UNIVERSITY_STUDENT_COUNT, 0);
        }else if (AppManager.getInstance().contains(AppConst.Preference.INFO_UNIVERSITY_COUNT)){
            statistics.user_count = AppManager.getInstance().getInt(AppConst.Preference.INFO_STUREND_COUNT, 0);
            statistics.evaluation_count = AppManager.getInstance().getInt(AppConst.Preference.INFO_EVALUATION_COUNT, 0);
            statistics.university_count = AppManager.getInstance().getInt(AppConst.Preference.INFO_UNIVERSITY_COUNT, 0);
        }else{
            statistics.user_count = 0;
            statistics.evaluation_count = 0;
            statistics.university_count = 0;
        }

        return statistics;
    }


    /**
     * if user has valid token, auto sign in.<br/>
     * else invalid token, current page set SigninFragment.
     */

    private Action1<StatisticsResponse> actionWithStatistics = statistics -> {
        if (statistics == null) this.timerFinished = true;
        else {
            SpannableStringBuilder styleTextBuilder = new SpannableStringBuilder();
            this.cachingStatistics(statistics);
            if (statistics.university == null) {
                Picasso.with(this.getActivity()).load(R.drawable.ic_light_intro_house).into(this.vUnivIcon);

                SpannableString styleText = new SpannableString(String.format("%d", statistics.university_count));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_number_of_university));
                this.vUnivText.setText(styleTextBuilder);

                styleText = new SpannableString(String.format("%d", statistics.user_count));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_student));
                this.vUserText.setText(styleTextBuilder);

                styleText = new SpannableString(String.format("%d", statistics.evaluation_count));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_evaluation));
                this.vEvalText.setText(styleTextBuilder);

                styleText = new SpannableString(getResources().getString(R.string.loading_word_evaluation));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_end_word));
                this.vLoadingEndWord.setText(styleTextBuilder);

                this.validAuthorization = false;
            } else {
                Picasso.with(this.getActivity()).load(statistics.university.image_url).into(this.vUnivIcon);

                SpannableString styleText = new SpannableString(String.format("%s", statistics.university.name));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_medium), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_university));
                this.vUnivText.setText(styleTextBuilder);

                styleText = new SpannableString(String.format("%d", statistics.university.user_count));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_student));
                this.vUserText.setText(styleTextBuilder);

                styleText = new SpannableString(String.format("%d", statistics.university.evaluation_count));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_evaluation));
                this.vEvalText.setText(styleTextBuilder);

                styleText = new SpannableString(getResources().getString(R.string.loading_word_evaluation));
                styleText.setSpan(new TextAppearanceSpan(getActivity().getBaseContext(), R.style.loading_highlight_big), 0, styleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                styleTextBuilder.clear();
                styleTextBuilder.append(styleText);
                styleTextBuilder.append(getResources().getString(R.string.loading_end_word));
                this.vLoadingEndWord.setText(styleTextBuilder);
                this.validAuthorization = true;
            } this.responseActionFinished = true;
        }
        if (this.timerFinished && this.responseActionFinished) {
            if ( validAuthorization == null ) return;
            if (!validAuthorization) this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNIN, false);
            else {
                this.subscriptions.add(
                    RetrofitApi.getInstance().refresh_token(User.getInstance().getAccessToken())
                        .map(user -> user.access_token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            token -> {
                                User.getInstance().setAccessToken(token);
                                AppManager.getInstance().putString(AppConst.Preference.ACCESS_TOKEN, token);
                                this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                                this.getActivity().finish();
                            },error -> {
                                Timber.d("refresh error : %s", error);
                                error.printStackTrace();
                                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNIN, false);
                            }
                        )
                );
            }
        }
    };
}