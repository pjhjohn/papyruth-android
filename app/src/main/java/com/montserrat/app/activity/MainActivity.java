package com.montserrat.app.activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.unique.Search;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.Search.AutoCompletableSearchView;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;
import com.montserrat.utils.view.viewpager.ViewPagerContainerManager;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavFragment.OnCategoryClickListener, ViewPagerContainerController, RecyclerViewClickListener, View.OnFocusChangeListener {
    private NavFragment drawer;
    private FlexibleViewPager viewpager;
    private ViewPagerContainerManager container;

    private CompositeSubscription subscriptions;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.query_result_outside) protected View outsideResult;

    private AutoCompletableSearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.viewpager = (FlexibleViewPager) this.findViewById(R.id.main_viewpager);
        this.drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setUp(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        /* Instantiate Multiple ViewPagerManagers */
        this.container = ViewPagerContainerManager.newInstance(this.viewpager, this.getFragmentManager());
        this.container.setCurrentPage(Page.at(AppConst.ViewPager.Type.HOME, AppConst.ViewPager.Home.HOME), false);

        this.subscriptions = new CompositeSubscription();

        this.search = new AutoCompletableSearchView(this, this);
        this.search.autoCompleteSetup(this.searchResult, this.outsideResult);

        viewpager.setOnFocusChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }

    @Override
    public void onCategorySelected (int category) {
        this.terminate = false;
        this.drawer.setActiveCategory(category);
        this.container.activate(AppConst.ViewPager.int2Type(category), true);
    }

    public int getActionbarHeight() {
        return this.getSupportActionBar().getHeight();
    }

    private MenuItem searchitem;
    private SearchView searchView;


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

        if (!this.drawer.isDrawerOpen()) {
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

    public void restoreActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("");
    }

    @Override
    public Stack<Page> getHistoryCopy() {
        return this.container.getHistoryCopy();
    }

    @Override
    public Page getPreviousPage() {
        return this.container.getPreviousPage();
    }

    @Override
    public void setCurrentPage(Page page, boolean addToBackStack) {
        this.terminate = false;
        this.container.setCurrentPage(page, addToBackStack);
    }

    @Override
    public boolean popCurrentPage () {
        this.terminate = false;
        return this.container.popCurrentPage();
    }

    @Override
    public boolean onBack() {
        return this.container.onBack();
    }

    //searchview Listener

    public boolean onQueryTextSubmit(String query) {
        Search.getInstance().clear().setQuery(query);
        Timber.d("submit query2 : %s", query);
        this.search.submit(query);

//        ((EditText)searchView.findViewById(R.id.search_src_text)).focus
        searchView.clearFocus();
        searchResult.clearFocus();
        this.search.expandResult(false);
        this.onCategorySelected(NavFragment.CategoryType.SEARCH);
        return false;
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        this.search.recyclerViewListClicked(view, position);
        if(!this.container.getCurrentPage().equals(AppConst.ViewPager.Type.SEARCH))
            this.onCategorySelected(NavFragment.CategoryType.SEARCH);
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (!this.container.onBack()) {
            if(terminate) super.onBackPressed();
            else {
                Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
                terminate = true;
            }
        } else terminate = false;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Timber.d("expand3 : %s?%s", v.getClass().toString(), hasFocus);
        if(v == searchView) {
            search.expandResult(false);
        }else if(v == searchResult){

        }
        this.subscriptions.add(
                this.search.autoComplete(
                        (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)
                )
        );
    }

}
