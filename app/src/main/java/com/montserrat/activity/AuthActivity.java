package com.montserrat.activity;

import android.app.Activity;
import android.os.Bundle;

import com.montserrat.controller.AppConst;
import com.montserrat.controller.AppManager;
import com.montserrat.parts.FragmentFactory;
import com.montserrat.utils.request.Api;
import com.montserrat.utils.viewpager.FlexibleViewPager;
import com.montserrat.utils.viewpager.ViewPagerController;
import com.montserrat.utils.viewpager.ViewPagerManager;

import timber.log.Timber;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private ViewPagerManager manager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);

        /* Initializer for entire Application */
        this.initApplication();

        /* Set Manager for ViewPager */
        this.manager = new ViewPagerManager(
                (FlexibleViewPager) findViewById(R.id.auth_viewpager),
                this.getFragmentManager(),
                FragmentFactory.Type.AUTH,
                AppConst.ViewPager.Auth.LENGTH
        );
        this.manager.setSwipeEnabled(false);
    }

    private void initApplication() {
        /* Store ApplicationContext to AppManager */
        AppManager.getInstance().setContext(this.getApplicationContext());

        /* Api Endpoint Setup */
        new Api.Builder()
               .setRoot("mont.izz:kr:3001")
               .setVersion("v1")
               .enableSSL(false)
               .build();

        /* Timber : logging tool */
        Timber.plant(new Timber.DebugTree());
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