package com.papyruth.android.activity;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.HomeFragment;
import com.papyruth.android.fragment.main.SettingsFragment;
import com.papyruth.android.fragment.main.SimpleCourseFragment;
import com.papyruth.android.model.unique.Evaluation;
import com.papyruth.android.navigation_drawer.NavigationDrawerCallback;
import com.papyruth.android.navigation_drawer.NavigationDrawerFragment;
import com.papyruth.android.navigation_drawer.NavigationDrawerUtils;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;
import com.papyruth.support.utility.error.Error;
import com.papyruth.support.utility.navigator.FragmentNavigator;
import com.papyruth.support.utility.navigator.NavigationCallback;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.search.SearchToolbar;
import com.papyruth.support.utility.softkeyboard.SoftKeyboardActivity;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import timber.log.Timber;

public class MainActivity extends SoftKeyboardActivity implements NavigationDrawerCallback, Navigator, SearchToolbar.OnVisibilityChangedListener, SearchToolbar.OnSearchByQueryListener, Error.OnReportToGoogleAnalytics {
    @InjectView(R.id.fac)                      protected FloatingActionControlContainer mFloatingActionControlContainer;
    @InjectView(R.id.navigation_drawer_layout) protected DrawerLayout mNavigationDrawerLayout;
    @InjectView(R.id.search_toolbar_root)      protected LinearLayout mSearchToolbarRoot;
    @InjectView(R.id.toolbar)                  protected Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.attachSoftKeyboardListeners();
        mTracker = ((PapyruthApplication) getApplication()).getTracker();
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(mFloatingActionControlContainer);
        MaterialMenuDrawable mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        mToolbar.inflateMenu(R.menu.main);
        mToolbar.setTitleTextColor(Color.WHITE);

        mMenuItemSearch  = mToolbar.getMenu().findItem(AppConst.Menu.SEARCH);
        mMenuItemSearch.setOnMenuItemClickListener(item -> {
            SearchToolbar.getInstance().show();
            return true;
        });
        mMenuItemSetting = mToolbar.getMenu().findItem(AppConst.Menu.SETTING);
        mMenuItemSetting.setOnMenuItemClickListener(item -> {
            this.navigate(SettingsFragment.class, true, AnimatorType.SLIDE_TO_RIGHT);
            return true;
        });
        super.onCreateOptionsMenu(mToolbar.getMenu());

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawer.setup(R.id.navigation_drawer, mNavigationDrawerLayout, mToolbar);
        mNavigationDrawer.update();
        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER, mToolbar);

        ViewHolderFactory.getInstance().setContext(this);
        SearchToolbar.getInstance().init(this, mSearchToolbarRoot, (view, position) -> {
            SearchToolbar.getInstance().setSelectedCandidate(position);
            SearchToolbar.getInstance().addToHistory(SearchToolbar.getInstance().getSelectedCandidate());
            this.navigate(SimpleCourseFragment.class, true);
        }, () -> {
            this.navigate(SimpleCourseFragment.class, true);
        });
        SearchToolbar.getInstance().setOnVisibilityChangedListener(this);
    }

    private MenuItem mMenuItemSearch;
    private MenuItem mMenuItemSetting;
    public void setMenuItemVisibility(int menuItemId, boolean visible) {
        switch(menuItemId) {
            case AppConst.Menu.SEARCH  : mMenuItemSearch.setVisible(visible); break;
            case AppConst.Menu.SETTING : mMenuItemSetting.setVisible(visible); break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SearchToolbar.getInstance().isOpened()) Observable.timer(100, TimeUnit.MILLISECONDS).subscribe(unused -> SearchToolbar.getInstance().showSoftKeyboard());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        SearchToolbar.getInstance().setOnVisibilityChangedListener(this);
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
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
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