package com.papyruth.android.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.papyruth.android.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by SSS on 2016-03-02.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EsSplashActivityTest {
    @Rule public ActivityTestRule<SplashActivity> mActivityRule = new ActivityTestRule(SplashActivity.class);

    @Test
    public void testSplash(){
        onView(withId(R.id.splash_background)).check(matches(isDisplayed()));
        onView(withId(R.id.splash_background_circle)).check(matches(isDisplayed()));
        onView(withId(R.id.splash_application_logo)).check(matches(isDisplayed()));
        onView(withId(R.id.splash_version_name)).check(matches(isDisplayed()));
    }
}
