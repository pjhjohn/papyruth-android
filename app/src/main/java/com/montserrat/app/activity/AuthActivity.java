package com.montserrat.app.activity;

import android.app.Activity;
import android.os.Bundle;

import com.montserrat.app.R;
import com.montserrat.app.AppConst;
import com.montserrat.app.fragment.FragmentFactory;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerManager;

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
                FragmentFactory.Type.AUTH,
                AppConst.ViewPager.Auth.LENGTH
        );
        this.manager.setSwipeEnabled(false);
    }

    @Override
    public int getPreviousPage () {
        return this.manager.getPreviousPage();
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.manager.setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public void popCurrentPage () {
        this.manager.popCurrentPage();
    }

    @Override
    public void onBackPressed() {
        if(!this.manager.onBackPressed()) super.onBackPressed();
    }
}