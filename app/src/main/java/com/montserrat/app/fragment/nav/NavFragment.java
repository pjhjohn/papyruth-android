package com.montserrat.app.fragment.nav;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.adapter.NavAdapter;
import com.montserrat.app.model.User;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

public class NavFragment extends Fragment implements RecyclerViewClickListener{
    private int iActiveCategory;
    private OnCategoryClickListener callback;
    private View fragmentContainerView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerView;
    private NavAdapter adapter;
    private List<NavAdapter.Holder.Data> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initially, with no SharedPreference used, set drawer initial index to ZERO */
        /*
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        this.isUserLeardedNav = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            this.currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            this.fromSavedInstanceState = true;
        }
        */
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    /**
     * TODO : Synchronize Category with Data initializatio in onCreateView
     */
    public static String stringify(int category) {
        switch(category) {
            case Category.HOME : return "HOME";
            case Category.SEARCH : return "SEARCH";
            case Category.SUGGEST : return "RECOMMENDATION";
            case Category.EVALUATION: return "EVALUAION";
            case Category.RANDOM : return "RANDOM";
            case Category.PROFILE : return "PROFILE";
            case Category.SIGNOUT : return "SIGNOUT";
            default : return "<UNASSIGNED>";
        }
    }
    public static final class Category {
        public static final int HOME    = 0;
        public static final int SEARCH  = 1;
        public static final int SUGGEST = 2;
        public static final int EVALUATION = 3;
        public static final int RANDOM  = 4;
        public static final int PROFILE = 5;
        public static final int SIGNOUT = 6;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav, container, false);
        /* views */
        ((TextView) view.findViewById(R.id.user_email)).setText(User.getInstance().getEmail() == null ? this.getResources().getString(R.string.email_default) : User.getInstance().getEmail());

        /* items */
        this.items = new ArrayList<>();
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_home) , R.drawable.ic_action_view_as_grid));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_search), R.drawable.ic_action_search));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_recommendation), R.drawable.ic_action_location_searching));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_evaluation), R.drawable.ic_action_edit));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_random), R.drawable.ic_action_shuffle));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_profile), R.drawable.ic_action_settings));
        this.items.add(new NavAdapter.Holder.Data(this.getString(R.string.nav_item_signout), R.drawable.ic_action_remove));

        /* adapter */
        this.adapter = NavAdapter.newInstance(this.items, this);

        /* recyclerview */
        this.recyclerView = (RecyclerView) view.findViewById(R.id.nav_recyclerview);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.recyclerView.setAdapter(this.adapter);

        return view;
    }

    public void setActiveCategory(int category) {
        this.iActiveCategory = category;
    }

    public int getActiveCategory() {
        return this.iActiveCategory;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        this.iActiveCategory = position;
        if (this.drawerLayout != null) this.drawerLayout.closeDrawer(this.fragmentContainerView);
        if (this.callback != null) this.callback.onCategorySelected(position);
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

        this.drawerLayout.post(this.drawerToggle::syncState);
        this.drawerLayout.setDrawerListener(this.drawerToggle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.callback = (OnCategoryClickListener) activity;
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
                // TODO : Transition to editText on ActionBar
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

    public static interface OnCategoryClickListener {
        void onCategorySelected (int category);
    }
}