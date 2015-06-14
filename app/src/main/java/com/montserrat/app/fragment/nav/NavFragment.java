package com.montserrat.app.fragment.nav;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Paint;
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
import com.montserrat.app.model.Category;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NavFragment extends Fragment implements RecyclerViewClickListener {
    private int activeCategory;
    public static class CategoryType {
        public static final int HOME            = 0;
        public static final int SEARCH          = 1;
        public static final int RECOMMENDATION  = 2;
        public static final int EVALUATION      = 3;
        public static final int RANDOM          = 4;
        public static final int PROFILE         = 5;
        public static final int SIGNOUT         = 6;
    }

    @InjectView(R.id.subtitle_nickname) protected TextView subtitle_nickname;
    @InjectView(R.id.subtitle_email) protected TextView subtitle_email;
    @InjectView(R.id.nav_recyclerview) protected RecyclerView recyclerView;
    private NavAdapter adapter;
    private List<Category> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nav, container, false);
        ButterKnife.inject(this, view);

        /* views */
        this.subtitle_nickname.setText(User.getInstance().getNickname());
        this.subtitle_nickname.setPaintFlags(this.subtitle_nickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        this.subtitle_email.setText(User.getInstance().getEmail());

        /* items */
        this.items = new ArrayList<>();
        this.items.add(new Category(this.getString(R.string.nav_item_home) , R.drawable.ic_light_home));
        this.items.add(new Category(this.getString(R.string.nav_item_search), R.drawable.ic_light_search));
        this.items.add(new Category(this.getString(R.string.nav_item_recommendation), R.drawable.ic_light_recommend));
        this.items.add(new Category(this.getString(R.string.nav_item_evaluation), R.drawable.ic_light_new_evaluation));
        this.items.add(new Category(this.getString(R.string.nav_item_random), R.drawable.ic_light_random));
        this.items.add(new Category(this.getString(R.string.nav_item_profile), R.drawable.ic_light_setting));
        this.items.add(new Category(this.getString(R.string.nav_item_signout), R.drawable.ic_light_signout));

        /* adapter */
        this.adapter = new NavAdapter(this.getActivity(), this.items, this);

        /* recyclerview */
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.recyclerView.setAdapter(this.adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    private OnClickCategory callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (OnClickCategory) activity;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }

    private View container;
    private DrawerLayout navLayout;
    private ActionBarDrawerToggle navToggle;
    public void setUp(int fragment_id, DrawerLayout drawerLayout) {
        this.container = this.getActivity().findViewById(fragment_id);
        this.navLayout = drawerLayout;
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        this.getActionBar().setHomeButtonEnabled(true);
        this.navToggle = new ActionBarDrawerToggle(this.getActivity(), drawerLayout, R.string.app_name, R.string.app_name) {
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
        this.navLayout.post(this.navToggle::syncState);
        this.navLayout.setDrawerListener(this.navToggle);
    }

    public void setActiveCategory(int category) {
        this.activeCategory = category;
        // TODO : Background highlighting or something else
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        this.activeCategory = position;
        if (this.navLayout != null) this.navLayout.closeDrawer(this.container);
        if (this.callback != null) this.callback.onClickCategory(position, true);
    }

    public boolean isDrawerOpen() {
        return this.navLayout != null && this.navLayout.isDrawerOpen(this.container);
    }

    /* Menu */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.navToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.navToggle.onOptionsItemSelected(item)) return true;
        switch (item.getItemId()) {
            case R.id.menu_search:
                // TODO : Transition to editText on ActionBar
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

    public interface OnClickCategory {
        void onClickCategory(int category);
        void onClickCategory(int category, boolean fromUser);
    }
}