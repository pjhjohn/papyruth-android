package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends Fragment{
    private ViewPagerController pagerController;
    @InjectView(R.id.entrance_year_list) protected ListView yearList;
    @InjectView(R.id.entrance_year) protected TextView year;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.setEntranceYear();

    }

    public void setEntranceYear(){
        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        this.yearList.setAdapter(new ArrayAdapter<String>(this.getActivity().getBaseContext(), R.layout.cardview_simpletext, years));

        this.subscription.add(
            WidgetObservable
                .itemClicks(yearList)
                .map(listener -> listener.position())
                .subscribe(position -> {
                    int height = yearList.getHeight();
                    int itemHeight = yearList.getChildAt(0).getHeight();
                    this.yearList.setSelectionFromTop(3, height / 2 - itemHeight / 2);
                    Timber.d("year2 %s", yearList.getChildAt(position).toString());
                })
        );
    }
}
