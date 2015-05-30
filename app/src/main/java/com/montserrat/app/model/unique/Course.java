package com.montserrat.app.model.unique;

import java.util.List;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    private Integer id; // lecture id
    private Integer unit; // lecture unit
    private String code; // lecture code
    private Integer universityId;
    private String name;
    private String professor;
    private Integer professorId;
    private Integer lectureId;
    private Integer pointOverall;
    private Integer pointGpaSatisfaction;
    private Integer pointEasiness;
    private Integer pointClarity;
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
                name, professor, id, pointOverall, pointGpaSatisfaction, pointEasiness, pointClarity);
    }

    public Integer getId() { return id; }
    public Integer getUnit() { return unit; }
    public String getCode() { return code; }
    public Integer getUniversityId() { return universityId; }
    public String getName() { return name; }
    public Integer getPointClarity() { return pointClarity; }
    public String getProfessor() { return professor; }
    public Integer getPointOverall() { return pointOverall; }
    public Integer getPointGpaSatisfaction() { return pointGpaSatisfaction; }
    public Integer getPointEasiness() { return pointEasiness; }
    public Integer getLectureId() { return lectureId; }
    public Integer getProfessorId() { return professorId; }
    public List<String> getTags() { return tags; }

    public void setPointClarity(Integer pointClarity) { this.pointClarity = pointClarity; }
    public void setPointOverall(Integer pointOverall) { this.pointOverall = pointOverall; }
    public void setPointGpaSatisfaction(Integer pointGpaSatisfaction) { this.pointGpaSatisfaction = pointGpaSatisfaction; }
    public void setPointEasiness(Integer pointEasiness) { this.pointEasiness = pointEasiness; }
    public void setId(Integer id) { this.id = id; }
    public void setUnit(Integer unit) { this.unit = unit; }
    public void setCode(String code) { this.code = code; }
    public void setUniversityId(Integer universityId) { this.universityId = universityId; }
    public void setName(String name) { this.name = name; }
    public void setProfessor(String professor) { this.professor = professor; }
    public void setLectureId(Integer lectureId) { this.lectureId = lectureId; }
    public void setProfessorId(Integer professorId) { this.professorId = professorId; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
