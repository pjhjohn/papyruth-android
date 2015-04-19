package com.montserrat.parts.nav;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.montserrat.activity.R;
import com.montserrat.utils.adapter.UniversalAdapter;

import java.util.ArrayList;
import java.util.List;

public class NavFragment extends Fragment {
    public NavFragment() {}

    private int iActiveNavItem;
    private NavCallback callback;
    private View fragmentContainerView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initially, no SharedPreference used, set drawer initial index to ZERO */
        /*
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        this.isUserLeardedNav = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            this.currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            this.fromSavedInstanceState = true;
        }
        */
        this.selectItem(this.iActiveNavItem = 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    private ArrayList<NavListItemView> navItems;
    private UniversalAdapter navAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav, container, false);
        final ListView listview = (ListView) view.findViewById(R.id.nav_listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listview.setItemChecked(position, true);
                NavFragment.this.selectItem(position);
            }
        });

        List<NavListItemView> items = new ArrayList<NavListItemView>();
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_home) , R.drawable.ic_action_view_as_grid)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_search), R.drawable.ic_action_search)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_recommendation), R.drawable.ic_action_location_searching)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_write), R.drawable.ic_action_edit)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_random), R.drawable.ic_action_shuffle)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_profile), R.drawable.ic_action_settings)));
        items.add(new NavListItemView( new NavListItemView.Data(this.getString(R.string.nav_item_signout), R.drawable.ic_action_remove)));

        listview.setAdapter(new UniversalAdapter(items, this.getActivity()));
        listview.setItemChecked(this.iActiveNavItem, true);
        return view;
    }

    public boolean isDrawerOpen() {
        return this.drawerLayout != null && this.drawerLayout.isDrawerOpen(this.fragmentContainerView);
    }

    public void setUp(int fragment_id, DrawerLayout drawerLayout) {
        this.fragmentContainerView = this.getActivity().findViewById(fragment_id);
        this.drawerLayout = drawerLayout;
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        this.drawerToggle = new ActionBarDrawerToggle(this.getActivity(), drawerLayout, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View navView) {
                super.onDrawerClosed(navView);
                if (NavFragment.this.isAdded()) NavFragment.this.getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View navView) {
                super.onDrawerClosed(navView);
                if (NavFragment.this.isAdded()) NavFragment.this.getActivity().invalidateOptionsMenu();
            }
        };

        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                NavFragment.this.drawerToggle.syncState();
            }
        });
        this.drawerLayout.setDrawerListener(this.drawerToggle);
    }
    private void selectItem(int position) {
        this.iActiveNavItem = position;
        if (this.drawerLayout != null) this.drawerLayout.closeDrawer(this.fragmentContainerView);
        if (this.callback != null) this.callback.onNavItemSelected(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.callback = (NavCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavCallback.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // navigation first
        if (this.drawerToggle.onOptionsItemSelected(item)) return true;
        // actionbar second
        switch (item.getItemId()) {
            case R.id.menu_search:
                Toast.makeText(this.getActivity(), "TODO : Transition to editText on ActionBar", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
////        if (this.drawerLayout != null && this.isDrawerOpen()) {
////            inflater.inflate(R.menu.global, menu);
////            this.showGlobalContextActionBar();
////        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }



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