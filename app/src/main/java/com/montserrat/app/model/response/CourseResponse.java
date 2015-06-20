package com.montserrat.app.model.response;

import com.montserrat.app.model.CourseData;

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
