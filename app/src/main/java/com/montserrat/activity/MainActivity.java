package com.montserrat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.montserrat.parts.main.MainAdapter;
import com.montserrat.parts.main.MainItem;
import com.montserrat.parts.nav.NavFragment;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends ActionBarActivity implements NavFragment.NavCallback {
    private NavFragment nav;
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.nav = (NavFragment) this.getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        this.title = this.getTitle();
        this.nav.setUp(R.id.navigation_drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout));

        this.startActivity(new Intent(this, AuthActivity.class));
    }

    @Override
    public void onNavItemSelected(int position) {
        this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
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
        actionBar.setTitle(title);
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

    public static class PlaceholderFragment extends Fragment {
        public PlaceholderFragment() {}

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        ListView mainListView;
        ArrayList<MainItem> mainItems;
        MainAdapter mainAdapter;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            View root = inflater.inflate(R.layout.main_fragment, container, false);
            Log.i("DEBUG", ""+container.getId());
            View mainFragment = inflater.inflate(R.layout.main_fragment, container, false);
            this.mainListView = (ListView) mainFragment.findViewById(R.id.main_listview);
            this.mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // do something
                }
            });
            Random random = new Random();
            this.mainItems = new ArrayList<>();
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject1), this.getString(R.string.main_dummy_professor1), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject2), this.getString(R.string.main_dummy_professor2), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject3), this.getString(R.string.main_dummy_professor3), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject4), this.getString(R.string.main_dummy_professor4), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject5), this.getString(R.string.main_dummy_professor5), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject6), this.getString(R.string.main_dummy_professor6), random.nextFloat() * 5));
            this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject7), this.getString(R.string.main_dummy_professor7), random.nextFloat() * 5));
            this.mainAdapter = new MainAdapter(this.mainListView.getContext(), R.layout.main_item, this.mainItems);
            this.mainListView.setAdapter(this.mainAdapter);
            return mainFragment;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(this.getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
