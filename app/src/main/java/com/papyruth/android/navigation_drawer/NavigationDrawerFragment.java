package com.papyruth.android.navigation_drawer;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.ProfileFragment;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.utility.navigator.Navigator;
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
    private View.OnClickListener mNavigationPriorClickListener;

    Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
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
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_home), R.drawable.ic_light_latest_evaluation));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_new_evaluation), R.drawable.ic_light_new_evaluation));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_bookmark), R.drawable.ic_light_bookmark));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_my_evaluation), R.drawable.ic_light_my_evaluation));
        mNavigationDrawerItems.add(new NavigationDrawerItem(this.getString(R.string.nav_item_my_comment), R.drawable.ic_light_my_comment));


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

    private boolean isDrawerOpened;
    private DrawerLayout mDrawerLayout;
    private DrawerLayout.DrawerListener mDrawerListener;
    private View mFragmentContainerView;
    public void setup(int fragment_id, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = (View) this.getActivity().findViewById(fragment_id).getParent();
        mDrawerLayout = drawerLayout;
        mDrawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!NavigationDrawerFragment.this.isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    AppManager.getInstance().putBoolean(PREF_USER_LEARNED_DRAWER, true);
                }
                mTracker.send(
                    new HitBuilders.EventBuilder()
                        .setAction(getString(R.string.navigation_drawer_open))
                        .setCategory(getString(R.string.ga_category_drawer))
                        .build()
                );
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mTracker.send(
                    new HitBuilders.EventBuilder()
                        .setAction(getString(R.string.navigation_drawer_close))
                        .setCategory(getString(R.string.ga_category_drawer))
                        .build()
                );
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerListener);

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) mDrawerLayout.openDrawer(mFragmentContainerView);
        else mDrawerLayout.closeDrawer(mFragmentContainerView);

        toolbar.setNavigationOnClickListener(
            this::onClick
        );

        /* setup Subtitle */
        mSubtitleNickname.setPaintFlags(mSubtitleNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        User.getInstance().getNicknameObservable().subscribe(mSubtitleNickname::setText, error -> ErrorHandler.throwError(error, this));
        User.getInstance().getEmailObservable().subscribe(mSubtitleEmail::setText, error -> ErrorHandler.throwError(error, this));
    }

    public void onClick(View view){
        if(mNavigationPriorClickListener == null) {
            FloatingActionControl.getInstance().closeMenuButton(true);
            if (isDrawerOpened) this.close();
            else this.open();
        } else if(!isDrawerOpened) mNavigationPriorClickListener.onClick(view);

    }

    public void setOnNavigationIconClickListener(View.OnClickListener listener) {
        mNavigationPriorClickListener = listener;
    }

    /* Drawer Actions */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean navigate) {
        this.select(position, navigate);
    }
    public void select(int position, boolean navigate) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
        if (mCallbacks != null && navigate) mCallbacks.onNavigationDrawerItemSelected(mNavigationDrawerAdapter.getItemsPosition(position), true);
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
        final String avatarUrl = User.getInstance().getAvatarUrl();
        if (avatarUrl != null) Picasso.with(this.getActivity()).load(avatarUrl).transform(new CircleTransformation()).into(mSubtitleAvatar);
        else Picasso.with(this.getActivity()).load(R.drawable.avatar_dummy).transform(new CircleTransformation()).into(mSubtitleAvatar);
    }

    /* Menu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                // TODO : Transition to editText on ActionBar
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}