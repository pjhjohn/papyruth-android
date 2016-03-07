package com.papyruth.testmanager;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.TimeoutException;

/**
 * Created by SSS on 2016-03-07.<br/>
 *
 * TestHelper help easy make other test code.
 *
 * @author SSS
 */
public class TestHelper {

    /**
     * Check TextView show any error.<br/>
     *
     * @author SSS
     **/
    public static class ErrorMessage {
        /**
         * Test view(instance of TextView) has error message.
        **/
        public static Matcher<View> withError(final String errorText){
            return new TypeSafeMatcher<View>() {
                @Override
                protected boolean matchesSafely(View item) {
                    return item instanceof EditText && ((EditText) item).getError() != null && ((EditText) item).getError().toString().equals(errorText);
                }

                @Override
                public void describeTo(Description description) {

                }
            };
        }

        /**
         * @param viewId view resource id
         * @param milliSeconds the maximum time to wait in milliseconds.
         * @param needError If must need error message, set true. else not want to error message set false.
         *
         * Wait until TextView show error message. else throw TimeOutException.
         * But if needError is false. not TimeOutException. But when check error message, throw RuntimeException.
         *
         * @exception TimeoutException Param needError is true and time out param milliSeconds.
         * @exception RuntimeException When param needError is false, but TextView has any error message.
         **/
        public static ViewAction waitErrorMsg(final int viewId, final long milliSeconds, final boolean needError){
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return ViewMatchers.isRoot();
                }

                @Override
                public String getDescription() {
                    return "wait for a specific TextView error not Exist";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    uiController.loopMainThreadUntilIdle();
                    final long startTime = System.currentTimeMillis();
                    final long endTime = startTime + milliSeconds;
                    do{
                        for(View child : TreeIterables.breadthFirstViewTraversal(view)){
                            if( child instanceof TextView && ((TextView) child).getError() != null) {
                                if(needError) return;
                                else {
                                    throw new PerformException.Builder()
                                            .withActionDescription(getDescription())
                                            .withCause(new RuntimeException("Error exist"))
                                            .build();
                                }
                            }
                        }
                        uiController.loopMainThreadForAtLeast(50);
                    }while (System.currentTimeMillis() < endTime);
                    if( !needError ) return;

                    throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
                }
            };
        }
    }
}
