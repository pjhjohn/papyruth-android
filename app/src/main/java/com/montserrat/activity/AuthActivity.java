package com.montserrat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.FragmentFactory;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerMediator;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private FlexibleViewPager viewPager;
    private PagerAdapter viewPagerAdapter;
    private ViewPagerMediator mediator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        this.viewPager = (FlexibleViewPager) findViewById(R.id.auth_viewpager);
        this.viewPagerAdapter = new SlideViewPagerAdapter(this.getFragmentManager());
        this.viewPager.setAdapter(this.viewPagerAdapter);
        this.viewPager.setSwipeEnabled(false);

        this.mediator = new ViewPagerMediator(this.viewPager, this.viewPagerAdapter);

        AppManager.getInstance().setContext(this);
    }

    @Override
    public void setCurrentPage (int pageNum, boolean addToBackStack) {
        this.mediator.setCurrentPage(pageNum, addToBackStack);
    }

    @Override
    public void onBackPressed() {
        if(!this.mediator.onBackPressed()) super.onBackPressed();
    }

    private class SlideViewPagerAdapter extends FragmentStatePagerAdapter {
        public SlideViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentFactory.create(FragmentFactory.Type.AUTH, position);
        }

        @Override
        public int getCount() {
            return AppConst.ViewPager.Auth.LENGTH;
        }
    }
}