package com.papyruth.android.fragment.Auth;

import android.content.Context;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.testmanager.TestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
public class SignUpStep1FragmentTest {
    @Rule
    public ActivityTestRule<AuthActivity> mActivityRule = new ActivityTestRule(AuthActivity.class);
    private Context mContext;

    @Test
    public void testEnter(){
        onView(withId(R.id.signin_signup_button)).check(matches(isDisplayed())).perform(ViewActions.click());
        testUniversityCount();
    }

    @Test
    public void testUniversityCount(){
        onView(isRoot()).perform(TestHelper.RecyclerViewHelper.waitItem(R.id.signup_university_recyclerview, 5000));
        onView(withId(R.id.signup_university_recyclerview)).perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));
    }
}
