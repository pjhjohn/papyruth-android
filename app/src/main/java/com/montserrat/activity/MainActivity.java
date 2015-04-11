package com.montserrat.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {// implements NavFragment.NavCallback {

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);

        this.setSupportActionBar(toolbar); /* toolbar acts like a ActionBar */

        this.toggle = new ActionBarDrawerToggle(this, this.drawer, R.string.app_name, R.string.app_name);
        this.drawer.setDrawerListener(this.toggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.toggle.onOptionsItemSelected(item)) return true;
        else return super.onOptionsItemSelected(item);
    }

    /*
    private NavFragment navigation_drawer;
    private FrameLayout container;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.navigation_drawer = (NavFragment) this.getFragmentManager().findFragmentById(R.id.navigation_drawer);
        this.container = (FrameLayout) this.findViewById(R.id.activity_main_container);

        this.navigation_drawer.setUp(R.id.navigation_drawer, (DrawerLayout) this.findViewById(R.id.activity_main_nav));

//        this.title = this.getTitle();
    }

    @Override
    public void onNavItemSelected(int position) {
        this.getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_container, FragmentFactory.create(FragmentFactory.Type.MAIN, position))
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(this.title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!this.navigation_drawer.isDrawerOpen()) {
            this.getMenuInflater().inflate(R.menu.main, menu);
            this.restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) return true;
        return super.onOptionsItemSelected(item);
    }*/
}
