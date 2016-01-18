package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.FailureDialog;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.papyruth.support.opensource.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileChangePasswordFragment extends TrackerFragment {
    private Navigator mNavigator;
    private Context mContext;
    private Resources mResources;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mContext = activity;
        mResources = activity.getResources();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mNavigator = null;
    }

    @Bind(R.id.password_icon) protected ImageView mPasswordIcon;
    @Bind(R.id.password_label) protected TextView mPasswordLabel;
    @Bind(R.id.password_old) protected EditText mOldPassword;
    @Bind(R.id.password_new) protected EditText mNewPassword;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_change_password, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        Picasso.with(mContext).load(R.drawable.ic_password_48dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mPasswordIcon);
        if(Locale.getDefault().equals(Locale.KOREA)) mPasswordLabel.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.profile_change_password_body_prefix), mResources.getString(R.string.profile_change_password_body), mResources.getString(R.string.profile_change_password_body_postfix))));
        else mPasswordLabel.setText(Html.fromHtml(String.format("%s <strong>%s</strong> %s", mResources.getString(R.string.profile_change_password_body_prefix), mResources.getString(R.string.profile_change_password_body), mResources.getString(R.string.profile_change_password_body_postfix))));
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_profile_change_password);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_blue);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        setSubmissionCallback();
        mCompositeSubscription.add(Observable.combineLatest(
                WidgetObservable.text(mOldPassword).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword).startWith((String) null),
                WidgetObservable.text(mNewPassword).debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).map(toString).map(RxValidator.getErrorMessagePassword).startWith((String) null),
                (String oldPasswordError, String newPasswordError) -> oldPasswordError == null && newPasswordError == null
            )
                .subscribe(valid -> {
                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                    if(visible && !valid) FloatingActionControl.getInstance().hide(true);
                    else if(!visible && valid) FloatingActionControl.getInstance().show(true);
                }, error -> ErrorHandler.handle(error, this))
        );
    }

    private void setSubmissionCallback() {
        mCompositeSubscription.add(FloatingActionControl
            .clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .map(unused -> {
                FloatingActionControl.getButton().setIndeterminate(true);
                return unused;
            })
            .observeOn(Schedulers.io())
            .flatMap(unused ->
                Api.papyruth().post_users_me_passwd(
                    User.getInstance().getAccessToken(),
                    mOldPassword.getText().toString(),
                    mNewPassword.getText().toString()
                ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if(response.success) mNavigator.back();
                },
                error -> {
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if(error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400:
                                FailureDialog.show(getActivity(), FailureDialog.Type.CHANGE_PASSWORD);
                                setSubmissionCallback();
                                break;
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                    ErrorHandler.handle(error, this);
                }
            ));
    }
}
