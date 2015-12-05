package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileFragment extends Fragment {
    private Navigator mNavigator;
    private Tracker mTracker;
    private Context mContext;
    private Resources mResources;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
        mContext = activity;
        mResources = mContext.getResources();
    }

    @InjectView(R.id.university_image)      protected ImageView mUniversityImage;
    @InjectView(R.id.university_name)       protected TextView mUniversityName;
    @InjectView(R.id.entrance_year)         protected TextView mEntranceYear;
    @InjectView(R.id.email_icon)            protected ImageView mEmailIcon;
    @InjectView(R.id.email_text)            protected TextView mEmailText;
    @InjectView(R.id.university_email_icon) protected ImageView mUniversityEmailIcon;
    @InjectView(R.id.university_email_text) protected TextView mUniversityEmailText;
    @InjectView(R.id.realname_icon)         protected ImageView mRealnameIcon;
    @InjectView(R.id.realname_text)         protected TextView mRealnameText;
    @InjectView(R.id.nickname_icon)         protected ImageView mNicknameIcon;
    @InjectView(R.id.nickname_text)         protected TextView mNicknameText;
    @InjectView(R.id.gender_icon)           protected ImageView mGenderIcon;
    @InjectView(R.id.gender_text)           protected TextView mGenderText;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_profile));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mToolbar.setTitle(R.string.toolbar_profile);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, true);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);

        FloatingActionControl.getInstance().setControl(R.layout.fam_profile).show(true, AppConst.ANIM_DURATION_SHORT, TimeUnit.MILLISECONDS);
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mUniversityName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Picasso.with(mContext).load(User.getInstance().getUniversityImageUrl()).into(mUniversityImage);
        mUniversityName.setText(User.getInstance().getUniversityName());
        mEntranceYear.setText(String.format("%d  %s", User.getInstance().getEntranceYear(), getResources().getString(R.string.entrance_postfix)));
        Picasso.with(mContext).load(R.drawable.ic_light_email).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mEmailIcon);
        mEmailText.setText(User.getInstance().getEmail());
        Picasso.with(mContext).load(R.drawable.ic_light_realname).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mRealnameIcon);
        mRealnameText.setText(User.getInstance().getRealname());
        Picasso.with(mContext).load(R.drawable.ic_light_nickname).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mNicknameIcon);
        mNicknameText.setText(User.getInstance().getNickname());
        Picasso.with(mContext).load(R.drawable.ic_light_gender).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mGenderIcon);
        mGenderText.setText(mResources.getString(User.getInstance().getGenderIsBoy() ? R.string.gender_male : R.string.gender_female));
        Picasso.with(mContext).load(R.drawable.ic_light_university_email).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mUniversityEmailIcon);
        mUniversityEmailText.setText(User.getInstance().getUniversityEmail() != null ? User.getInstance().getUniversityEmail() : mResources.getString(R.string.label_university_email_need));

        if(!User.getInstance().getUniversityConfirmed()) {
            mUniversityEmailText.setText(R.string.confirm_university_email);
            mUniversityEmailText.setClickable(true);
            mUniversityEmailText.setOnClickListener(v -> {
                AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.NEED_UNIVERSITY_CONFIRMATION);
            });
        }

        mCompositeSubscription.add(FloatingActionControl
                .clicks(R.id.fab_mini_register_university_email)
                .filter(unused -> User.getInstance().getUniversityEmail() == null)
                .subscribe(unused -> mNavigator.navigate(ProfileRegisterUniversityEmailFragment.class, true), error -> ErrorHandler.handle(error, this))
        );
        mCompositeSubscription.add(FloatingActionControl
                .clicks(R.id.fab_mini_change_nickname)
                .subscribe(unused -> mNavigator.navigate(ProfileChangeNicknameFragment.class, true), error -> ErrorHandler.handle(error, this))
        );
        mCompositeSubscription.add(FloatingActionControl
                .clicks(R.id.fab_mini_change_password)
                .subscribe(unused -> mNavigator.navigate(ProfileChangePasswordFragment.class, true), error -> ErrorHandler.handle(error, this))
        );
    }
}
