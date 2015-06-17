package com.montserrat.app.model;

import com.montserrat.app.model.unique.Course;

/**
 * Created by pjhjohn on 2015-06-18.
 */
public class Favorite {
    public Integer id;
    public Integer user_id;
    public Integer course_id;
    public Course course;
    public String created_at;
    public String updated_at;
}
