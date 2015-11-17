package com.montserrat.app.activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.SettingsFragment;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.ApiError;
import com.montserrat.utils.support.retrofit.ApiErrorCallback;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.NavigationCallback;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.search.ToolbarSearchView;
import com.montserrat.utils.view.softkeyboard.SoftKeyboardActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class MainActivity extends SoftKeyboardActivity implements NavigationDrawerCallback, Navigator, ToolbarSearchView.ToolbarSearchViewListener, ApiErrorCallback {
    @InjectView(R.id.fac)                      protected FloatingActionControlContainer mFloatingActionControlContainer;
    @InjectView(R.id.navigation_drawer_layout) protected DrawerLayout mNavigationDrawerLayout;
    @InjectView(R.id.toolbar_search_view)      protected LinearLayout searchViewToolbar;
    @InjectView(R.id.toolbar)                  protected Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        this.attachSoftKeyboardListeners();
        ApiError.setApiErrorCallback(this);

        FloatingActionControl.getInstance().setContainer(mFloatingActionControlContainer);
        MaterialMenuDrawable mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        mToolbar.inflateMenu(R.menu.main);
        mToolbar.setTitleTextColor(Color.WHITE);

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawer.setup(R.id.navigation_drawer, mNavigationDrawerLayout, mToolbar);
        mNavigationDrawer.update();
        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER, mToolbar);

        this.createToolbarOptionsMenu(mToolbar.getMenu());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
        ToolbarSearchView.getInstance().initializeToolbarSearchView(this, searchViewToolbar, (view, position) -> {
            ToolbarSearchView.getInstance().setSelectedCandidate(position);
            ToolbarSearchView.getInstance().addHistory(ToolbarSearchView.getInstance().getSelectedCandidate());
            this.navigate(SimpleCourseFragment.class, true);
        });
        ToolbarSearchView.getInstance().setSearchViewListener(this);
    }

    /* Toolbar Search */
    @Override
    public void onSearchViewShowChanged(boolean show) {
        if (show) FloatingActionControl.getInstance().hide(false);
        else FloatingActionControl.getInstance().show(false);
    }

    @Override
    public void onSearchByQuery() {
        this.navigate(SimpleCourseFragment.class, true);
    }

    /* Menu Control */
    private MenuItem mMenuItemSearch;
    private MenuItem mMenuItemSetting;
    public void setMenuItemVisibility(int menuItemId, boolean visible) {
        switch(menuItemId) {
            case AppConst.Menu.SEARCH  : mMenuItemSearch.setVisible(visible); break;
            case AppConst.Menu.SETTING : mMenuItemSetting.setVisible(visible); break;
        }
    }

    private boolean createToolbarOptionsMenu(Menu menu) {
        mMenuItemSearch  = menu.findItem(AppConst.Menu.SEARCH);
        mMenuItemSearch.setOnMenuItemClickListener(item -> {
            ToolbarSearchView.getInstance().show();
            return true;
        });

        mMenuItemSetting = menu.findItem(AppConst.Menu.SETTING);
        mMenuItemSetting.setOnMenuItemClickListener(item -> {
            this.navigate(SettingsFragment.class, true, AnimatorType.SLIDE_TO_RIGHT);
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    /* Double Back-Pressed Termination of MainActivity */
    private boolean mReadyToTerminate = false;
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isOpened()) mNavigationDrawer.close();
        else if (ToolbarSearchView.getInstance().back()) /* Does Nothing */;
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
        this.navigate(NavigationDrawerUtils.getFragmentClassOf(position), true, fromUser);
    }

    /* Map Navigator methods to mNavigator */
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

    @Override
    public void activityFinish() {
        AppManager.getInstance().clear(AppConst.Preference.HISTORY);
        AppManager.getInstance().remove(AppConst.Preference.ACCESS_TOKEN);
        User.getInstance().clear();
        this.startActivity(new Intent(this, AuthActivity.class));
        this.finish();
    }

    @Override
    public void sendTracker(String cause, String from) {
        Timber.d("cause : %s, from : %s", cause, from);
    }
}
