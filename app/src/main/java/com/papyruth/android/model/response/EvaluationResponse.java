package com.papyruth.android.model.response;

import com.papyruth.android.model.EvaluationData;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class EvaluationResponse {
    public Boolean success;
    public Integer evaluation_id;
    public EvaluationData evaluation;

    @Override
    public String toString() {
        return String.format("%s with evaluation_id = #%d", success? "Succeed" : "Failed", evaluation_id);
    }
}
