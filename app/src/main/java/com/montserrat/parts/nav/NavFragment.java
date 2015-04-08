package com.montserrat.parts.nav;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.montserrat.activity.R;

import java.util.ArrayList;

public class NavFragment extends Fragment {
    public NavFragment() {
    }

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private NavCallback callbacks;
    private ActionBarDrawerToggle navToggle;
    private DrawerLayout navLayout;
    private ListView navListView;
    private View fragmentContainerView;

    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean isUserLeardedNav;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        this.isUserLeardedNav = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            this.currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            this.fromSavedInstanceState = true;
        }

        this.selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    private ArrayList<NavItem> navItems;
    private NavAdapter navAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.navListView = (ListView) inflater.inflate(R.layout.nav_fragment, container, false);
        this.navListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavFragment.this.selectItem(position);
            }
        });
        this.navItems = new ArrayList<>();
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_home) , R.drawable.ic_action_view_as_grid));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_search), R.drawable.ic_action_search));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_recommendation), R.drawable.ic_action_location_searching));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_write), R.drawable.ic_action_edit));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_random), R.drawable.ic_action_shuffle));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_profile), R.drawable.ic_action_settings));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_time), R.drawable.ic_action_time));
        this.navItems.add(new NavItem(this.getString(R.string.nav_item_signout), R.drawable.ic_action_remove));

        this.navAdapter = new NavAdapter(this.getActionBar().getThemedContext(), R.layout.nav_item, this.navItems);

        this.navListView.setAdapter(this.navAdapter);
        this.navListView.setItemChecked(this.currentSelectedPosition, true);
        return this.navListView;
    }

    public boolean isDrawerOpen() {
        return this.navLayout != null && this.navLayout.isDrawerOpen(this.fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout navLayout) {
        this.fragmentContainerView = this.getActivity().findViewById(fragmentId);
        this.navLayout = navLayout;
        this.navLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.navToggle = new ActionBarDrawerToggle(
                getActivity(),
                navLayout,
                R.drawable.ic_drawer,
                R.string.app_name, // for open
                R.string.app_name  // for close. But dunno what these strings do.
        ) {
            @Override
            public void onDrawerClosed(View navView) {
                super.onDrawerClosed(navView);
                if (!isAdded()) return;
                NavFragment.this.getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View navView) {
                super.onDrawerClosed(navView);
                if (!isAdded()) return;
                if (!NavFragment.this.isUserLeardedNav) {
                    NavFragment.this.isUserLeardedNav = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NavFragment.this.getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                NavFragment.this.getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        if (!this.isUserLeardedNav && !this.fromSavedInstanceState)
            this.navLayout.openDrawer(fragmentContainerView);

        this.navLayout.post(new Runnable() {
            @Override
            public void run() {
                NavFragment.this.navToggle.syncState();
            }
        });
        this.navLayout.setDrawerListener(this.navToggle);
    }

    private void selectItem(int position) {
        this.currentSelectedPosition = position;
        if (this.navListView != null) this.navListView.setItemChecked(position, true);
        if (this.navLayout != null) this.navLayout.closeDrawer(this.fragmentContainerView);
        if (this.callbacks != null) this.callbacks.onNavItemSelected(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.callbacks = (NavCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavCallback.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, this.currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.navToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.navLayout != null && this.isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            this.showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // navigation first
        if (this.navToggle.onOptionsItemSelected(item)) return true;
        // actionbar second
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(this.getActivity(), "TODO : Transition to editText on ActionBar", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Per the navigation drawer design guidelines, updates the action bar to show the global app 'context', rather than just what's in the current screen. */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) this.getActivity()).getSupportActionBar();
    }

    public static interface NavCallback {
        void onNavItemSelected(int position);
    }
}