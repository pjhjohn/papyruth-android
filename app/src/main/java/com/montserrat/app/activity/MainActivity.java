package com.montserrat.app.activity;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.montserrat.utils.view.search.CustomSearchView;
import com.montserrat.utils.view.search.ToolbarSearch;

import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallback, RecyclerViewItemClickListener, Navigator {
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;


    private CompositeSubscription mCompositeSubscription;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.main_navigator) FrameLayout navigatorContainer;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.query_result_outside) protected View outsideResult;
    @InjectView(R.id.toolbar) protected Toolbar mToolbar;
    private AutoCompletableSearchView mAutoCompletableSearch;
    private MaterialMenuDrawable mMaterialMenuDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);
        mCompositeSubscription = new CompositeSubscription();
        mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        mToolbar.inflateMenu(R.menu.searchview);
        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_search) return true;
            return super.onOptionsItemSelected(item);
        });

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        mNavigationDrawer.setup(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout), mToolbar);
        mNavigationDrawer.update();

        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER);

        /* Instantiate Multiple ViewPagerManagers */
//        mAutoCompletableSearch = new AutoCompletableSearchView(this, this, AutoCompletableSearchView.Type.SEARCH);
        mAutoCompletableSearch = ToolbarSearch.getInstance().newSearchView(this,this,AutoCompletableSearchView.Type.SEARCH);
        ToolbarSearch.getInstance().setActivityComponent(this);
        mAutoCompletableSearch.initAutoComplete(this.searchResult, this.outsideResult);

        this.onInitializeMenuOnToolbar(mToolbar.getMenu());
    }
    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    private MenuItem searchitem;
    private CustomSearchView searchView;

    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        mAutoCompletableSearch.submit(query);
        ToolbarSearch.getInstance().search(true);
        return false;
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(searchResult != null && ((RecyclerView)view.getParent()).getId() == searchResult.getId())
            ToolbarSearch.getInstance().recyclerViewClicked(view, position, true);
        else
            ToolbarSearch.getInstance().recyclerViewClicked(view, position, false);
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        Timber.d("Back!! Pressed!!");
        if (this.mNavigationDrawer.isOpened()) this.mNavigationDrawer.close();
        else if(this.mAutoCompletableSearch.onBack()) ;
        else if (this.mNavigator.back()) terminate = false;
        else if (terminate) super.onBackPressed();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
            terminate = true;
        }
    }

    /* Menu */
    private boolean onInitializeMenuOnToolbar(Menu menu) {
        this.searchitem = menu.findItem(R.id.menu_search);
        this.searchView = (CustomSearchView) searchitem.getActionView();
        this.searchitem.expandActionView();
        this.searchView.setOnBackListener(()->{
            this.mAutoCompletableSearch.onBack();
            return true;
        });
        if(searchView != null){
            searchView.setQueryHint("Input Search Query");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if(searchManager != null) searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            ((EditText)searchView.findViewById(R.id.search_src_text)).setOnEditorActionListener(
                (v, actionId, event) -> {
                    if(actionId == EditorInfo.IME_ACTION_SEARCH){
                        onQueryTextSubmit(searchView.getQuery().toString());
                        return true;
                    }
                    return false;
                }
            );
        }
        this.mAutoCompletableSearch.autoComplete(
            (TextView) this.searchView.findViewById(R.id.search_src_text)
        );
        ImageView closeBtn = (ImageView) this.searchView.findViewById(R.id.search_close_btn);
        closeBtn.setOnClickListener(view -> {
            if(!(((EditText)searchView.findViewById(R.id.search_src_text)).getText().toString().length() > 0)) {
                this.mAutoCompletableSearch.showCandidates(false);
            }
            this.searchView.setIconified(true);
        });

        this.searchView.setTextStrokeColor(0xffffffff);
        this.removeCrossBtn();

        return super.onCreateOptionsMenu(menu);
    }


    public void removeCrossBtn(){
        try {
            Field field = SearchView.class.getDeclaredField("mCloseButton");
            field.setAccessible(true);
            ImageView img = (ImageView)field.get(MenuItemCompat.getActionView(this.searchitem));
//            img.setEnabled(false);
            img.setImageDrawable(getResources().getDrawable(R.drawable.ic_dark_next));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /* Click Callbacks for Navigation Drawer */
    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromUser) {
        this.terminate = false;
        if(position == NavigationDrawerUtils.ItemType.SEARCH)
            AppManager.getInstance().putBoolean(AppConst.Preference.SEARCH, false);
        this.navigate(NavigationDrawerUtils.getFragmentClassOf(position), true, fromUser);
    }

    /* Map Navigator methods to this.navigator */
    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack) {
        this.mNavigator.navigate(target, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType) {
        this.mNavigator.navigate(target, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, addToBackStack, animatorType, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack) {
        this.mNavigator.navigate(target, bundle, addToBackStack);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, clear);
    }

    @Override
    public void navigate(Class<? extends Fragment> target, Bundle bundle, boolean addToBackStack, AnimatorType animatorType, boolean clear) {
        this.mNavigator.navigate(target, bundle, addToBackStack, animatorType, clear);
    }

    @Override
    public String getBackStackNameAt(int index) {
        return this.mNavigator.getBackStackNameAt(index);
    }

    @Override
    public boolean back() {
        Fragment current = this.getFragmentManager().findFragmentById(this.navigatorContainer.getId());
        if(current instanceof EvaluationStep1Fragment){
            ((EvaluationStep1Fragment)current).back();
        }
        return this.mNavigator.back();
    }
}
