package com.papyruth.android.fragment.main;

import android.app.Activity;
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

import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.fragment.TrackerFragment;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileFragment extends TrackerFragment {
    private Navigator mNavigator;
    private Context mContext;
    private Resources mResources;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mContext = activity;
        mResources = mContext.getResources();
    }

    @Bind(R.id.university_image)      protected ImageView mUniversityImage;
    @Bind(R.id.university_name)       protected TextView mUniversityName;
    @Bind(R.id.entrance_year)         protected TextView mEntranceYear;
    @Bind(R.id.email_icon)            protected ImageView mEmailIcon;
    @Bind(R.id.email_text)            protected TextView mEmailText;
    @Bind(R.id.university_email_icon) protected ImageView mUniversityEmailIcon;
    @Bind(R.id.university_email_text) protected TextView mUniversityEmailText;
    @Bind(R.id.realname_icon)         protected ImageView mRealnameIcon;
    @Bind(R.id.realname_text)         protected TextView mRealnameText;
    @Bind(R.id.nickname_icon)         protected ImageView mNicknameIcon;
    @Bind(R.id.nickname_text)         protected TextView mNicknameText;
    @Bind(R.id.gender_icon)           protected ImageView mGenderIcon;
    @Bind(R.id.gender_text)           protected TextView mGenderText;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(mCompositeSubscription ==null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_profile);
        final boolean univConfirmed = User.getInstance().getUniversityConfirmed();
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SEARCH, false);
        ToolbarHelper.menuItemVisibility(mToolbar, AppConst.Menu.SETTING, true);
        FloatingActionControl.getInstance().setControl(univConfirmed? R.layout.fam_profile_without_university : R.layout.fam_profile).show(true, AppConst.ANIM_DURATION_SHORT, TimeUnit.MILLISECONDS);
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mUniversityName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Picasso.with(mContext).load(User.getInstance().getUniversityImageUrl()).into(mUniversityImage);
        mUniversityName.setText(User.getInstance().getUniversityName());
        mEntranceYear.setText(String.format("%d  %s", User.getInstance().getEntranceYear(), getResources().getString(R.string.profile_label_entrance_year_postfix)));
        Picasso.with(mContext).load(R.drawable.ic_email_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mEmailIcon);
        mEmailText.setText(User.getInstance().getEmail());
        Picasso.with(mContext).load(R.drawable.ic_realname_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mRealnameIcon);
        mRealnameText.setText(User.getInstance().getRealname());
        Picasso.with(mContext).load(R.drawable.ic_nickname_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mNicknameIcon);
        mNicknameText.setText(User.getInstance().getNickname());
        Picasso.with(mContext).load(R.drawable.ic_gender_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mGenderIcon);
        mGenderText.setText(mResources.getString(User.getInstance().getGenderIsBoy()? R.string.profile_value_male : R.string.profile_value_female));
        Picasso.with(mContext).load(R.drawable.ic_university_email_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mUniversityEmailIcon);

        if(User.getInstance().getUniversityEmail() == null) mUniversityEmailText.setText(mResources.getString(R.string.profile_value_university_email_confirmation_required));
        else if(univConfirmed) mUniversityEmailText.setText(User.getInstance().getUniversityEmail());
        else mUniversityEmailText.setText(R.string.profile_value_university_email_confirmation_pending);

        mCompositeSubscription.clear();
        mCompositeSubscription.add(FloatingActionControl
            .clicks(R.id.fab_mini_register_university_email)
            .subscribe(
                unused -> {
                    if(User.getInstance().getUniversityEmail() != null && !univConfirmed) AlertDialog.show(getActivity(), mNavigator, AlertDialog.Type.UNIVERSITY_CONFIRMATION_REQUIRED);
                    else mNavigator.navigate(ProfileRegisterUniversityEmailFragment.class, true);
                }, error -> ErrorHandler.handle(error, this)
            )
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
