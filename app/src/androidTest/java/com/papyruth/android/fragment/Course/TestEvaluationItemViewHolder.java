package com.papyruth.android.fragment.Course;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.UserData;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.viewholder.EvaluationItemViewHolder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TestEvaluationItemViewHolder {
    User user;
    EvaluationData evaluation;

    @Before
    public void initData(){
        evaluation = new EvaluationData();
        user = User.getInstance();
    }

    @Test
    public void testIsValidData(){
        UserData userData = new UserData();

// -----1.1.1
        evaluation.university_confirmation_needed = false;
        userData.mandatory_evaluation_count = 1;
        userData.confirmed = false;
        userData.university_confirmed = false;
        user.update(userData);
        dataValidTest(false);

// -----1.1.2
        userData.confirmed = true;
        user.update(userData);
        dataValidTest(false);

// ----1.1.3
        userData.university_confirmed = true;
        user.update(userData);
        dataValidTest(false);


// -----1.2.1
        userData.mandatory_evaluation_count = 0;
        userData.confirmed = false;
        userData.university_confirmed = false;
        user.update(userData);
        dataValidTest(false);

// -----1.2.2
        userData.confirmed = true;
        user.update(userData);
        dataValidTest(true);

// ----1.2.3
        userData.university_confirmed = true;
        user.update(userData);
        dataValidTest(true);



// -----2.1.1
        evaluation.university_confirmation_needed = true;
        userData.mandatory_evaluation_count = 1;
        userData.confirmed = false;
        userData.university_confirmed = false;
        user.update(userData);
        dataValidTest(false);

// -----2.1.2
        userData.confirmed = true;
        user.update(userData);
        dataValidTest(false);

// ----2.1.3
        userData.university_confirmed = true;
        user.update(userData);
        dataValidTest(false);


// -----2.2.1
        userData.mandatory_evaluation_count = 0;
        userData.confirmed = false;
        userData.university_confirmed = false;
        user.update(userData);
        dataValidTest(false);

// -----2.2.2
        userData.confirmed = true;
        user.update(userData);
        dataValidTest(false);

// ----2.2.3
        userData.university_confirmed = true;
        user.update(userData);
        dataValidTest(true);
    }

    private void dataValidTest(boolean valid){
        insertEvaluadionData(EVALUATION_STATE.INVALID);
        assertFalse(EvaluationItemViewHolder.isValidData(evaluation, user));
        if(valid) {
            insertEvaluadionData(EVALUATION_STATE.VALID);
            assertTrue(EvaluationItemViewHolder.isValidData(evaluation, user));

            insertEvaluadionData(EVALUATION_STATE.EMPTYBODY);
            assertTrue(EvaluationItemViewHolder.isValidData(evaluation, user));
        }else{
            insertEvaluadionData(EVALUATION_STATE.VALID);
            assertFalse(EvaluationItemViewHolder.isValidData(evaluation, user));

            insertEvaluadionData(EVALUATION_STATE.EMPTYBODY);
            assertFalse(EvaluationItemViewHolder.isValidData(evaluation, user));
        }
    }

    enum EVALUATION_STATE {
        INVALID, VALID, EMPTYBODY
    }
    private void insertEvaluadionData(EVALUATION_STATE STATE){
        switch (STATE){
            case INVALID:
                evaluation.body = "";
                evaluation.point_clarity = 0;
                evaluation.point_easiness = 0;
                evaluation.point_overall = 0;
                evaluation.point_gpa_satisfaction = 0;
                break;
            case VALID:
                evaluation.body = "valid";
                evaluation.point_clarity = 5;
                evaluation.point_easiness = 5;
                evaluation.point_overall = 5;
                evaluation.point_gpa_satisfaction = 5;
                break;
            case EMPTYBODY:
                evaluation.body = "";
                evaluation.point_clarity = 5;
                evaluation.point_easiness = 5;
                evaluation.point_overall = 5;
                evaluation.point_gpa_satisfaction = 5;
                break;
        }
    }
}
