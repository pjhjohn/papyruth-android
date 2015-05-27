package com.montserrat.app.model.response;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class EvaluationResponse {
    public Boolean success;
    public Integer evaluation_id;

    @Override
    public String toString() {
        return String.format("%s with evaluation_id = #%d", success? "Succeed" : "Failed", evaluation_id);
    }
}
