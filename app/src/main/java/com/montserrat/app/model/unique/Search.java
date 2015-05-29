package com.montserrat.app.model.unique;

import com.montserrat.app.model.PartialCourse;

/**
 * Created by SSS on 2015-05-30.
 */
public class Search {
    private String professor_name;
    private Integer professor_id;
    private String lecture_name;
    private Integer lecture_id;
    private PartialCourse course;

    public String getProfessor_name() { return professor_name; }
    public void setProfessor_name(String professor_name) { this.professor_name = professor_name; }
    public Integer getProfessor_id() { return professor_id; }
    public void setProfessor_id(Integer professor_id) { this.professor_id = professor_id; }
    public String getLecture_name() { return lecture_name; }
    public void setLecture_name(String lecture_name) { this.lecture_name = lecture_name; }
    public Integer getLecture_id() { return lecture_id; }
    public void setLecture_id(Integer lecture_id) { this.lecture_id = lecture_id; }
    public PartialCourse getCourse() { return course; }
    public void setCourse(PartialCourse course) { this.course = course; }

    private static Search instance = null;
    public static synchronized Search getInstance() {
        if( Search.instance == null ) Search.instance = new Search();
        return Search.instance;
    }

    public static synchronized boolean isInstance(){
        if( Search.instance == null )
            return false;
        else
            return true;
    }
    public void clear(){
        if( Search.instance != null) Search.instance = null;
    }
}
