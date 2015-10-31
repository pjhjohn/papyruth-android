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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.DummyFragment;
import com.montserrat.app.fragment.main.EvaluationStep1Fragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.fragment.main.MyInfoFragment;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.navigation_drawer.NavigationDrawerCallback;
import com.montserrat.app.navigation_drawer.NavigationDrawerFragment;
import com.montserrat.app.navigation_drawer.NavigationDrawerUtils;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.ToolbarUtil;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.NavigationCallback;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.montserrat.utils.view.search.AutoCompletableSearchView;
import com.montserrat.utils.view.search.CustomSearchView;
import com.montserrat.utils.view.search.ToolbarSearch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallback, RecyclerViewItemClickListener, Navigator, AutoCompletableSearchView.SearchViewListener {
    private NavigationDrawerFragment mNavigationDrawer;
    private FragmentNavigator mNavigator;
    private ToolbarSearch toolbarSearch;

    private CompositeSubscription mCompositeSubscription;
    @InjectView(R.id.fac) FloatingActionControlContainer fac;
    @InjectView(R.id.main_navigator) FrameLayout navigatorContainer;
    @InjectView(R.id.search_result) protected RecyclerView searchResult;
    @InjectView(R.id.query_result_outside) protected View outsideResult;
    @InjectView(R.id.toolbar) protected Toolbar mToolbar;
    private MaterialMenuDrawable mMaterialMenuDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);
        toolbarSearch = new ToolbarSearch(this, this);
        mCompositeSubscription = new CompositeSubscription();
        mMaterialMenuDrawable = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);

        mToolbar.setNavigationIcon(mMaterialMenuDrawable);
        ToolbarUtil.registerMenu(mToolbar, R.menu.main, item -> item.getItemId() == R.id.menu_search || super.onOptionsItemSelected(item));

        mNavigationDrawer = (NavigationDrawerFragment) this.getFragmentManager().findFragmentById(R.id.drawer);
        mNavigationDrawer.setup(R.id.drawer, (DrawerLayout) this.findViewById(R.id.drawer_layout), mToolbar);
        mNavigationDrawer.update();

        mNavigator = new FragmentNavigator(mNavigationDrawer, this.getFragmentManager(), R.id.main_navigator, HomeFragment.class, mMaterialMenuDrawable, MaterialMenuDrawable.IconState.BURGER, mToolbar);

        this.onInitializeMenuOnToolbar(mToolbar.getMenu());
        List<Candidate> candidates = new ArrayList<>();
        /* Instantiate Multiple ViewPagerManagers */

        toolbarSearch.initAutoComplete(this.searchResult, this.outsideResult, this.editText, candidates);

        toolbarSearch.setSearchViewListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
        this.setMenuItemVisibility(AppConst.Menu.MENU_SETTING, false);
        checkMandatoryEvlauation();
    }

    public void checkMandatoryEvlauation(){
        if(User.getInstance().getMandatory_evaluation_count() > 0) {
            new MaterialDialog.Builder(this)
                .content(getResources().getString(R.string.inform_mandatory_evaluation, User.getInstance().getMandatory_evaluation_count()))
                .positiveText(R.string.goto_write)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) return;
        mCompositeSubscription.unsubscribe();
    }

    private MenuItem itemSearch;
    private MenuItem itemSetting;
    private CustomSearchView searchView;
    private EditText editText;


    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(searchResult != null && ((RecyclerView)view.getParent()).getId() == searchResult.getId())
            toolbarSearch.recyclerViewClicked(view, position);
    }

    private boolean terminate = false;
    @Override
    public void onBackPressed() {
        if (this.mNavigationDrawer.isOpened()) this.mNavigationDrawer.close();
        else if (toolbarSearch.onBack()) ;
        else if (this.mNavigator.back()) terminate = false;
        else if (terminate) super.onBackPressed();
        else {
            Toast.makeText(this, this.getResources().getString(R.string.confirm_exit), Toast.LENGTH_LONG).show();
            terminate = true;
        }
    }

    /* Menu */
    public void setMenuItemVisibility(int res, boolean visible){
        if(itemSearch.getItemId() == res)
            itemSearch.setVisible(visible);
        else if(itemSetting.getItemId() == res)
            itemSetting.setVisible(visible);
    }
    private boolean onInitializeMenuOnToolbar(Menu menu) {
        this.itemSearch = menu.findItem(R.id.menu_search);
        this.itemSetting = menu.findItem(R.id.menu_setting);
        this.searchView = (CustomSearchView) itemSearch.getActionView();
        this.itemSearch.expandActionView();
        this.searchView.setOnBackListener(()->{
            toolbarSearch.onBack();
            return true;
        });
        if(searchView != null) {
            this.editText = (EditText) searchView.findViewById(R.id.search_src_text);
            searchView.setQueryHint("Input Search Query");

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if (searchManager != null)
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
            this.editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchView.clearFocus();
                    toolbarSearch.submitQuery(searchView.getQuery().toString());
                    toolbarSearch.search();
                    return true;
                }
                return false;
            });
            ImageView closeBtn = (ImageView) this.searchView.findViewById(R.id.search_close_btn);
            closeBtn.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(this.editText.getText())) {
                    this.editText.setText("");
                    this.toolbarSearch.getAutoCompletableSearchView().showCandidates(true);
                } else {
                    this.editText.setText(" ");
                    this.searchView.setIconified(true);
                    this.editText.setText("");
                }
            });
            this.showCrossBtn(true);
            this.mToolbar.setOnMenuItemClickListener(item -> {
                if (item.equals(itemSetting)) {
                    this.navigate(MyInfoFragment.class, true, AnimatorType.SLIDE_TO_RIGHT);
                }
                return true;
            });
        }
        return super.onCreateOptionsMenu(menu);
    }


    public void showCrossBtn(boolean remove){
        try {
            Field field = SearchView.class.getDeclaredField("mCloseButton");
            field.setAccessible(true);
            ImageView img = (ImageView)field.get(MenuItemCompat.getActionView(this.itemSearch));
            if(remove) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.background_transparent));
            }else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.ic_light_clear));
            }
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

    @Override
    public void setOnNavigateListener(NavigationCallback listener) {
        this.mNavigator.setOnNavigateListener(listener);
    }

    @Override
    public void onTextChange(String query) {
        if (TextUtils.isEmpty(query)){
            showCrossBtn(true);
        }else {
            showCrossBtn(false);
        }
    }

    @Override
    public void onShowChange(boolean show) {
        FloatingActionControl.getInstance().closeMenuButton(true);
        if(show) {
            this.mMaterialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            this.mToolbar.setNavigationOnClickListener(view -> {
                this.toolbarSearch.getAutoCompletableSearchView().showCandidates(false);
                this.editText.setText("");
                this.searchView.setIconified(true);
            });
        }else{
            if(getFragmentManager().getBackStackEntryCount() > 1)
                this.mMaterialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.ARROW);
            else
                this.mMaterialMenuDrawable.animateIconState(MaterialMenuDrawable.IconState.BURGER);

            this.editText.setText("");
            this.searchView.setIconified(true);
            this.mToolbar.setNavigationOnClickListener(
                this.mNavigationDrawer::onClick
            );
        }
    }

    public ToolbarSearch getToolbarSearch(){
        return this.toolbarSearch;
    }

    @Override
    public void submitQuery(String query) {

    }

    @Override
    public void onItemSelected(Candidate candidate) {

    }
}
