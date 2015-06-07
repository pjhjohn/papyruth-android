package com.montserrat.app.model.unique;

import com.montserrat.app.model.PartialCourse;

import java.util.List;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    private Integer id; // course id
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
    private Integer evaluation_count;
    private String date;
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

    public void fromPartailCourse(PartialCourse partialCourse){
        this.setId(partialCourse.id);
        this.setProfessor(partialCourse.professor_name);
        this.setName(partialCourse.name);
        this.setLectureId(partialCourse.lecture_id);
        this.setProfessorId(partialCourse.professor_id);
        this.setUnit(partialCourse.unit);
        this.setCode(partialCourse.code);
        this.setUniversityId(partialCourse.university_id);
        this.setEvaluation_count(partialCourse.evaluation_count);
        this.setPointOverall(partialCourse.point_overall);
        this.setPointEasiness(partialCourse.point_easiness);
        this.setPointGpaSatisfaction(partialCourse.point_gpa_satisfaction);
        this.setPointClarity(partialCourse.point_clarity);
        this.setDate(partialCourse.created_at);
        this.setTags(partialCourse.tag);
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
    public Integer getEvaluation_count() { return evaluation_count; }
    public String getDate() { return date; }

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
    public void setEvaluation_count(Integer evaluation_count) { this.evaluation_count = evaluation_count; }
    public void setDate(String date) { this.date = date; }
}
