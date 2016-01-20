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
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.papyruth.support.opensource.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileChangeNicknameFragment extends TrackerFragment {
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

    @Bind(R.id.nickname_icon)   protected ImageView mNicknameIcon;
    @Bind(R.id.nickname_label)  protected TextView mNicknameLabel;
    @Bind(R.id.nickname_text)   protected EditText mNickname;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_change_nickname, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        Picasso.with(mContext).load(R.drawable.ic_nickname_48dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mNicknameIcon);
        if(Locale.getDefault().equals(Locale.KOREA)) mNicknameLabel.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", mResources.getString(R.string.profile_change_nickname_body_prefix), mResources.getString(R.string.profile_change_nickname_body), mResources.getString(R.string.profile_change_nickname_body_postfix))));
        else mNicknameLabel.setText(Html.fromHtml(String.format("%s <strong>%s</strong> %s", mResources.getString(R.string.profile_change_nickname_body_prefix), mResources.getString(R.string.profile_change_nickname_body), mResources.getString(R.string.profile_change_nickname_body_postfix))));
        mNickname.setText(User.getInstance().getNickname());
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
        mToolbar.setTitle(R.string.toolbar_profile_change_nickname);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, false);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_blue);
        if(FloatingActionControl.getButton() != null) {
            FloatingActionControl.getButton().setMax(100);
            FloatingActionControl.getButton().setShowProgressBackground(false);
        }
        mCompositeSubscription.clear();
        setSubmissionCallback();
        mCompositeSubscription.add(WidgetObservable
            .text(mNickname)
            .debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .map(toString)
            .map(RxValidator.getErrorMessageNickname)
            .startWith((String) null)
            .map(error -> error == null)
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
                Api.papyruth().post_users_me_edit_nickname(
                    User.getInstance().getAccessToken(),
                    mNickname.getText().toString()
                ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if(response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        mNavigator.back();
                    }
                },
                error -> {
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if(error instanceof RetrofitError && ((RetrofitError) error).getResponse().getStatus() == 400) {
                        FailureDialog.show(getActivity(), FailureDialog.Type.CHANGE_NICKNAME);
                        setSubmissionCallback();
                    } else ErrorHandler.handle(error, this, true);
                }
            )
        );
    }
}
