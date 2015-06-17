package com.montserrat.app.activity;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import com.montserrat.app.R;
import com.montserrat.app.fragment.DummyFragment;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.PartialCourseFragment;
import com.montserrat.app.fragment.main.ProfileFragment;
import com.montserrat.app.fragment.main.SignOutFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.navigation_drawer.NavigationDrawerItem;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallback, RecyclerViewClickListener, View.OnFocusChangeListener, Navigator {
    private NavigationDrawerFragment drawer;
    private FragmentNavigator navigator;

    private CompositeSubscription subscriptions;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.main_navigator) FrameLayout navigatorContainer;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.query_result_outside) protected View outsideResult;

    private AutoCompletableSearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);
        this.subscriptions = new CompositeSubscription();

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.navigator = new FragmentNavigator(R.id.main_navigator, this.getFragmentManager(), HomeFragment.class);

        this.drawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setup(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout), (Toolbar) this.findViewById(R.id.toolbar));
        this.drawer.update();

        /* Instantiate Multiple ViewPagerManagers */
        this.search = new AutoCompletableSearchView(this, this, AutoCompletableSearchView.Type.TOOLBAR);
        this.search.autoCompleteSetup(this.searchResult, this.outsideResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    public int getActionbarHeight() {
        return this.getSupportActionBar().getHeight();
    }

    private MenuItem searchitem;
    private SearchView searchView;

    public void restoreActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("");
    }

    public boolean onQueryTextSubmit(String query) {
        Search.getInstance().clear().setQuery(query);
        Timber.d("submit query2 : %s", query);
        this.search.submit(query);

//        ((EditText)searchView.findViewById(R.id.search_src_text)).focus
        searchView.clearFocus();
        searchResult.clearFocus();
        this.search.expandResult(false);
        return false;
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        this.search.recyclerViewListClicked(view, position);
//        if(!this.container.getCurrentPage().equals(AppConst.ViewPager.Type.SEARCH))
//            this.onNavigationDrawerItemSelected(NavFragment.CategoryType.SEARCH);
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (this.drawer.isOpened()) this.drawer.close();
        else if (this.navigator.back()) terminate = false;
        else if (terminate) super.onBackPressed();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
            terminate = true;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Timber.d("expand3 : %s?%s", view.getClass().toString(), hasFocus);
        if(view == searchView) {
            search.expandResult(false);
        }else if(view == searchResult){
            // TODO : implement it!
        }
        this.subscriptions.add(
            this.search.autoComplete(
                (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)
            )
        );
    }

    /* Menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.searchview, menu);
        this.searchitem = menu.findItem(R.id.menu_search);
        this.searchView = (SearchView) searchitem.getActionView();
        this.searchitem.expandActionView();
        if(searchView != null){
            searchView.setQueryHint("?");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if(searchManager != null){
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }
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
//        this.searchitem.setOnMenuItemClickListener(this);
        searchitem.collapseActionView();
//        searchView.setOnClickListener(this);
        searchView.setOnQueryTextFocusChangeListener(this);
        searchResult.setOnFocusChangeListener(this);

        if (!this.drawer.isOpened()) {
            this.getMenuInflater().inflate(R.menu.main, menu);
            this.restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search){
            Timber.i("Ready to user searchView");
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        this.navigator.navigate(target, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, FragmentNavigator.AnimatorType animatorType) {
        this.navigator.navigate(target, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.navigator.navigate(target, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, FragmentNavigator.AnimatorType animatorType, boolean clear) {
        this.navigator.navigate(target, addToBackStack, animatorType, clear);
    }

    @Override
    public String getBackStackNameAt(int index) {
        return this.navigator.getBackStackNameAt(index);
    }

    @Override
    public boolean back() {
        return this.navigator.back();
    }
}
