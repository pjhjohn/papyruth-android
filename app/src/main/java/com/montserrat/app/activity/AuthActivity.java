package com.montserrat.app.activity;

import android.app.Activity;
import android.os.Bundle;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;

import java.util.Stack;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private ViewPagerManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);

        /* Set Manager for ViewPager */
        this.manager = new ViewPagerManager(
                (FlexibleViewPager) findViewById(R.id.auth_viewpager),
                this.getFragmentManager(),
                AppConst.ViewPager.Type.AUTH,
                AppConst.ViewPager.Auth.LENGTH
        );
        this.manager.setSwipeEnabled(false);
    }

    @Override
    public Stack<Integer> getHistoryCopy() {
        return this.manager.getHistoryCopy();
    }

    @Override
    public int getPreviousPage() {
        return this.manager.getPreviousPage();
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.manager.setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public boolean popCurrentPage () {
        return this.manager.popCurrentPage();
    }

    @Override
    public void onBackPressed() {
        if(!this.manager.popCurrentPage()) super.onBackPressed();
    }
}