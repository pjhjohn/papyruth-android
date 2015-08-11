package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import static com.montserrat.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileEditPasswordFragment extends Fragment {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView (R.id.old_password) protected MaterialEditText old_password;
    @InjectView (R.id.new_password) protected MaterialEditText new_password;
    @InjectView (R.id.progress) protected View progress;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit_password, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
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

        this.subscriptions.add(Observable.combineLatest(
            WidgetObservable.text(this.old_password).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            WidgetObservable.text(this.old_password).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword),
            (String oldPasswordError, String newPasswordError) -> {
                this.old_password.setError(oldPasswordError);
                this.new_password.setError(newPasswordError);
                return oldPasswordError == null && newPasswordError == null;
            })
            .startWith(false)
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
                RetrofitApi.getInstance().users_me_passwd(
                    User.getInstance().getAccessToken(),
                    this.old_password.getText().toString(),
                    this.new_password.getText().toString()
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    this.progress.setVisibility(View.GONE);
                    if (response.success) {
                        this.navigator.back();
                    } else {
                        Toast.makeText(this.getActivity(), getResources().getString(R.string.failed_passwd), Toast.LENGTH_SHORT).show();
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
