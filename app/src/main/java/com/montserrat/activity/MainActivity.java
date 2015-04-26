package com.montserrat.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.FragmentFactory;
import com.montserrat.parts.nav.NavFragment;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends ActionBarActivity implements NavFragment.NavCallback {
    private NavFragment drawer;
    private FlexibleViewPager viewpager;
    private String title;
    private List<ViewPagerManager> managers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.viewpager = (FlexibleViewPager) this.findViewById(R.id.main_viewpager);
        this.drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setUp(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        AppManager.getInstance().setContext(this);

        /* Instantiate Multiple ViewPagerManagers */
        this.managers = new ArrayList<> ();
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.HOME     , AppConst.ViewPager.Home.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.SEARCH   , AppConst.ViewPager.Search.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.SUGGEST  , AppConst.ViewPager.Suggest.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.WRITE    , AppConst.ViewPager.Write.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.RANDOM   , AppConst.ViewPager.Random.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.PROFILE  , AppConst.ViewPager.Profile.LENGTH));
        this.managers.add(new ViewPagerManager(this.viewpager, this.getFragmentManager(), FragmentFactory.Type.SIGNOUT  , AppConst.ViewPager.Signout.LENGTH));
        this.managers.get(0).active();
    }

    //
    @Override
    public void onNavItemSelected(int position) {
        this.managers.get(position).active();
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
            Log.i("Main-Search", "ready to use searchView***********");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(this.title);
    }


    //searchview Listener
    public class queryTextListner implements SearchView.OnQueryTextListener{

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        //자동완성 기능에 사용.
        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }
}
