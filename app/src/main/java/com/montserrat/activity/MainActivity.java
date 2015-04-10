package com.montserrat.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.montserrat.parts.FragmentFactory;
import com.montserrat.parts.nav.NavFragment;

public class MainActivity extends ActionBarActivity implements NavFragment.NavCallback {
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
    }
}
