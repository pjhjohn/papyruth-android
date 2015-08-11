package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-06-18.
 */
public class FavoriteData {
    public Integer id;
    public Integer user_id;
    public Integer course_id;
    public CourseData course;
    public String created_at;
    public String updated_at;
    public FavoriteData(Integer id, Integer user_id, Integer course_id){
        this.id = id;
        this.user_id = user_id;
        this.course_id = course_id;
        this.course = new CourseData();
    }
}
