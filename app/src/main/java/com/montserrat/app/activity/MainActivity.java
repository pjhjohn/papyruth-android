package com.montserrat.app.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.montserrat.utils.view.search.ToolbarSearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallback, RecyclerViewClickListener, View.OnFocusChangeListener, Navigator {
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;


    private CompositeSubscription mCompositeSubscription;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.main_navigator) FrameLayout navigatorContainer;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.query_result_outside) protected View outsideResult;
    @InjectView(R.id.toolbar) protected Toolbar mToolbar;
    private AutoCompletableSearchView mAutoCompletableSearch;
    private MaterialMenuDrawable mMaterialMenuDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);
        mCompositeSubscription = new CompositeSubscription();
        mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        mToolbar.inflateMenu(R.menu.searchview);
        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_search) return true;
            return super.onOptionsItemSelected(item);
        });
        this.onInitializeMenuOnToolbar(mToolbar.getMenu());

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        mNavigationDrawer.setup(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout), mToolbar);
        mNavigationDrawer.update();

        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER);

        /* Instantiate Multiple ViewPagerManagers */
//        mAutoCompletableSearch = new AutoCompletableSearchView(this, this, AutoCompletableSearchView.Type.SEARCH);
        mAutoCompletableSearch = ToolbarSearch.getInstance().newSearchView(this,this,AutoCompletableSearchView.Type.SEARCH);
        ToolbarSearch.getInstance().setActivityComponent(this);
        mAutoCompletableSearch.initAutoComplete(this.searchResult, this.outsideResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    private MenuItem searchitem;
    private SearchView searchView;

    public boolean onQueryTextSubmit(String query) {
        Search.getInstance().clear().setQuery(query);
        this.mAutoCompletableSearch.submit(query);

        searchView.clearFocus();
        searchResult.clearFocus();
        this.mAutoCompletableSearch.showCandidates(false);
        return false;
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(searchResult != null && ((RecyclerView)view.getParent()).getId() == searchResult.getId())
            ToolbarSearch.getInstance().onRecyclerViewClicked(view, position, true);
        else
            ToolbarSearch.getInstance().onRecyclerViewClicked(view, position, false);
    }


    public void reloadFragment(Bundle bundle){
        Fragment fragment;
        Fragment active = this.getFragmentManager().findFragmentByTag(AppConst.Tag.ACTIVE_FRAGMENT);

        if(active!=null && active.getClass() == SimpleCourseFragment.class)
            fragment = new SimpleCourseFragment();
        else
            fragment = new SimpleCourseFragment(); // temporary assign

        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(this.navigatorContainer.getId(), fragment, AppConst.Tag.ACTIVE_FRAGMENT).commit();
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (this.mNavigationDrawer.isOpened()) this.mNavigationDrawer.close();
        else if (this.mNavigator.back()) terminate = false;
        else if (terminate) super.onBackPressed();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
            terminate = true;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(view == searchView) {
            mAutoCompletableSearch.showCandidates(false);
        }else if(view == searchResult){
            // TODO : implement it!
        }
        this.mCompositeSubscription.add(
            this.mAutoCompletableSearch.autoComplete(
                (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)
            )
        );
    }

    /* Menu */
    private boolean onInitializeMenuOnToolbar(Menu menu) {
        this.searchitem = menu.findItem(R.id.menu_search);
        this.searchView = (SearchView) searchitem.getActionView();
        this.searchitem.expandActionView();
        if(searchView != null){
            searchView.setQueryHint("Input Search Query");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if(searchManager != null) searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            ((EditText)searchView.findViewById(R.id.search_src_text)).setOnEditorActionListener(
                (v, actionId, event) -> {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH){
                        onQueryTextSubmit(searchView.getQuery().toString());
                        return true;
                    }
                    return false;
                }
            );
        }
        searchitem.collapseActionView();
        searchView.setOnQueryTextFocusChangeListener(this);
        searchResult.setOnFocusChangeListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    /* Click Callbacks for Navigation Drawer */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromUser) {
        this.terminate = false;
        if(position == NavigationDrawerUtils.ItemType.SEARCH)
            AppManager.getInstance().putBoolean(AppConst.Preference.SEARCH, false);
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
}
