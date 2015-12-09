package com.papyruth.android.model.response;

import com.papyruth.android.model.EvaluationData;
import com.papyruth.android.model.MyCommentData;

import java.util.List;

/**
 * Created by SSS on 2015-08-07.
 */
public class MyWrittenResponse {
    public boolean success;
    public List<EvaluationData> evaluations;
    public List<MyCommentData> comments;
}
