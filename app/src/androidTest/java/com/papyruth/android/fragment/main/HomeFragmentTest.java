package com.papyruth.android.fragment.main;

import android.content.Context;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.papyruth.android.R;
import com.papyruth.android.activity.AuthActivity;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.testmanager.TestHelper;

import org.hamcrest.Matchers;
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
 * Created by SSS on 2016-06-01.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);
    private Context mContext;

    @Test
    public void testEnter(){
        onView(isRoot()).perform(TestHelper.CommonHelper.doWait(5000));
//        ((Toolbar) mActivityRule.getActivity().findViewById(R.id.toolbar)).get
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
        onView(Matchers.allOf(ViewMatchers.withParent(withId(R.id.toolbar)), ViewMatchers.withClassName(Matchers.is(ImageButton.class.getName())))).check(matches(isDisplayed())).perform(ViewActions.click());
    }
}
