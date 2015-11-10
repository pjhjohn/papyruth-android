package com.montserrat.app.activity;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.SettingsFragment;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.NavigationCallback;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.montserrat.utils.view.search.ToolbarSearchView;
import com.montserrat.utils.view.softkeyboard.SoftKeyboardActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends SoftKeyboardActivity implements NavigationDrawerCallback, Navigator, ToolbarSearchView.ToolbarSearchViewListener {
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;

    private CompositeSubscription mCompositeSubscription;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.main_navigator) FrameLayout navigatorContainer;
    @InjectView(R.id.toolbar) protected Toolbar mToolbar;
    @InjectView(R.id.toolbar_search_view) protected LinearLayout searchViewToolbar;
    private MaterialMenuDrawable mMaterialMenuDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        this.attachSoftKeyboardListeners();

        FloatingActionControl.getInstance().setContainer(this.fac);
        mCompositeSubscription = new CompositeSubscription();
        mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        ToolbarUtil.registerMenu(mToolbar, R.menu.main, item -> item.getItemId() == R.id.menu_search || super.onOptionsItemSelected(item));

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        mNavigationDrawer.setup(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout), mToolbar);
        mNavigationDrawer.update();

        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER, mToolbar);

        this.onInitializeMenuOnToolbar(mToolbar.getMenu());
        /* Instantiate Multiple ViewPagerManagers */
    }



    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
        ToolbarSearchView.getInstance().initializeToolbarSearchView(this, searchViewToolbar, (view, position)->{
            ToolbarSearchView.getInstance().setSelectedCandidate(position);
            ToolbarSearchView.getInstance().addHistory(ToolbarSearchView.getInstance().getSelectedCandidate());
            this.navigate(SimpleCourseFragment.class, true);
        });
        ToolbarSearchView.getInstance()
            .setSearchViewListener(this);

        this.setMenuItemVisibility(AppConst.Menu.MENU_SETTING, false);
//        this.setOnShowSoftKeyboard(keyboardHeight -> FloatingActionControl.getInstance().hide(false));
//        this.setOnHideSoftKeyboard(() -> FloatingActionControl.getInstance().show(false));
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    private MenuItem itemSearch;
    private MenuItem itemSetting;

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (this.mNavigationDrawer.isOpened()) this.mNavigationDrawer.close();
        else if (ToolbarSearchView.getInstance().back()) ;
        else if (this.mNavigator.back()) terminate = false;
        else if (terminate) super.onBackPressed();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
            terminate = true;
        }
    }

    /* Menu */
    public void setMenuItemVisibility(int res, boolean visible){
        if(itemSearch.getItemId() == res)
            itemSearch.setVisible(visible);
        else if(itemSetting.getItemId() == res)
            itemSetting.setVisible(visible);
    }
    private boolean onInitializeMenuOnToolbar(Menu menu) {
        this.itemSearch = menu.findItem(R.id.menu_search);
        this.itemSetting = menu.findItem(R.id.menu_setting);

        this.itemSearch.setOnMenuItemClickListener(item -> {
            ToolbarSearchView.getInstance().show();
            return true;
        });

        this.mToolbar.setOnMenuItemClickListener(item -> {
            if (item.equals(itemSetting)) {
                this.navigate(SettingsFragment.class, true, AnimatorType.SLIDE_TO_RIGHT);
            }
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }


    /* Click Callbacks for Navigation Drawer */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromUser) {
        this.terminate = false;
        this.navigate(NavigationDrawerUtils.getFragmentClassOf(position), true, fromUser);
    }

    /* Map Navigator methods to this.navigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        this.mNavigator.navigate(target, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType) {
        this.mNavigator.navigate(target, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        this.mNavigator.navigate(target, bundle, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }

    @Override
    public String getBackStackNameAt(int index) {
        return this.mNavigator.getBackStackNameAt(index);
    }

    @Override
    public boolean back() {
        return this.mNavigator.back();
    }

    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        this.mNavigator.setOnNavigateListener(listener);
    }

    @Override
    public void onSearchViewShowChanged(boolean show) {
        if (show)
            FloatingActionControl.getInstance().hide(false);
        else
            FloatingActionControl.getInstance().show(false);
    }

    @Override
    public void onSearchByQuery() {
        this.navigate(SimpleCourseFragment.class, true);
    }
}
