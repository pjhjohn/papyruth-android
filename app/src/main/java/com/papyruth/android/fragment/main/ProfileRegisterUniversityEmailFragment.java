package com.papyruth.android.fragment.main;

import android.app.Activity;
import android.app.Fragment;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.FailureDialog;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.opensource.rx.RxValidator;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.StatusBarHelper;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.navigator.Navigator;
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

import static com.papyruth.support.opensource.rx.RxValidator.toString;

/**
 * Created by pjhjohn on 2015-05-19.
 */
public class ProfileRegisterUniversityEmailFragment extends Fragment {
    private Navigator navigator;
    private Context context;
    private Resources res;
    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((PapyruthApplication) getActivity().getApplication()).getTracker();
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

    @InjectView (R.id.university_email_icon)    protected ImageView icon;
    @InjectView (R.id.university_email_label)   protected TextView label;
    @InjectView (R.id.university_email_text)    protected EditText email;
    private CompositeSubscription subscriptions;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_register_university_email, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        Picasso.with(context).load(R.drawable.ic_light_university_email).transform(new ColorFilterTransformation(res.getColor(R.color.icon_material))).into(this.icon);
        if(Locale.getDefault().equals(Locale.KOREA)) this.label.setText(Html.fromHtml(String.format("%s<strong>%s</strong>%s", res.getString(R.string.label_university_email_prefix), res.getString(R.string.label_university_email_content), res.getString(R.string.label_university_email_postfix))));
        else this.label.setText(Html.fromHtml(String.format("%s <strong>%s</strong> %s", res.getString(R.string.label_university_email_prefix), res.getString(R.string.label_university_email_content), res.getString(R.string.label_university_email_postfix))));
        this.email.setText(User.getInstance().getUniversityEmail());
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
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_profile_change_university_email));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        toolbar.setTitle(R.string.toolbar_profile_register_university_email);
        ToolbarHelper.getColorTransitionAnimator(toolbar, R.color.toolbar_blue).start();
        StatusBarHelper.changeColorTo(getActivity(), R.color.status_bar_blue);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.getInstance().setControl(R.layout.fab_normal_done_blue);
        FloatingActionControl.getButton().setMax(100);
        FloatingActionControl.getButton().setShowProgressBackground(false);
        this.subscriptions.add(this.registerSubmitCallback());
        this.subscriptions.add(WidgetObservable
            .text(this.email)
            .debounce(400, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .map(toString)
            .map(RxValidator.getErrorMessageEmail)
            .startWith((String) null)
            .map(error -> error == null)
            .subscribe(valid -> {
                boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                else if (!visible && valid) FloatingActionControl.getInstance().show(true);
            }, error -> ErrorHandler.handle(error, this))
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
                Api.papyruth().users_me_university_email(
                    User.getInstance().getAccessToken(),
                    this.email.getText().toString()
                ))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    Timber.d("Response : %s", response);
                    FloatingActionControl.getButton().setIndeterminate(false);
                    FloatingActionControl.getButton().setProgress(0, true);
                    if (response.success) {
                        User.getInstance().setUniversityEmail(email.getText().toString());
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
                                FailureDialog.show(this.getActivity(), FailureDialog.Type.UNIVERSITY_EMAIL);
                                this.subscriptions.add(this.registerSubmitCallback());
                                break;
                            default:
                                Timber.e("Unexpected Status code : %d - Needs to be implemented", ((RetrofitError) error).getResponse().getStatus());
                        }
                    }
                    ErrorHandler.handle(error, this);
                }
            );
    }
}
