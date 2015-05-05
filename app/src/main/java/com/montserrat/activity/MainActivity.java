package com.montserrat.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.montserrat.controller.AppConst;
import com.montserrat.parts.FragmentFactory;
import com.montserrat.parts.navigation_drawer.NavFragment;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerManager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavFragment.OnCategoryClickListener, ViewPagerController {
    private NavFragment drawer;
    private FlexibleViewPager viewpager;
    private List<ViewPagerManager> managers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.viewpager = (FlexibleViewPager) this.findViewById(R.id.main_viewpager);
        this.drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setUp(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        /* Instantiate Multiple ViewPagerManagers */
        this.managers = new ArrayList<ViewPagerManager> ();
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.HOME     , AppConst.ViewPager.Home.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.SEARCH   , AppConst.ViewPager.Search.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.RECOMMENDATION, AppConst.ViewPager.Recommendation.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.EVALUAION, AppConst.ViewPager.Evaluation.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.RANDOM   , AppConst.ViewPager.Random.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.PROFILE  , AppConst.ViewPager.Profile.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.SIGNOUT  , AppConst.ViewPager.Signout.LENGTH));
        this.managers.get(0).active();
        for(ViewPagerManager manager : this.managers) manager.setSwipeEnabled(false);
    }

    //
    @Override
    public void onCategorySelected (int category) {
        this.terminate = false;
        this.drawer.setActiveCategory(category);
        this.managers.get(category).active();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.searchview, menu);


            MenuItem searchitem = menu.findItem(R.id.menu_search);
            SearchView searchView = (SearchView) searchitem.getActionView();
            if(searchView != null){
                searchView.setQueryHint("?");

                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                if(searchManager != null){
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                }
                searchView.setIconifiedByDefault(true);
                searchView.setOnQueryTextListener(new queryTextListner());

            }

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
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.terminate = false;
        this.managers.get(this.drawer.getActiveCategory()).setCurrentPage(pageNum, addToBackStack);
    }


    //searchview Listener
    public class queryTextListner implements SearchView.OnQueryTextListener{

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        // for text auto-completion
        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if(!this.managers.get(this.drawer.getActiveCategory()).onBackPressed()) {
            if(terminate) super.onBackPressed();
            else {
                Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
                terminate = true;
            }
        } else terminate = false;
    }
}
