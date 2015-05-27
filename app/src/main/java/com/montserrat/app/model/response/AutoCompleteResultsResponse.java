package com.montserrat.app.model.response;

import java.util.List;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class AutoCompleteResultsResponse {
    public Boolean success;
    public List<AutoCompleteResponse> results;

    @Override
    public String toString() {
        return String.format("%s with number of result = #%d", success? "Succeed" : "Failed", results.size());
    }
}
