package com.papyruth.android.navigation_drawer;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppManager;
import com.papyruth.android.PapyruthApplication;
import com.papyruth.android.R;
import com.papyruth.android.fragment.main.ProfileFragment;
import com.papyruth.android.model.unique.User;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.search.SearchToolbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallback {
    private NavigationDrawerCallback mNavigationDrawerCallback;
    private Navigator mNavigator;
    private Activity mActivity;
    private Tracker mTracker;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mTracker  = ((PapyruthApplication) activity.getApplicationContext()).getTracker();
        try {
            mNavigationDrawerCallback = (NavigationDrawerCallback) activity;
            mNavigator = (Navigator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement OnClickCategory");
        }
        if(User.getInstance().getUniversityId() != null && User.getInstance().getUniversityId() == 1) {
            mHeaderBackgroundDrawableRes = R.drawable.dummy_nav_university;
        }
    }

    private static final String SELECTED_POSITION   = "NavigationDrawerFragment.SelectedPosition";
    private static final String USER_LEARNED_DRAWER = "NavigationDrawerFragment.UserLearnedDrawer";
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition = 0;
    private View.OnClickListener mNavigationPriorClickListener;
    private int mHeaderBackgroundDrawableRes = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = AppManager.getInstance().getBoolean(USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
            mCurrentSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }
    }

    @Bind(R.id.navigation_drawer_header)            protected RelativeLayout mHeader;
    @Bind(R.id.navigation_drawer_header_background) protected ImageView mHeaderBackground;
    @Bind(R.id.navigation_drawer_header_nickname)   protected TextView mUserNickname;
    @Bind(R.id.navigation_drawer_header_email)      protected TextView mUserEmail;
    @Bind(R.id.navigation_drawer_header_avatar)     protected ImageView mUserAvatar;
    @Bind(R.id.navigation_drawer_recyclerview)      protected RecyclerView mNavigationRecyclerView;
    @Bind(R.id.navigation_drawer_contact_us)        protected FrameLayout mContactUs;
    private NavigationDrawerAdapter mNavigationDrawerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.bind(this, view);

        mHeader.setOnClickListener(subtitleView -> {
            mNavigator.navigate(ProfileFragment.class, true);
            if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
        });

        /* recyclerview items */
        List<NavigationDrawerItem> mNavigationDrawerItems = new ArrayList<>();
        mNavigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.navigation_drawer_item_home),          R.drawable.ic_latest_evaluation_24dp));
        mNavigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.navigation_drawer_item_compose_evaluation),R.drawable.ic_new_evaluation_24dp));
        mNavigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.navigation_drawer_item_favorite),      R.drawable.ic_favorite_24dp));
        mNavigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.navigation_drawer_item_my_evaluation), R.drawable.ic_my_evaluations_24dp));
        mNavigationDrawerItems.add(new NavigationDrawerItem(getString(R.string.navigation_drawer_item_my_comment),    R.drawable.ic_my_comments_24dp));

        mNavigationDrawerAdapter = new NavigationDrawerAdapter(mActivity, mNavigationDrawerItems);
        mNavigationDrawerAdapter.setClickCategoryCallback(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mNavigationRecyclerView.setLayoutManager(layoutManager);
        mNavigationRecyclerView.setHasFixedSize(true);
        mNavigationRecyclerView.setAdapter(mNavigationDrawerAdapter);
        selectDrawerItem(mCurrentSelectedPosition, false);

        Picasso.with(getActivity()).load(R.drawable.ic_email_24dp).transform(new ColorFilterTransformation(getResources().getColor(R.color.icon_material))).into(((ImageView) mContactUs.findViewById(R.id.navigation_drawer_item_icon)));
        ((TextView) mContactUs.findViewById(R.id.navigation_drawer_item_label)).setText(R.string.navigation_drawer_contact_us);
        mContactUs.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, Uri.parse(getString(R.string.contact_email)));
            Intent chooser = Intent.createChooser(emailIntent, "email");
            startActivity(chooser);
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_POSITION, mCurrentSelectedPosition);
    }

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = (View) mActivity.findViewById(fragmentId).getParent();
        mDrawerLayout = drawerLayout;
        DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(SearchToolbar.getInstance().isOpened())
                    SearchToolbar.getInstance().hide();
                if (!NavigationDrawerFragment.this.isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    AppManager.getInstance().putBoolean(USER_LEARNED_DRAWER, true);
                }
                mTracker.send(new HitBuilders.EventBuilder().setAction(getString(R.string.navigation_drawer_open)).setCategory(getString(R.string.ga_category_drawer)).build());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mTracker.send(new HitBuilders.EventBuilder().setAction(getString(R.string.navigation_drawer_close)).setCategory(getString(R.string.ga_category_drawer)).build());
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerListener);

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) mDrawerLayout.openDrawer(mFragmentContainerView);
        else mDrawerLayout.closeDrawer(mFragmentContainerView);

        toolbar.setNavigationOnClickListener(view -> {
            if(mNavigationPriorClickListener == null) {
                FloatingActionControl.getInstance().closeMenuButton(true);
                this.open();
            } else mNavigationPriorClickListener.onClick(view);
        });

        /* setup Subtitle */
        mUserNickname.setPaintFlags(mUserNickname.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        User.getInstance().getNicknameObservable().subscribe(mUserNickname::setText, error -> ErrorHandler.handle(error, this));
        User.getInstance().getEmailObservable().subscribe(mUserEmail::setText, error -> ErrorHandler.handle(error, this));
    }

    public void setOnNavigationIconClickListener(View.OnClickListener listener) {
        mNavigationPriorClickListener = listener;
    }

    /* Drawer Actions */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean navigate) {
        selectDrawerItem(position, navigate);
    }
    public void selectDrawerItem(int position, boolean navigate) {
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(mFragmentContainerView);
        if (mNavigationDrawerCallback != null && navigate) mNavigationDrawerCallback.onNavigationDrawerItemSelected(mNavigationDrawerAdapter.getItemsPosition(position), true);
        if (mNavigationRecyclerView.getAdapter() != null) ((NavigationDrawerAdapter) mNavigationRecyclerView.getAdapter()).selectPosition(position);
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
        if (avatarUrl != null) Picasso.with(mActivity).load(avatarUrl).transform(new CircleTransformation()).into(mUserAvatar);
        if(mHeaderBackgroundDrawableRes != 0) Picasso.with(getActivity()).load(mHeaderBackgroundDrawableRes).into(mHeaderBackground);
    }

    /* Menu : TODO - What exactly this does? */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search : return true;
            default : return super.onOptionsItemSelected(item);
        }
    }
}