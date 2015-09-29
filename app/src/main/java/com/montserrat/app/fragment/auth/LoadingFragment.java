package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.activity.MainActivity;
import com.montserrat.app.model.UniversityData;
import com.montserrat.app.model.response.StatisticsResponse;
import com.montserrat.app.model.unique.Statistics;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class LoadingFragment extends Fragment {
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

    @InjectView (R.id.loading_university_icon) protected ImageView universityIcon;
    @InjectView (R.id.loading_university_text) protected TextView universityText;
    @InjectView (R.id.loading_user_icon) protected ImageView userIcon;
    @InjectView (R.id.loading_user_text) protected TextView userText;
    @InjectView (R.id.loading_evaluation_icon) protected ImageView evaluationIcon;
    @InjectView (R.id.loading_evaluation_text) protected TextView evaluationText;
    @InjectView (R.id.loading_quote) protected TextView loadingQuote;
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

    @Override
    public void onResume () {
        super.onResume();
        if(Statistics.getInstance().getUniversity() == null) {
            Picasso.with(this.getActivity()).load(R.drawable.ic_light_intro_house).into(this.universityIcon);
            this.universityText.setText(String.format("%d", Statistics.getInstance().getUniversityCount()));
            this.userText.setText(String.format("%d", Statistics.getInstance().getUserCount()));
            this.evaluationText.setText(String.format("%d", Statistics.getInstance().getEvaluationCount()));
        } else {
            final UniversityData univ = Statistics.getInstance().getUniversity();
            Picasso.with(this.getActivity()).load(univ.image_url).into(this.universityIcon);
            this.universityText.setText(univ.name);
            this.userText.setText(String.format("%d", univ.user_count));
            this.evaluationText.setText(String.format("%d", univ.evaluation_count));
        }
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);

        subscriptions.add(Observable
            .timer(2, TimeUnit.SECONDS)
            .map(unused -> (StatisticsResponse) null)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(unused -> {
                if(Statistics.getInstance().getUniversity() != null /* Condition that user has been successfully authenticated */) {
                    this.getActivity().startActivity(new Intent(this.getActivity(), MainActivity.class));
                    this.getActivity().finish();
                } else this.navigator.navigate(AuthFragment.class, false);
            })
        );
    }
}