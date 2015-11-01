package com.montserrat.app.model.response;

import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.MyCommentData;

import java.util.List;

/**
 * Created by SSS on 2015-08-07.
 */
public class MyWrittenResponse {
    public boolean success;
    public List<EvaluationData> evaluations;
    public List<MyCommentData> comments;
}
