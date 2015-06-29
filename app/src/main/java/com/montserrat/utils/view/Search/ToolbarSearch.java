package com.montserrat.utils.view.search;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.view.View;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.fragment.main.CourseFragment;
import com.montserrat.app.fragment.main.SimpleCourseFragment;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import timber.log.Timber;

/**
 * Created by SSS on 2015-06-27.
 */
public class ToolbarSearch {
    private AutoCompletableSearchView searchView;
    private Navigator navigator;
    private FragmentManager fragmentManager;

    private static ToolbarSearch instance;
    private ToolbarSearch(){
        searchView = null;
    }
    public static synchronized ToolbarSearch getInstance(){
        if(ToolbarSearch.instance == null) ToolbarSearch.instance = new ToolbarSearch();
        return ToolbarSearch.instance;
    }

    public AutoCompletableSearchView newSearchView(RecyclerViewItemClickListener listener, Context context, AutoCompletableSearchView.Type type){
        searchView = new AutoCompletableSearchView(listener, context, type);
        return searchView;
    }

    public AutoCompletableSearchView getAutoCompletableSearchView(){
        return searchView;
    }

    public void setActivityComponent(Activity activity){
        this.navigator = (Navigator) activity;
        this.fragmentManager = activity.getFragmentManager();
    }

    public void onRecyclerViewClicked(View view, int position, boolean isAutoComplete){
        this.searchView.onRecyclerViewItemClick(view, position);

        if(isAutoComplete) {
            this.searchView.onRecyclerViewItemClick(view, position);
            AppManager.getInstance().putBoolean(AppConst.Preference.SEARCH, true);
            Fragment active = this.fragmentManager.findFragmentByTag(AppConst.Tag.ACTIVE_FRAGMENT);

            if (active != null && active.getClass() == SimpleCourseFragment.class)
                this.searchView.searchCourse();
            else
                this.navigator.navigate(SimpleCourseFragment.class, true);
        }else{
            this.searchView.onRecyclerViewItemClick(view, position);
            Timber.d("***go Course");
            this.navigator.navigate(CourseFragment.class, true);
        }
    }
}
