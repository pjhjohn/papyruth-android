package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-05-27.
 */
public class Candidate {
    public String professor_name;
    public Integer professor_id;
    public String lecture_name;
    public Integer lecture_id;
    public CourseData course;

    public Candidate(String lecture_name, Integer lecture_id, String professor_name, Integer professor_id, CourseData course){
        this.professor_name = professor_name;
        this.professor_id = professor_id;
        this.lecture_name = lecture_name;
        this.lecture_id = lecture_id;
        this.course = course;
    }
}
