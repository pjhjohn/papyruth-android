package com.montserrat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;

import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.FragmentFactory;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerManager;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private FlexibleViewPager viewPager;
    private ViewPagerManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        this.viewPager = (FlexibleViewPager) findViewById(R.id.auth_viewpager);
        this.manager = new ViewPagerManager(this.viewPager, this.getFragmentManager(), FragmentFactory.Type.AUTH, AppConst.ViewPager.Auth.LENGTH);
        this.manager.setSwipeEnabled(false);

        AppManager.getInstance().setContext(this);
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.manager.setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public void onBackPressed() {
        if(!this.manager.onBackPressed()) super.onBackPressed();
    }


}