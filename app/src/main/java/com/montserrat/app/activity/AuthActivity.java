package com.montserrat.app.activity;

import android.app.Activity;
import android.os.Bundle;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private ViewPagerManager manager;

    @InjectView(R.id.fac) FloatingActionControlContainer fac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);

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
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
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
    public boolean onBack() {
        return this.manager.onBack();
    }

    @Override
    public void onBackPressed() {
        if(!this.manager.onBack()) super.onBackPressed();
    }
}