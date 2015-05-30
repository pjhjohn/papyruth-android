package com.montserrat.app.model.unique;

import java.util.List;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    private Integer id; // lecture id
    private Integer unit; // lecture unit
    private String code; // lecture code
    private Integer university_id;
    private String name;
    private String professor;
    private Integer professor_id;
    private Integer lecture_id;
    private Integer point_overall;
    private Integer point_gpa_satisfaction;
    private Integer point_easiness;
    private Integer point_clarity;
    private List<String> tags;


    private static Course instance = null;
    public static synchronized Course getInstance() {
        if( Course.instance == null ) Course.instance = new Course();
        return Course.instance;
    }
    public void clear(){
        if( Course.instance != null) Course.instance = null;
    }

    @Override
    public String toString() {
        return String.format("<lecture:%s> <professor:%s> <course-id:%d> <overall:%d> <gpa satisfaction:%d> <easiness:%d> <clarity:%d>",
                name, professor, id, point_overall, point_gpa_satisfaction, point_easiness, point_clarity);
    }

    public Integer getId() { return id; }
    public Integer getUnit() { return unit; }
    public String getCode() { return code; }
    public Integer getUniversity_id() { return university_id; }
    public String getName() { return name; }
    public Integer getPointClarity() { return point_clarity; }
    public String getProfessor() { return professor; }
    public Integer getPoint_overall() { return point_overall; }
    public Integer getPointGpaSatisfaction() { return point_gpa_satisfaction; }
    public Integer getPointEasiness() { return point_easiness; }
    public Integer getLecture_id() { return lecture_id; }
    public Integer getProfessor_id() { return professor_id; }
    public List<String> getTags() { return tags; }

    public void setPointClarity(Integer pointClarity) { this.point_clarity = pointClarity; }
    public void setPoint_overall(Integer point_overall) { this.point_overall = point_overall; }
    public void setPointGpaSatisfaction(Integer pointGpaSatisfaction) { this.point_gpa_satisfaction = pointGpaSatisfaction; }
    public void setPointEasiness(Integer pointEasiness) { this.point_easiness = pointEasiness; }
    public void setId(Integer id) { this.id = id; }
    public void setUnit(Integer unit) { this.unit = unit; }
    public void setCode(String code) { this.code = code; }
    public void setUniversity_id(Integer university_id) { this.university_id = university_id; }
    public void setName(String name) { this.name = name; }
    public void setProfessor(String professor) { this.professor = professor; }
    public void setLecture_id(Integer lecture_id) { this.lecture_id = lecture_id; }
    public void setProfessor_id(Integer professor_id) { this.professor_id = professor_id; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
