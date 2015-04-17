package com.montserrat.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.montserrat.parts.FragmentFactory;
import com.montserrat.parts.nav.NavFragment;

public class MainActivity extends ActionBarActivity implements NavFragment.NavCallback {

    private NavFragment drawer;
    private FrameLayout container;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        this.container = (FrameLayout) this.findViewById(R.id.container);
        this.drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        this.drawer.setUp(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        this.startActivity(new Intent(this, AuthActivity.class));
    }

    @Override
    public void onNavItemSelected(int position) {
        this.getFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentFactory.create(FragmentFactory.Type.MAIN, position))
                .commit();
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
}
