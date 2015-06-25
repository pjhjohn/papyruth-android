package com.montserrat.app.model;

import java.util.List;

/**
 * Created by pjhjohn on 2015-05-03.
 */
public class CourseData {
    public Integer id;
    public Integer professor_id;
    public Integer lecture_id;
    public Integer university_id;
    public Integer evaluation_count;
    public Integer point_overall;
    public Integer point_gpa_satisfaction;
    public Integer point_easiness;
    public Integer point_clarity;
    public String created_at;
    public String updated_at;
    public String name;
    public String professor_name;
    public List<String> hashtags;
    public Boolean is_favorite;

    public static CourseData Sample(){
        CourseData courseData = new CourseData();
        courseData.id = -1;
        courseData.created_at = "";
        courseData.updated_at = "";
        courseData.name = "";
        courseData.professor_name = "";
        courseData.professor_id = -1;
        courseData.lecture_id = -1;
        courseData.university_id = -1;
        courseData.evaluation_count = -1;
        courseData.point_overall = -1;
        courseData.point_gpa_satisfaction = -1;
        courseData.point_easiness = -1;
        courseData.point_clarity = -1;

        return courseData;
    }
}
