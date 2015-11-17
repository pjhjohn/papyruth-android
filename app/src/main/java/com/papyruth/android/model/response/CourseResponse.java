package com.papyruth.android.model.response;

import com.papyruth.android.model.CourseData;

/**
 * Created by pjhjohn on 2015-06-05.
 */
public class CourseResponse {
    public Boolean success;
    public CourseData course;

    @Override
    public String toString() {
        return String.format("Request %s with Course %s", this.success? "succeed" : "failure", this.course);
    }
}
