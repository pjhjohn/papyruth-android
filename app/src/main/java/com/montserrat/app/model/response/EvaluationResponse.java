package com.montserrat.app.model.response;

import com.montserrat.app.model.PartialEvaluation;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class EvaluationResponse {
    public Boolean success;
    public Integer evaluation_id;
    public PartialEvaluation evaluation;

    @Override
    public String toString() {
        return String.format("%s with evaluation_id = #%d", success? "Succeed" : "Failed", evaluation_id);
    }
}
