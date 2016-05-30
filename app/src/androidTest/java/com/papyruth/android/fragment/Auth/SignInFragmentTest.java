package com.papyruth.android.fragment.auth;

import android.content.Context;
import android.support.test.espresso.action.EspressoKey;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;

import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.testmanager.TestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by SSS on 2016-03-02.<br/>
 *
 * Test Tool : Espresso
 *
 * @author SSS
 * @see com.papyruth.android.fragment.auth.SignInFragment
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInFragmentTest {
    @Rule public ActivityTestRule<AuthActivity> mActivityRule = new ActivityTestRule(AuthActivity.class);
    private Context mContext;



    @Test
    public void testAllElementExist(){
        onView(withId(R.id.signin_email_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signin_password_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signin_button)).check(matches(isDisplayed()));
        onView(withId(R.id.signin_signup_button)).check(matches(isDisplayed()));
        onView(withId(R.id.signin_password_recovery)).check(matches(isDisplayed()));
//        onView(withId(R.id.material_progress_large)).check(matches(isDisplayed()));
    }

    /**
     * Test sign in process check some case.<br/><br/>
     *
     * case1. wrong email format, short password<br/>
     * case2. wrong email format, enough length password.<br/>
     * case3. valid email format, enough length password.<br/>
     *
    **/
    @Test
    public void testSignInProcessError(){
        mContext = mActivityRule.getActivity();
        // case 1
        onView(withId(R.id.signin_email_text)).perform(ViewActions.clearText(), ViewActions.typeText("22222"));
        onView(withId(R.id.signin_password_text)).perform(ViewActions.click(), ViewActions.clearText(), ViewActions.typeText("322222"));
        onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signin_email_text, 5000, true));
        onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signin_password_text, 5000, true));
        onView(withId(R.id.signin_email_text)).check(matches(TestHelper.TextViewHelper.withError("잘못된 이메일 형식입니다")));
        onView(withId(R.id.signin_password_text)).perform(ViewActions.click()).check(matches(TestHelper.TextViewHelper.withError("비밀번호가 너무 짧습니다")));

        //case 2
        onView(withId(R.id.signin_email_text)).perform(ViewActions.click(), ViewActions.clearText(), ViewActions.typeText("22222"));
        onView(withId(R.id.signin_password_text)).perform(ViewActions.click(), ViewActions.clearText(), ViewActions.typeText("22222222"));
        onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signin_email_text, 5000, true));
        onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signin_password_text, 5000, true));
        onView(withId(R.id.signin_email_text)).check(matches(TestHelper.TextViewHelper.withError("잘못된 이메일 형식입니다")));

        //case 3
        onView(withId(R.id.signin_email_text)).perform(ViewActions.click(), ViewActions.clearText(), ViewActions.typeText("2@2.2"));
        onView(isRoot()).perform(TestHelper.TextViewHelper.waitErrorMsg(R.id.signin_email_text, 5000, false));
        onView(withId(R.id.signin_button)).check(matches(isEnabled()));

        //
        pressBack();
        onView(withId(R.id.signin_signup_button)).perform(ViewActions.click());
        onView(isRoot()).perform(TestHelper.CommonHelper.doWait(5000));
    }

    @Test
    public void testEnterSignUp(){
        new EspressoKey.Builder().withKeyCode(KeyEvent.KEYCODE_HOME).build();
    }
}