package com.papyruth.android.activity;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.BuildConfig;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.CourseFragment;
import com.papyruth.android.fragment.main.HomeFragment;
import com.papyruth.android.fragment.main.SettingsFragment;
import com.papyruth.android.fragment.main.SimpleCourseFragment;
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.unique.Course;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.navigation_drawer.NavigationDrawerCallback;
import com.papyruth.android.navigation_drawer.NavigationDrawerFragment;
import com.papyruth.android.navigation_drawer.NavigationDrawerUtils;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;
import com.papyruth.support.utility.error.Error;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.navigator.FragmentNavigator;
import com.papyruth.support.utility.navigator.NavigationCallback;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.search.SearchToolbar;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends Activity implements NavigationDrawerCallback, Navigator, SearchToolbar.OnVisibilityChangedListener, SearchToolbar.OnSearchByQueryListener, Error.OnReportToGoogleAnalytics {
    @Bind(R.id.fac)                      protected FloatingActionControlContainer mFloatingActionControlContainer;
    @Bind(R.id.navigation_drawer_layout) protected DrawerLayout mNavigationDrawerLayout;
    @Bind(R.id.search_toolbar_root)      protected LinearLayout mSearchToolbarRoot;
    @Bind(R.id.toolbar)                  protected Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        mTracker = ((PapyruthApplication) getApplication()).getTracker();

        Crashlytics.setUserIdentifier(User.getInstance().getId() == null? null : User.getInstance().getId().toString());
        Crashlytics.setUserEmail(User.getInstance().getEmail());
        Crashlytics.setUserName(User.getInstance().getNickname());
        Crashlytics.setString(getResources().getString(R.string.crashlytics_key_university), User.getInstance().getUniversityName());
        Crashlytics.setBool(getResources().getString(R.string.crashlytics_key_debug_mode), BuildConfig.DEBUG);

        ButterKnife.bind(this);
        FloatingActionControl.getInstance().setContainer(mFloatingActionControlContainer);
        MaterialMenuDrawable mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        mToolbar.inflateMenu(R.menu.main);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_red));

        mToolbar.getMenu().findItem(AppConst.Menu.SEARCH).setOnMenuItemClickListener(item -> {
            SearchToolbar.getInstance().show();
            return true;
        });
        mToolbar.getMenu().findItem(AppConst.Menu.SETTING).setOnMenuItemClickListener(item -> {
            this.navigate(SettingsFragment.class, true, AnimatorType.SLIDE_TO_RIGHT);
            return true;
        });
        super.onCreateOptionsMenu(mToolbar.getMenu());

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawer.setup(R.id.navigation_drawer, mNavigationDrawerLayout, mToolbar);
        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER, mToolbar);

        ViewHolderFactory.getInstance().setContext(this);
        SearchToolbar.getInstance().init(this, mSearchToolbarRoot, (view, object) -> {
            if(object instanceof CandidateData) {
                CandidateData candidate = ((CandidateData) object);
                if (candidate.course_id != null) {
                    Course.getInstance().clear();
                    Course.getInstance().setId(candidate.course_id);
                    this.navigate(CourseFragment.class, true);
                }else
                    this.navigate(SimpleCourseFragment.class, true);
            }
        }, () -> this.navigate(SimpleCourseFragment.class, true));
        SearchToolbar.getInstance().setOnVisibilityChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavigationDrawer.update();
        if(SearchToolbar.getInstance().isOpened()) Observable.timer(100, TimeUnit.MILLISECONDS).subscribe(unused -> SearchToolbar.getInstance().showSoftKeyboard());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        SearchToolbar.getInstance().setOnVisibilityChangedListener(this);
        Api.papyruth().get_users_me(User.getInstance().getAccessToken()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            response -> User.getInstance().update(response.user), error -> ErrorHandler.handle(error, this)
        );
    }

    /* Double Back-Pressed Termination of MainActivity */
    private boolean mReadyToTerminate = false;
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isOpened()) mNavigationDrawer.close();
        else if (SearchToolbar.getInstance().back()) /* Does Nothing */;
        else if (mNavigator.back()) mReadyToTerminate = false;
        else if (mReadyToTerminate) this.finish();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.application_confirm_exit), Toast.LENGTH_SHORT).show();
            mReadyToTerminate = true;
        }
    }

    /* Click Callbacks for Navigation Drawer */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromUser) {
        mReadyToTerminate = false;
        Class<? extends Fragment> fragmentClass = NavigationDrawerUtils.getFragmentClassOf(position);
        Evaluation.getInstance().clear();
        this.navigate(fragmentClass, true, fromUser);
        mTracker.send(
            new HitBuilders.EventBuilder(
                getString(R.string.ga_category_drawer),
                getString(R.string.ga_event_click)
            ).build()
        );
    }

    /* Toolbar Search */
    @Override
    public void onVisibilityChanged(boolean visible) {
        HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder().setCategory(getString(R.string.ga_category_search_view));
        if (visible) {
            FloatingActionControl.getInstance().hide(false);
            mTracker.send(builder
                .setAction(getResources().getString(R.string.ga_event_open))
                .build());
        }else {
            FloatingActionControl.getInstance().show(false);
            mTracker.send(builder
                .setAction(getResources().getString(R.string.ga_event_close))
                .build());
        }
    }
    @Override
    public void onSearchByQuery() {
        this.navigate(SimpleCourseFragment.class, true);
    }

    /* Google Analytics */
    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
    @Override
    public void onReportToGoogleAnalytics(String description, String source, boolean fatal) {
        Timber.d("MainActivity.onReportToGoogleAnalytics from %s\nCause : %s", source, description);
        mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(fatal).build());
    }

    /* Bind FragmentNavigator methods to mNavigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        mNavigator.navigate(target, addToBackStack);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType) {
        mNavigator.navigate(target, addToBackStack, animatorType);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, addToBackStack, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        mNavigator.navigate(target, bundle, addToBackStack);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, clear);
    }
    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }
    @Override
    public String getBackStackNameAt(int index) {
        return mNavigator.getBackStackNameAt(index);
    }
    @Override
    public boolean back() {
        return mNavigator.back();
    }
    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        mNavigator.setOnNavigateListener(listener);
    }
}