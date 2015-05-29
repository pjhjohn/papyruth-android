package com.montserrat.app.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.AutoCompleteAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.response.AutoCompleteResponse;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavFragment.OnCategoryClickListener, ViewPagerController, RecyclerViewClickListener, SearchView.OnQueryTextListener, View.OnFocusChangeListener, View.OnClickListener {
    private NavFragment drawer;
    private FlexibleViewPager viewpager;
    private List<ViewPagerManager> managers;

    private CompositeSubscription subscriptions;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.search_result_outside) protected View outsideResult;

    private AutoCompleteAdapter adapter;
    private List<AutoCompleteResponse> autoCompleteResponses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainerView(this.fac);

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.viewpager = (FlexibleViewPager) this.findViewById(R.id.main_viewpager);
        this.drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setUp(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        /* Instantiate Multiple ViewPagerManagers */
        this.managers = new ArrayList<>();
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.HOME          , AppConst.ViewPager.Home.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.SEARCH        , AppConst.ViewPager.Search.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.RECOMMENDATION, AppConst.ViewPager.Recommendation.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.EVALUATION    , AppConst.ViewPager.Evaluation.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.RANDOM        , AppConst.ViewPager.Random.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.PROFILE       , AppConst.ViewPager.Profile.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), AppConst.ViewPager.Type.SIGNOUT       , AppConst.ViewPager.Signout.LENGTH));
        this.managers.get(0).active();
        for(ViewPagerManager manager : this.managers) manager.setSwipeEnabled(false);

        this.subscriptions = new CompositeSubscription();

        autoCompleteResponses = new ArrayList<>();
        adapter = AutoCompleteAdapter.newInstance(autoCompleteResponses, this);
        this.searchResult.setLayoutManager(new LinearLayoutManager(this));
        this.searchResult.setAdapter(this.adapter);
        viewpager.setOnFocusChangeListener(this);
        outsideResult.setOnFocusChangeListener(this);
        outsideResult.setOnClickListener(this);
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
        this.managers.get(category).active();
    }

    public int getActionbarHeight(){
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
            searchView.setOnQueryTextListener(this);
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
    public Stack<Integer> getHistoryCopy() {
        return this.managers.get(this.drawer.getActiveCategory()).getHistoryCopy();
    }

    @Override
    public int getPreviousPage() {
        return this.managers.get(this.drawer.getActiveCategory()).getPreviousPage();
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.terminate = false;
        this.managers.get(this.drawer.getActiveCategory()).setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public boolean popCurrentPage () {
        this.terminate = false;
        return this.managers.get(this.drawer.getActiveCategory()).popCurrentPage();
    }

    @Override
    public boolean onBack() {
        return this.managers.get(this.drawer.getActiveCategory()).onBack();
    }

    //searchview Listener

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // for text auto-completion
    @Override
    public boolean onQueryTextChange(String newText) {
        subscriptions.add(
                RetrofitApi.getInstance().autocomplete(newText)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(response -> response.results)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                results -> {
                                    autoCompleteResponses.clear();
                                    autoCompleteResponses.addAll(sampleData());
                                    adapter.notifyDataSetChanged();
                                    expandResult(true);
                                },
                                error -> {
                                    autoCompleteResponses.clear();
                                    autoCompleteResponses.addAll(sampleData());
                                    adapter.notifyDataSetChanged();
                                    expandResult(true);
                                }
                        )
        );

        return false;
    }

    private void expandResult(boolean expand){
        if(expand){
            ViewGroup.LayoutParams param =  searchResult.getLayoutParams();
            param.height = 250;
            param.width = (int)(this.getResources().getDisplayMetrics().widthPixels * 0.8);
            searchResult.setLayoutParams(param);

            param =  outsideResult.getLayoutParams();
            param.height = this.getResources().getDisplayMetrics().heightPixels;
            param.width = this.getResources().getDisplayMetrics().widthPixels;

            outsideResult.setLayoutParams(param);
        }else{
            ViewGroup.LayoutParams param =  searchResult.getLayoutParams();
            param.height = 0;
            param.width = (int)(this.getResources().getDisplayMetrics().widthPixels * 0.8);
            searchResult.setLayoutParams(param);

            param =  outsideResult.getLayoutParams();
            param.height = 0;
            param.width = this.getResources().getDisplayMetrics().widthPixels;

            outsideResult.setLayoutParams(param);
        }
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
        AutoCompleteResponse item = autoCompleteResponses.get(position);
        Timber.d("autocomplete : %s", position);
    }

    private List<AutoCompleteResponse> sampleData(){
        List<AutoCompleteResponse> list = new ArrayList<>();
        list.add(new AutoCompleteResponse("math", 1, null, null, null));
        list.add(new AutoCompleteResponse(null, null, "prof", 2, null));
        list.add(new AutoCompleteResponse("math", 1, null, null, null));
        list.add(new AutoCompleteResponse(null, null, "prof", 2, null));
        return list;
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (!this.managers.get(this.drawer.getActiveCategory()).onBack()) {
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
            expandResult(false);
        }else if(v == searchResult){

        }
    }

    @Override
    public void onClick(View v) {
        expandResult(false);
    }
}
