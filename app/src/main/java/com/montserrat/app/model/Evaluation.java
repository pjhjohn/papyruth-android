package com.montserrat.app.model;

/**
 * Created by SSS on 2015-05-24.
 */
public class Evaluation {
    private Integer id;
    private Integer user_id;
    private String user_name;
    private Integer course_id;
    private Integer point_overall;
    private Integer point_easiness;
    private Integer point_gpa_satisfaction;
    private Integer point_clarity;
    private String body;
    private String created_at;
    private String updated_at;
    private String professor_name;
    private String lecture_name;


    private static Evaluation instance = null;
    public synchronized static Evaluation getInstance(){
        if ( Evaluation.instance == null ) Evaluation.instance = new Evaluation();
        return Evaluation.instance;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUser_id() { return user_id; }
    public void setUser_id(Integer user_id) { this.user_id = user_id; }
    public Integer getCourse_id() { return course_id; }
    public void setCourse_id(Integer course_id) { this.course_id = course_id; }
    public Integer getPoint_overall() { return point_overall; }
    public void setPoint_overall(Integer point_overall) { this.point_overall = point_overall; }
    public Integer getPoint_easiness() { return point_easiness; }
    public void setPoint_easiness(Integer point_easiness) { this.point_easiness = point_easiness; }
    public Integer getPoint_gpa_satisfaction() { return point_gpa_satisfaction; }
    public void setPoint_gpa_satisfaction(Integer point_gpa_satisfaction) { this.point_gpa_satisfaction = point_gpa_satisfaction; }
    public Integer getPoint_clarity() { return point_clarity; }
    public void setPoint_clarity(Integer point_clarity) { this.point_clarity = point_clarity; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
    public String getProfessor_name() { return professor_name; }
    public void setProfessor_name(String professor_name) { this.professor_name = professor_name; }
    public String getLecture_name() { return lecture_name; }
    public void setLecture_name(String lecture_name) { this.lecture_name = lecture_name; }
    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }

    public void clear(){
        this.id = null;
        this.user_id = null;
        this.user_name = null;
        this.course_id = null;
        this.point_overall = null;
        this.point_clarity = null;
        this.point_easiness = null;
        this.point_gpa_satisfaction = null;
        this.body = null;
        this.created_at = null;
        this.updated_at = null;
        this.professor_name = null;
        this.lecture_name = null;
    }
}
