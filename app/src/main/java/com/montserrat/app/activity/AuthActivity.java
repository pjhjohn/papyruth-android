package com.montserrat.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.fragment.auth.AuthFragmentFactory;
import com.montserrat.app.recyclerview.viewholder.ViewHolderFactory;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.view.FloatingActionControlContainer;
import com.montserrat.utils.view.MetricUtil;
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;
import com.squareup.picasso.Picasso;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Activity For Authentication.
 */
public class AuthActivity extends Activity implements ViewPagerController {
    private ViewPagerManager manager;

    @InjectView(R.id.fac) protected FloatingActionControlContainer fac;
    @InjectView(R.id.sign_up_step) protected LinearLayout signUpStep;
    @InjectView(R.id.auth_header) protected LinearLayout header;
    @InjectView(R.id.state_name) protected TextView stateName;
    @InjectView(R.id.logo) protected ImageView logo;

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
            AuthFragmentFactory.getInstance(),
            AppConst.ViewPager.Auth.LENGTH
        );
    }
    @Override
    public void onResume() {
        super.onResume();
        ViewHolderFactory.getInstance().setContext(this);
        Picasso.with(this.getBaseContext()).load(R.drawable.ic_light_edit).transform(new ColorFilterTransformation(this.getResources().getColor(R.color.primary_dark_material_dark))).into(this.logo);

        /* Cache expired for TEST DEBUGGING */
        this.logo.setOnClickListener(v->{
            AppManager.getInstance().clear(AppConst.Preference.INFO_EVALUATION_COUNT);
            AppManager.getInstance().clear(AppConst.Preference.INFO_UNIVERSITY_COUNT);
            AppManager.getInstance().clear(AppConst.Preference.INFO_STUREND_COUNT);
            AppManager.getInstance().clear(AppConst.Preference.UNIVERSITY_NAME);
            AppManager.getInstance().clear(AppConst.Preference.UNIVERSITY_EVALUATION_COUNT);
            AppManager.getInstance().clear(AppConst.Preference.UNIVERSITY_STUDENT_COUNT);
            Toast.makeText(this, "cache expired!!!", Toast.LENGTH_LONG).show();
        });
        /* DEBUGGING CODE END*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    private final int SIGNUP_STEP_LENGTH = 4;
    /**
     * @param currentStep set currentStep to arrange layout<br>
     * currentStep < 0 : sign up step bar height : 0<br>
     * currentStep <= 4 : sign up step bar height : 4 and set color<br>
     * currentStep > 4 : auth activity header height : 0
     *
     */
    public void signUpStep(int currentStep){
        this.header.setVisibility(currentStep > SIGNUP_STEP_LENGTH ? View.GONE : View.VISIBLE);
        if (currentStep > SIGNUP_STEP_LENGTH) return;
        if(currentStep < 0){
            ViewGroup.LayoutParams param =  this.signUpStep.getLayoutParams();
            param.height = 0;
            this.signUpStep.setLayoutParams(param);
            this.stateName.setText("");
        } else {
            if(this.signUpStep.getHeight() < 1){
                ViewGroup.LayoutParams param =  this.signUpStep.getLayoutParams();
                this.stateName.setText(R.string.action_sign_up);
                param.height = MetricUtil.toPixels(this, 4);
                this.signUpStep.setLayoutParams(param);
            }
            for (int i = 0; i < SIGNUP_STEP_LENGTH; i++)
                this.signUpStep.getChildAt(i).setBackgroundColor(this.getResources().getColor(i < currentStep ? R.color.fg_normal : R.color.translucent));
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
}