package com.montserrat.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.fragment.auth.AuthFragmentFactory;
import com.montserrat.app.fragment.auth.SignInFragment;
import com.montserrat.app.fragment.main.HomeFragment;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.navigator.FragmentNavigator;
import com.montserrat.utils.view.navigator.Navigator;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController, Navigator {
    private ViewPagerManager manager;
    private FragmentNavigator mNavigator;

    @InjectView(R.id.fac) protected FloatingActionControlContainer fac;
    @InjectView(R.id.sign_up_step) protected LinearLayout signUpStep;
    @InjectView(R.id.state_name) protected TextView stateName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_auth);
        ButterKnife.inject(this);
        FloatingActionControl.getInstance().setContainer(this.fac);

        this.mNavigator = new FragmentNavigator(this.getFragmentManager(), R.id.auth_viewpager, SignInFragment.class);
//        this.mNavigator = new FragmentNavigator(null, this.getFragmentManager(), R.id.auth_viewpager, SignInFragment.class, null, null);

        /* Set Manager for ViewPager */
        this.manager = new ViewPagerManager(
            (FlexibleViewPager) findViewById(R.id.auth_viewpager),
            this.getFragmentManager(),
            AuthFragmentFactory.getInstance(),
            AppConst.ViewPager.Auth.LENGTH
        );
    }
    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    public void signUp(boolean signup){
        ViewGroup.LayoutParams param =  this.signUpStep.getLayoutParams();
        if(signup){
            this.stateName.setText(R.string.action_sign_up);
            param.height = (int) (8 * this.getBaseContext().getResources().getDisplayMetrics().density);
        }else{
            this.stateName.setText("");
            param.height = 0;
        }
        this.signUpStep.setLayoutParams(param);
    }
    public void signUpStep(int step){
        for (int i = 0; i < 4; i++){
            if(i < step)
                this.signUpStep.getChildAt(i).setBackgroundColor(this.getResources().getColor(R.color.fg_normal));
            else
                this.signUpStep.getChildAt(i).setBackgroundColor(this.getResources().getColor(R.color.translucent));

        }
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
        return this.mNavigator.back();
    }
}