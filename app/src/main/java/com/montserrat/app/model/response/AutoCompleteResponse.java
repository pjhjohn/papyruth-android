package com.montserrat.app.model.response;

import com.montserrat.app.model.PartialCourse;

/**
 * Created by pjhjohn on 2015-05-27.
 */
public class AutoCompleteResponse {
    public String professor_name;
    public Integer professor_id;
    public String lecture_name;
    public Integer lecture_id;
    public  PartialCourse course;

    public AutoCompleteResponse(String lecture_name, Integer lecture_id, String professor_name, Integer professor_id, PartialCourse course){
        this.professor_name = professor_name;
        this.professor_id = professor_id;
        this.lecture_name = lecture_name;
        this.lecture_id = lecture_id;
        this.course = course;
    }
}
