package com.montserrat.parts.main;

import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {
    public MainFragment() {
    }
//
//    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
//    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
//    private NavCallback callbacks;
//    private ActionBarDrawerToggle navToggle;
//    private DrawerLayout navLayout;
//    private ListView navListView;
//    private View fragmentContainerView;
//
//    private int currentSelectedPosition = 0;
//    private boolean fromSavedInstanceState;
//    private boolean isUserLeardedNav;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
//        this.isUserLeardedNav = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
//
//        if (savedInstanceState != null) {
//            this.currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
//            this.fromSavedInstanceState = true;
//        }
//
//        this.selectItem(currentSelectedPosition);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        this.setHasOptionsMenu(true);
//    }
//
//    private ArrayList<MainItem> mainItems;
//    private MainAdapter navAdapter;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        this.navListView = (ListView) inflater.inflate(R.layout.nav_fragment, container, false).findViewById(R.id.main_listview);
//        this.navListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MainFragment.this.selectItem(position);
//            }
//        });
//        Random random = new Random();
//        this.mainItems = new ArrayList<>();
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject1), this.getString(R.string.main_dummy_professor1), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject2), this.getString(R.string.main_dummy_professor2), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject3), this.getString(R.string.main_dummy_professor3), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject4), this.getString(R.string.main_dummy_professor4), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject5), this.getString(R.string.main_dummy_professor5), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject6), this.getString(R.string.main_dummy_professor6), random.nextFloat() * 5));
//        this.mainItems.add(new MainItem(this.getString(R.string.main_dummy_subject7), this.getString(R.string.main_dummy_professor7), random.nextFloat() * 5));
//        this.navAdapter = new MainAdapter(this.getActionBar().getThemedContext(), R.layout.nav_item, this.mainItems);
//        this.navListView.setAdapter(this.navAdapter);
//        this.navListView.setItemChecked(this.currentSelectedPosition, true);
//        return this.navListView;
//    }
//
//    public boolean isDrawerOpen() {
//        return this.navLayout != null && this.navLayout.isDrawerOpen(this.fragmentContainerView);
//    }
//
//    public void setUp(int fragmentId, DrawerLayout navLayout) {
//        this.fragmentContainerView = this.getActivity().findViewById(fragmentId);
//        this.navLayout = navLayout;
//        this.navLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//        ActionBar actionBar = this.getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeButtonEnabled(true);
//        this.navToggle = new ActionBarDrawerToggle(
//                getActivity(),
//                navLayout,
//                R.drawable.ic_drawer,
//                R.string.app_name, // for open
//                R.string.app_name  // for close. But dunno what these strings do.
//        ) {
//            @Override
//            public void onDrawerClosed(View navView) {
//                super.onDrawerClosed(navView);
//                if (!isAdded()) return;
//                MainFragment.this.getActivity().supportInvalidateOptionsMenu();
//            }
//
//            @Override
//            public void onDrawerOpened(View navView) {
//                super.onDrawerClosed(navView);
//                if (!isAdded()) return;
//                if (!MainFragment.this.isUserLeardedNav) {
//                    MainFragment.this.isUserLeardedNav = true;
//                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainFragment.this.getActivity());
//                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
//                }
//                MainFragment.this.getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
//            }
//        };
//
//        if (!this.isUserLeardedNav && !this.fromSavedInstanceState)
//            this.navLayout.openDrawer(fragmentContainerView);
//
//        this.navLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                MainFragment.this.navToggle.syncState();
//            }
//        });
//        this.navLayout.setDrawerListener(this.navToggle);
//    }
//
//    private void selectItem(int position) {
//        this.currentSelectedPosition = position;
//        if (this.navListView != null) this.navListView.setItemChecked(position, true);
//        if (this.navLayout != null) this.navLayout.closeDrawer(this.fragmentContainerView);
//        if (this.callbacks != null) this.callbacks.onNavItemSelected(position);
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            this.callbacks = (NavCallback) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException("Activity must implement NavCallback.");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        this.callbacks = null;
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt(STATE_SELECTED_POSITION, this.currentSelectedPosition);
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        this.navToggle.onConfigurationChanged(newConfig);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        if (this.navLayout != null && this.isDrawerOpen()) {
//            inflater.inflate(R.menu.global, menu);
//            this.showGlobalContextActionBar();
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // navigation first
//        if (this.navToggle.onOptionsItemSelected(item)) return true;
//        // actionbar second
//        switch (item.getItemId()) {
//            case R.id.action_search:
//                Toast.makeText(this.getActivity(), "TODO : Transition to editText on ActionBar", Toast.LENGTH_SHORT).show();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    /** Per the navigation drawer design guidelines, updates the action bar to show the global app 'context', rather than just what's in the current screen. */
//    private void showGlobalContextActionBar() {
//        ActionBar actionBar = this.getActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setTitle(R.string.app_name);
//    }
//
//    private ActionBar getActionBar() {
//        return ((ActionBarActivity) this.getActivity()).getSupportActionBar();
//    }
//
//    public static interface NavCallback {
//        void onNavItemSelected(int position);
//    }
}