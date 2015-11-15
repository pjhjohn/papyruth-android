package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.activity.AuthActivity;
import com.montserrat.app.model.unique.SignUpForm;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-12.
 */

public class SignUpStep3Fragment extends Fragment implements OnPageFocus, OnPageUnfocus{
    private ViewPagerController pagerController;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = ((AuthActivity) activity).getViewPagerController();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.pagerController = null;
    }

    @InjectView(R.id.gender) protected RadioGroup gender;
    @InjectView(R.id.realname) protected EditText realname;
    @InjectView(R.id.icon_gender) protected ImageView iconGender;
    @InjectView(R.id.icon_realname) protected ImageView iconRealname;
    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_step3, container, false);
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
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_gender).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconGender);
        Picasso.with(this.getActivity().getBaseContext()).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.iconRealname);
        this.pagerController.addImeControlFragment(AppConst.ViewPager.Auth.SIGNUP_STEP3);
        if(this.pagerController.getCurrentPage() == AppConst.ViewPager.Auth.SIGNUP_STEP3){
            ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.realname, InputMethodManager.SHOW_FORCED);
        }
    }

    public void showFAC() {
        String validateName = RxValidator.getErrorMessageRealname.call(this.realname.getText().toString());

        boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
        boolean valid = validateName == null && this.gender.getCheckedRadioButtonId() != -1;

        if (!visible && valid) FloatingActionControl.getInstance().show(true, 200, TimeUnit.MILLISECONDS);
        else if (visible && !valid) FloatingActionControl.getInstance().hide(true);
    }

    @Override
    public void onPageFocused() {
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_next).hide(true);
        ((AuthActivity) getActivity()).setOnShowSoftKeyboard(null);
        ((AuthActivity) getActivity()).setOnHideSoftKeyboard(null);
        if(this.subscription.isUnsubscribed()) this.subscription = new CompositeSubscription();

        InputMethodManager imm = ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(this.realname, InputMethodManager.SHOW_FORCED);
        this.realname.requestFocus();

        if(SignUpForm.getInstance().getRealname() != null){
            this.realname.setText(SignUpForm.getInstance().getRealname());
            ((RadioButton)this.gender.findViewById(this.gender.getChildAt((SignUpForm.getInstance().getIsBoy()?0:1)).getId())).setChecked(true);
            this.showFAC();
        }

        if(this.realname.length() > 0 && this.gender.getCheckedRadioButtonId() != -1){
            this.showFAC();
        }

        this.subscription.add(WidgetObservable
            .text(this.realname)
            .debounce(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(event -> {
                String validateName = RxValidator.getErrorMessageRealname.call(event.text().toString());
                this.realname.setError(validateName);
                this.showFAC();
            })
        );

        this.gender.setOnCheckedChangeListener((group, id) -> this.showFAC());


        this.subscription.add(
            Observable.mergeDelayError(
                FloatingActionControl.clicks().map(event -> FloatingActionControl.getButton().getVisibility() == View.VISIBLE),
                Observable.create(observer -> this.realname.setOnEditorActionListener((TextView v, int action, KeyEvent event) -> {
                    observer.onNext(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                    return !(FloatingActionControl.getButton().getVisibility() == View.VISIBLE);
                }))
            )
                .filter(use -> use)
                .subscribe(
                    use -> {
                        SignUpForm.getInstance().setRealname(this.realname.getText().toString());
                        SignUpForm.getInstance().setIsBoy(((RadioButton) this.gender.findViewById(this.gender.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male)));
                        this.pagerController.setCurrentPage(AppConst.ViewPager.Auth.SIGNUP_STEP4, true);
                    },
                    error -> error.printStackTrace())
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
