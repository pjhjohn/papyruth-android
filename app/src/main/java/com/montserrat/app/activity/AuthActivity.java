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
import com.montserrat.utils.view.viewpager.FlexibleViewPager;
import com.montserrat.utils.view.viewpager.ViewPagerController;
import com.montserrat.utils.view.viewpager.ViewPagerManager;
import com.squareup.picasso.Picasso;

import java.util.Stack;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

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
            Toast.makeText(this,"cache expired!!!", Toast.LENGTH_LONG).show();
        });
        /* DEBUGGING CODE END*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    private final int MAXSTEP = 4;
    /**
     * @param step : < 0 : sign up step bar height : 0
     *             : <= 4 : sign up step bar height : 4 and set color
     *             : > 4 : auth activity header height : 0
     *
     */
    public void signUpStep(int step){
        if (step > MAXSTEP) {
            this.header.setVisibility(View.GONE);
        }else{
            this.header.setVisibility(View.VISIBLE);
            if(step < 0){
                ViewGroup.LayoutParams param =  this.signUpStep.getLayoutParams();
                this.stateName.setText("");
                param.height = 0;
                this.signUpStep.setLayoutParams(param);

            }else {
                if(this.signUpStep.getHeight() < 1){
                    ViewGroup.LayoutParams param =  this.signUpStep.getLayoutParams();
                    this.stateName.setText(R.string.action_sign_up);
                    param.height = (int) (4 * this.getBaseContext().getResources().getDisplayMetrics().density);
                    this.signUpStep.setLayoutParams(param);
                }
                for (int i = 0; i < MAXSTEP; i++) {
                    if (i < step)
                        this.signUpStep.getChildAt(i).setBackgroundColor(this.getResources().getColor(R.color.fg_normal));
                    else
                        this.signUpStep.getChildAt(i).setBackgroundColor(this.getResources().getColor(R.color.translucent));

                }
            }
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