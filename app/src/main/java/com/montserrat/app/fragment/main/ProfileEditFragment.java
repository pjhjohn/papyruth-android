package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gc.materialdesign.views.ButtonFlat;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.support.rx.RxValidator;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.Navigator;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.isValidRadioButton;
import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileEditFragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView (R.id.email) protected MaterialEditText email;
    @InjectView (R.id.realname) protected MaterialEditText realname;
    @InjectView (R.id.nickname) protected MaterialEditText nickname;
    @InjectView (R.id.gender) protected RadioGroup gender;
    @InjectView (R.id.university) protected ButtonFlat university;
    @InjectView (R.id.entrance) protected ButtonFlat entrance;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.university.setEnabled(false);
        this.entrance.setEnabled(false);
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_edit_profile);
        toolbar.setTitleTextColor(Color.WHITE);
        ToolbarUtil.getColorTransitionAnimator(toolbar, AppConst.COLOR_POINT_GPA_SATISFACTION).start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(toolbar.getY() < 0) ToolbarUtil.show(toolbar);
        FloatingActionControl.getInstance().setControl(R.layout.fab_done);

        this.email.setText(User.getInstance().getEmail());
        this.university.setText(""+User.getInstance().getUniversityName());
        this.realname.setText(User.getInstance().getRealname());
        this.nickname.setText(User.getInstance().getNickname());
        this.gender.check(User.getInstance().getGenderIsBoy() ? R.id.gender_male : R.id.gender_female);
        this.entrance.setText(String.valueOf(User.getInstance().getEntranceYear()));

        this.subscriptions.add(Observable.combineLatest(
            WidgetObservable.text(this.realname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageRealname).startWith((String)null),
            WidgetObservable.text(this.nickname).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessageNickname).startWith((String)null),
            RxValidator.createObservableRadioGroup(gender).map(isValidRadioButton).startWith(true).delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()),
            (String realnameError, String nicknameError, Boolean validRadioGroup) -> {
                this.realname.setError(realnameError);
                this.nickname.setError(nicknameError);
                return realnameError == null && nicknameError == null && validRadioGroup != null && validRadioGroup;
            })
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            })
        );

        this.subscriptions.add(FloatingActionControl
            .clicks()
            .map(pass -> {
                this.progress.setVisibility(View.VISIBLE);
                return pass;
            })
            .observeOn(Schedulers.io())
            .flatMap(unused ->
                RetrofitApi.getInstance().users_me_edit(
                    User.getInstance().getAccessToken(),
                    this.email.getText().toString(),
                    this.realname.getText().toString(),
                    this.nickname.getText().toString(),
                    ((RadioButton) this.gender.findViewById(gender.getCheckedRadioButtonId())).getText().equals(this.getResources().getString(R.string.gender_male))
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    this.progress.setVisibility(View.GONE);
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        this.navigator.back();
                    } else {
                        // TODO : Failed to Update User Profile
                    }
                },
                error -> {
                    this.progress.setVisibility(View.GONE);
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                }
            )
        );
    }
}
