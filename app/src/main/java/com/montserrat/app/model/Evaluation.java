package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-05-10.
 */
public class Evaluation {
    public String comment;
    public int id;
    public int user_id;
    public int course_id;
    public int point_overall;
    public int point_gpa_satisfaction;
    public int point_clarity;
    public int point_easyness;

    @Override
    public String toString() {
        return String.format("%s %d %d %d %d %d %d %d", comment, id, user_id, course_id, point_overall, point_gpa_satisfaction, point_clarity, point_easyness);
    }
}
