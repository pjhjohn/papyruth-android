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
import com.montserrat.app.fragment.main.EvaluationStep3Fragment;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
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

public class SignUpStep1Fragment extends Fragment implements OnPageFocus{
    private ViewPagerController pagerController;
    @InjectView(R.id.entrance_year_list) protected ListView yearList;
    @InjectView(R.id.entrance_year) protected TextView year;

    private Navigator navigator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.navigator = (Navigator)activity;
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
        String[] years = new String[length+4];
        for(int i = 0; i < length+4; i ++){
            if(i < 2 || i >= length+2)
                years[i] = "";
            else
                years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i+2);
        }
        this.yearList.setAdapter(new ArrayAdapter<String>(this.getActivity().getBaseContext(), R.layout.cardview_simpletext, years));

        this.subscription.add(
            WidgetObservable
                .itemClicks(yearList)
                .map(listener -> listener.position())
                .subscribe(position -> {
                    int height = yearList.getHeight();
                    int itemHeight = yearList.getChildAt(0).getHeight();
                    this.yearList.setSelectionFromTop(position, height / 2 - itemHeight / 2);
                    this.year.setText(years[position].toString());
                    Timber.d("selection : %s", years[position]);
//                    this.navigator.navigate(SignUpStep2Fragment.class, true);

                }, error -> {
                    Timber.d("click error : %s", error);
                })
        );
        this.subscription.add(
            WidgetObservable
                .listScrollEvents(yearList)
                .map(listener -> listener.firstVisibleItem())
                .subscribe(position -> {
                    this.year.setText(years[position+2]);
                    Timber.d("year3 %s", position);
                })
        );
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_next).show(true);
        this.subscription.add(FloatingActionControl
                .clicks()
                .subscribe(unused -> {
                    Signup.getInstance().clear();
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
                })
        );
    }
}
