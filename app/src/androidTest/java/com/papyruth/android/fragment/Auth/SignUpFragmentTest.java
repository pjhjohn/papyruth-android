package com.papyruth.android.fragment.auth;

import android.content.Context;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.testmanager.TestHelper;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by SSS on 2016-03-07.<br/>
 *
 * Test Tool : Espresso
 *
 * @author SSS
 * @see com.papyruth.android.fragment.auth.SignUpStep1Fragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpFragmentTest {
    @Rule
    public ActivityTestRule<AuthActivity> mActivityRule = new ActivityTestRule(AuthActivity.class);
    private Context mContext;

    @Test
    public void testSuit(){
        enterSignUp();
        signupTestStep1();
        signupTestStep2();
        signupTestStep3();
    }

    private void enterSignUp() {
        onView(withId(R.id.signin_signup_button)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    public void signupTestStep1(){
        onView(isRoot()).perform(TestHelper.RecyclerViewHelper.waitItem(R.id.signup_university_recyclerview, 5000));
        onView(withId(R.id.signup_university_recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
        onView(isRoot()).perform(TestHelper.CommonHelper.doWait(2000));
        onData(Matchers.allOf(Matchers.is(Matchers.instanceOf(String.class)), Matchers.is("2016"))).perform(ViewActions.click());
    }

    public void signupTestStep2(){
        if(mActivityRule.getActivity().getFragmentManager().findFragmentById(R.id.auth_navigator).getClass().getSimpleName().equals(SignUpStep1Fragment.class.getSimpleName())){
            signupTestStep1();
        }else if(mActivityRule.getActivity().getFragmentManager().findFragmentById(R.id.auth_navigator).getClass().getSimpleName().equals(SignUpStep2Fragment.class.getSimpleName())){
            onView(withId(R.id.signup_email_text)).perform(ViewActions.clearText(), ViewActions.typeText("wwww43211@gmail.com"));
            onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signup_email_text, 5000, true));
            onView(withId(R.id.signup_email_text)).perform(ViewActions.clearText(), ViewActions.typeText("wwww43211+test@gmail.com"));
            onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signup_email_text, 5000, false));
            onView(withId(R.id.signup_nickname_text)).perform(ViewActions.clearText(), ViewActions.typeText("222"));
            onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signup_nickname_text, 5000, true));
            onView(withId(R.id.signup_nickname_text)).perform(ViewActions.clearText(), ViewActions.typeText("222+snu"));
            onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signup_nickname_text, 5000, false));
        }
    }
    public void signupTestStep3(){
        if(FloatingActionControl.getButton().isShown()){
            onView(withId(FloatingActionControl.getButton().getId())).check(matches(isDisplayed())).perform(ViewActions.click());
        }
    }
}
