package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.FailureDialog;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.support.rx.RxValidator;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.papyruth.utils.support.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileChangeNicknameFragment extends Fragment {
    private Navigator navigator;
    private Context context;
    private Resources res;
    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
        this.context = activity;
        this.res = activity.getResources();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.navigator = null;
    }

    @InjectView (R.id.nickname_icon) protected ImageView icon;
    @InjectView (R.id.nickname_label) protected TextView label;
    @InjectView (R.id.nickname_text) protected EditText nickname;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_change_nickname, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        Picasso.with(context).load(R.drawable.ic_light_person).transform(new ColorFilterTransformation(Color.GRAY)).into(this.icon);
        if(Locale.getDefault().equals(Locale.KOREA)) this.label.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", res.getString(R.string.label_nickname_change_prefix), res.getString(R.string.label_nickname_change_content), res.getString(R.string.label_nickname_change_postfix))));
        else this.label.setText(Html.fromHtml(String.format("%s <strong>%s</strong> %s", res.getString(R.string.label_nickname_change_prefix), res.getString(R.string.label_nickname_change_content), res.getString(R.string.label_nickname_change_postfix))));
        this.nickname.setText(User.getInstance().getNickname());
        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscriptions!=null && !this.subscriptions.isUnsubscribed()) this.subscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_profile_change_nickname));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar.setTitle(R.string.toolbar_edit_profile);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.toolbar_blue).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_blue);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        this.subscriptions.add(this.registerSubmitCallback());
        this.subscriptions.add(WidgetObservable
                .text(this.nickname)
                .debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(toString)
                .map(RxValidator.getErrorMessageNickname)
                .startWith((String) null)
                .map(error -> error == null)
                .subscribe(valid -> {
                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                    if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                    else if (!visible && valid) FloatingActionControl.getInstance().show(true);
                }, error-> ErrorHandler.throwError(error, this))
        );
    }

    public Subscription registerSubmitCallback() {
        return FloatingActionControl
            .clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .map(unused -> {
                FloatingActionControl.getButton().setIndeterminate(true);
                return unused;
            })
            .observeOn(Schedulers.io())
            .flatMap(unused ->
                Api.papyruth().users_me_edit_nickname(
                    User.getInstance().getAccessToken(),
                    this.nickname.getText().toString()
                ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    Timber.d("Response : %s", response);
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if (response.success) {
                        User.getInstance().update(response.user, response.access_token);
                        this.navigator.back();
                    } else {
                        // TODO : Failed to Update User Profile
                    }
                },
                error -> {
                    Timber.d("Error : %s", error);
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if (error instanceof RetrofitError) {
                        switch (((RetrofitError) error).getResponse().getStatus()) {
                            case 400:
                                FailureDialog.show(this.getActivity(), FailureDialog.Type.CHANGE_NICKNAME);
                                this.subscriptions.add(this.registerSubmitCallback());
                                break;
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                    ErrorHandler.throwError(error, this);
                }
            );
    }
}