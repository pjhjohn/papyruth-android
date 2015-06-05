package com.montserrat.app.model;

import java.util.List;

/**
 * Created by pjhjohn on 2015-05-03.
 */
public class PartialCourse {
    public Integer id; // lecture id
    public Integer unit; // lecture unit
    public String code; // lecture code
    public Integer university_id;
    public Integer lecture_id;
    public Integer professor_id;
    public String name;
    public String professor_name;
    public Float rating;
    public Integer evaluation_count;
    public Integer point_overall;
    public Integer point_gpa_satisfaction;
    public Integer point_easiness;
    public Integer point_clarity;
    public String created_at;
    public String updated_at;
    public List<String> tag;
}
