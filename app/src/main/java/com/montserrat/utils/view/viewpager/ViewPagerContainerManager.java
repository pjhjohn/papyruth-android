package com.montserrat.utils.view.viewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppConst.ViewPager.Type;
import com.montserrat.utils.support.fab.FloatingActionControl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-23.
 */
public class ViewPagerContainerManager extends ViewPager.SimpleOnPageChangeListener implements ViewPagerContainerController {
    private FlexibleViewPager viewpager;
    private FragmentManager fragment_manager;

    private Map<Type, ViewPagerManager> container;
    private Stack<Page> history;
    private Page current;
    private Page previous;
    private Type currentCategory;
    private boolean addToBackStack;

    public ViewPagerContainerManager(FlexibleViewPager viewpager, FragmentManager fragment_manager) {
        Timber.d("Constructor");
        this.viewpager = viewpager;
        this.fragment_manager = fragment_manager;
        this.container = new HashMap<>();
        this.clear();
    }
    public void clear() {
        Timber.d("Clear");
        this.current = null;
        this.previous = null;
        this.history = new Stack<>();
        this.currentCategory = null;
        this.addToBackStack = true;
    }

    public ViewPagerManager activate(Type category, boolean clear) {
        ViewPagerManager manager = this.container.get(category);
        if(clear) {
            this.clear();
            this.viewpager.setAdapter(manager.adapter);
            this.viewpager.setOnPageChangeListener(manager.listener);
            this.currentCategory = category;
            this.current = Page.at(category, 0);
            manager.setCurrentPage(0, false);
        } else {
            this.viewpager.setAdapter(manager.adapter);
            this.viewpager.setOnPageChangeListener(manager.listener);
            this.currentCategory = category;
        } return manager;
    }

    public ViewPagerManager activate(Type category) {
        return activate(category, false);
    }

    @Override
    public Stack<Page> getHistoryCopy() {
        Timber.d("getHistoryCopy");
        return (Stack<Page>) this.history.clone();
    }

    @Override
    public Page getPreviousPage() {
        Timber.d("getPreviousPage");
        return this.previous;
    }

    @Override
    public void setCurrentPage(Page page, boolean addToBackStack) {
        Timber.d("MOVE TO %s", page);
        Timber.d("%s + [%s]", this.history, addToBackStack? this.current : "");
        this.addToBackStack = addToBackStack;
        if(current == null) this.activate(page.category, true);
        else if(current.category == page.category) this.container.get(page.category).setCurrentPage(page.index, addToBackStack);
        else {
            this.activate(page.category).setCurrentPage(page.index, addToBackStack);
            if (this.addToBackStack) this.history.push(current);
            this.previous = this.current;
            this.current = page;
        } Timber.d("= %s, current:%s", this.history, this.current);
    }

    @Override
    public boolean popCurrentPage() {
        Timber.d("POPPING %s", this.current);
        Timber.d("%s - [%s]", this.history, this.history.isEmpty()? "" : this.history.peek());
        if(!this.history.isEmpty()) {
            final Page popped = this.history.pop();
            this.activate(popped.category).popCurrentPage();
            this.current = popped;
            Timber.d("= %s, current:%s", this.history, this.current);
            return true;
        } return false;
    }

    @Override
    public boolean onBack() {
        Timber.d("onBack");
        final Fragment target = ((ViewPagerManager.Adapter)this.viewpager.getAdapter()).getFragmentAt(this.current.index);
        if (target != null) {
            boolean backed = false;
            if(FloatingActionControl.getMenu()!=null && FloatingActionControl.getMenu().isOpened()) {
                Timber.d("Close Control");
                FloatingActionControl.getMenu().close(true);
                backed = true;
            }
            if(!backed && target instanceof OnBack) {
                backed = ((OnBack) target).onBack();
            }
            if(!backed) {
                backed = this.popCurrentPage();
            }
            Timber.d("=> %s, current:%s", this.history, this.current);
            return backed;
        } return false;
    }

    @Override
    public void onPageSelected (int position) {
        Timber.d("onPageSelected with %d", position);
        if (this.addToBackStack) this.history.push(current);
        this.previous = this.current;
        this.current = Page.at(this.currentCategory, position);
    }


    private ViewPagerContainerManager add(Type category, int length) {
        Timber.d("add with %s, %d", AppConst.ViewPager.type2Str(category), length);
        this.container.put(category, new ViewPagerManager(this.viewpager, this.fragment_manager, category, length, this));
        return this;
    }
    public static ViewPagerContainerManager newInstance(FlexibleViewPager viewpager, FragmentManager fragmentManager) {
        return new ViewPagerContainerManager(viewpager, fragmentManager)
            .add(AppConst.ViewPager.Type.HOME, AppConst.ViewPager.Home.LENGTH)
            .add(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.LENGTH)
            .add(AppConst.ViewPager.Type.RECOMMENDATION, AppConst.ViewPager.Recommendation.LENGTH)
            .add(AppConst.ViewPager.Type.EVALUATION, AppConst.ViewPager.Evaluation.LENGTH)
            .add(AppConst.ViewPager.Type.RANDOM, AppConst.ViewPager.Random.LENGTH)
            .add(AppConst.ViewPager.Type.PROFILE, AppConst.ViewPager.Profile.LENGTH)
            .add(AppConst.ViewPager.Type.SIGNOUT, AppConst.ViewPager.Signout.LENGTH);
    }
}
