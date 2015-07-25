package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.fragment.main.EvaluationStep3Fragment;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.Signup;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;

    @InjectView(R.id.nextBtn) protected Button next;
    @InjectView (R.id.entrance) protected ButtonFlat entrance;

    private MaterialDialog entranceYearDialog;
    private Integer entranceYear;
    private Observable<Integer> entranceYearObservable;

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
        this.entranceYearObservable = this.buildEntranceYearDialog();
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
    }

    private Observable<Integer> buildEntranceYearDialog() {
        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        return Observable.create(observer -> this.entranceYearDialog = new MaterialDialog.Builder(this.getActivity())
                .title(R.string.dialog_title_entrance_year)
                .negativeText(R.string.cancel)
                .items(years)
                .itemsCallback((dialog, view, which, text) -> {
                    this.entrance.setText(text.toString() + getResources().getString(R.string.entrance_postfix));
                    this.entranceYear = Integer.parseInt(text.toString());
                    observer.onNext(this.entranceYear);
                })
                .build()
        );
    }

    @Override
    public void onPageFocused() {
        ((AuthActivity)this.getActivity()).signUp(true);
        ((AuthActivity)this.getActivity()).signUpStep(1);

        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        this.subscription.add(ViewObservable.clicks(this.entrance).filter(unused -> !this.entranceYearDialog.isShowing()).subscribe(unused -> this.entranceYearDialog.show()));

        this.setEntranceYear();
        this.subscription.add(
            this.entranceYearObservable.startWith((Integer) null)
                .map(year ->
                        entranceYear != null && AppConst.MIN_ENTRANCE_YEAR <= entranceYear && entranceYear <= Calendar.getInstance().get(Calendar.YEAR)
                )
                .subscribe(
                    valid -> {
                        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                        if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                        else if (!visible && valid) FloatingActionControl.getInstance().show(true);
                    }
                )
        );
        this.subscription.add(
            ViewObservable
                .clicks(FloatingActionControl.getButton())
                .subscribe(unused -> {
                    Signup.getInstance().setEntrance_year(this.entranceYear);
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
                }, error -> Timber.d("page change error %s", error))
        );

        this.subscription.add(
            ViewObservable.clicks(this.next)
                .subscribe(u -> {
                    this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
                })
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
