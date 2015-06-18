package com.montserrat.app.navigation_drawer;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.ProfileFragment;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.picasso.CircleTransformation;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallback {
    private static final String STATE_SELECTED_POSITION = "selected_nav_position";
    private static final String PREF_USER_LEARNED_DRAWER = "user_learned_nav";
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = AppManager.getInstance().getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @InjectView(R.id.subtitle) protected RelativeLayout mSubtitle;
    @InjectView(R.id.subtitle_nickname) protected TextView mSubtitleNickname;
    @InjectView(R.id.subtitle_email) protected TextView mSubtitleEmail;
    @InjectView(R.id.subtitle_avatar) protected ImageView mSubtitleAvatar;
    @InjectView(R.id.nav_recyclerview) protected RecyclerView mDrawerList;
    private NavigationDrawerAdapter mNavigationDrawerAdapter;
    private List<NavigationDrawerItem> mNavigationDrawerItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.inject(this, view);
        /* subtitle */
        mSubtitle.setOnClickListener(subtitleView -> {
            mNavigator.navigate(ProfileFragment.class, true);
            if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
        });

        /* recyclerview items */
        mNavigationDrawerItems = new ArrayList<>();
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_home), R.drawable.ic_light_home));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_search), R.drawable.ic_light_search));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_recommendation), R.drawable.ic_light_recommend));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_evaluation), R.drawable.ic_light_new_evaluation));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_random), R.drawable.ic_light_random));

        mNavigationDrawerAdapter = new NavigationDrawerAdapter(this.getActivity(), this.mNavigationDrawerItems);
        mNavigationDrawerAdapter.setClickCategoryCallback(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);
        mDrawerList.setAdapter(this.mNavigationDrawerAdapter);
        this.select(mCurrentSelectedPosition, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    private NavigationDrawerCallback mCallbacks;
    private Navigator mNavigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallback) activity;
            mNavigator = (Navigator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement OnClickCategory");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mNavigator = null;
    }

    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    public void setup(int fragment_id, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = (View) this.getActivity().findViewById(fragment_id).getParent();
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setStatusBarBackground(R.color.bg_normal);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this.getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View navView) {
                super.onDrawerClosed(navView);
                if (!NavigationDrawerFragment.this.isAdded()) return;
                NavigationDrawerFragment.this.getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View navView) {
                super.onDrawerClosed(navView);
                if (!NavigationDrawerFragment.this.isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    AppManager.getInstance().putBoolean(PREF_USER_LEARNED_DRAWER, true);
                }
                NavigationDrawerFragment.this.getActivity().invalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) mDrawerLayout.openDrawer(mFragmentContainerView);

        mDrawerLayout.post(mActionBarDrawerToggle::syncState);
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    /* Drawer Actions */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean navigate) {
        this.select(position, navigate);
    }
    public void select(int position, boolean navigate) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
        if (mCallbacks != null && navigate) mCallbacks.onNavigationDrawerItemSelected(position, true);
        if (mDrawerList.getAdapter() != null) ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }
    public boolean isOpened() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    public void open() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }
    public void close() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }
    public void update() {
        mSubtitleNickname.setText(User.getInstance().getNickname());
        mSubtitleNickname.setPaintFlags(mSubtitleNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mSubtitleEmail.setText(User.getInstance().getEmail());
        final String avatarUrl = User.getInstance().getAvatarUrl();
        if (avatarUrl != null) Picasso.with(this.getActivity()).load(avatarUrl).transform(new CircleTransformation()).into(mSubtitleAvatar);
        else Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).transform(new CircleTransformation()).into(mSubtitleAvatar);
    }

    /* Menu */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) return true;
        switch (item.getItemId()) {
            case R.id.menu_search:
                // TODO : Transition to editText on ActionBar
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}