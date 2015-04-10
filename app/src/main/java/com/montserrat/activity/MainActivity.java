package com.montserrat.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.montserrat.parts.main.MainFragment;
import com.montserrat.parts.nav.NavFragment;

public class MainActivity extends ActionBarActivity implements NavFragment.NavCallback {
    private NavFragment nav;
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.nav = (NavFragment) this.getFragmentManager().findFragmentById(R.id.navigation_drawer);
        this.nav.setUp(R.id.navigation_drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));
        this.title = this.getTitle();

        /* Launch AuthActivity For Authentication */
//        this.startActivity(new Intent(this, AuthActivity.class));
    }

    @Override
    public void onNavItemSelected(int position) {
        this.getFragmentManager().beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1: title = this.getString(R.string.nav_item_home);             break;
            case 2: title = this.getString(R.string.nav_item_search);           break;
            case 3: title = this.getString(R.string.nav_item_recommendation);   break;
            case 4: title = this.getString(R.string.nav_item_write);            break;
            case 5: title = this.getString(R.string.nav_item_random);           break;
            case 6: title = this.getString(R.string.nav_item_profile);          break;
            case 7: title = this.getString(R.string.nav_item_time);             break;
            case 8: title = this.getString(R.string.nav_item_signout);          break;
            default: break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(this.title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!this.nav.isDrawerOpen()) {
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
