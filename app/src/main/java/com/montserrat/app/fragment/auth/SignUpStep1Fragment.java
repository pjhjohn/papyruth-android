package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.SignUpForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.SquareImageView;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep1Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.pagerController = null;
    }

    @InjectView(R.id.nextBtn) protected Button next;
    @InjectView(R.id.entrance) protected ButtonFlat entrance;
    @InjectView(R.id.university_item_image) protected SquareImageView univerity;
    @InjectView(R.id.entrance_year_icon) protected ImageView entranceYearIcon;
    private MaterialDialog entranceYearDialog;
    private Integer entranceYear;
    private Observable<Integer> entranceYearObservable;
    private boolean isNext;
    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        this.entrance.setRippleSpeed(40.0f);
        this.entranceYearObservable = this.buildEntranceYearDialog();
        this.isNext = false;
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
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_history).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.entranceYearIcon);
    }

    private Observable<Integer> buildEntranceYearDialog() {
        final int length = Calendar.getInstance().get(Calendar.YEAR) - AppConst.MIN_ENTRANCE_YEAR + 1;
        String[] years = new String[length];
        for(int i = 0; i < length; i ++) years[i] = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - i);
        return Observable
            .create(observer -> this.entranceYearDialog = new MaterialDialog.Builder(this.getActivity())
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
        ((AuthActivity)this.getActivity()).signUpStep(1);
        Picasso.with(this.getActivity().getBaseContext()).load(SignUpForm.getInstance().getImageUrl()).into(this.univerity);
        if(this.subscription.isUnsubscribed()) this.subscription = new CompositeSubscription();

        if(SignUpForm.getInstance().getEntranceYear() != null){
            this.entrance.setText(SignUpForm.getInstance().getEntranceYear().toString() + getResources().getString(R.string.entrance_postfix));
            this.entranceYear = SignUpForm.getInstance().getEntranceYear();
            this.isNext = true;
        }

        FloatingActionControl.getInstance().setControl(R.layout.fab_next);
        this.subscription.add(ViewObservable.clicks(this.entrance).filter(unused -> !this.entranceYearDialog.isShowing()).subscribe(unused -> this.entranceYearDialog.show()));

        this.subscription.add(this.entranceYearObservable
            .startWith((Integer) null)
            .map(year -> entranceYear != null && AppConst.MIN_ENTRANCE_YEAR <= entranceYear && entranceYear <= Calendar.getInstance().get(Calendar.YEAR))
            .subscribe(
                valid -> {
                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                    if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                    else if (isNext || (!visible && valid))
                        FloatingActionControl.getInstance().show(true);
                }
            )
        );
        this.subscription.add(ViewObservable
            .clicks(FloatingActionControl.getButton())
            .subscribe(unused -> {
                if (!isNext)
                    SignUpForm.getInstance().setEntranceYear(this.entranceYear);
                this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP2, true);
            }, error -> Timber.d("page change error %s", error))
        );

        this.subscription.add(ViewObservable
            .clicks(this.next)
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
