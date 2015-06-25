package com.montserrat.app.model.unique;

import com.montserrat.app.model.CourseData;

import java.util.List;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    private Integer id;
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
    private List<String> hashtags;


    private static Course instance = null;
    public static synchronized Course getInstance() {
        if( Course.instance == null ) Course.instance = new Course();
        return Course.instance;
    }
    public Course clear(){
        this.id = null;
        this.universityId = null;
        this.name = null;
        this.professor = null;
        this.professorId = null;
        this.lectureId = null;
        this.pointOverall = null;
        this.pointGpaSatisfaction = null;
        this.pointEasiness = null;
        this.pointClarity = null;
        this.evaluation_count = null;
        this.date = null;
        this.hashtags = null;
        return this;
    }

    @Override
    public String toString() {
        return String.format("<lecture:%s> <professor:%s> <course-id:%d> <overall:%d> <gpa satisfaction:%d> <easiness:%d> <clarity:%d>",
                name, professor, id, pointOverall, pointGpaSatisfaction, pointEasiness, pointClarity);
    }

    public void update(CourseData courseData){
        this.setId(courseData.id);
        this.setProfessor(courseData.professor_name);
        this.setName(courseData.name);
        this.setLectureId(courseData.lecture_id);
        this.setProfessorId(courseData.professor_id);
        this.setUniversityId(courseData.university_id);
        this.setEvaluationCount(courseData.evaluation_count);
        this.setPointOverall(courseData.point_overall);
        this.setPointEasiness(courseData.point_easiness);
        this.setPointGpaSatisfaction(courseData.point_gpa_satisfaction);
        this.setPointClarity(courseData.point_clarity);
        this.setDate(courseData.created_at);
        this.setHashtags(courseData.hashtags);
    }

    public Integer getId() { return id; }
    public Integer getUniversityId() { return universityId; }
    public String getName() { return name; }
    public Integer getPointClarity() { return pointClarity; }
    public String getProfessor() { return professor; }
    public Integer getPointOverall() { return pointOverall; }
    public Integer getPointGpaSatisfaction() { return pointGpaSatisfaction; }
    public Integer getPointEasiness() { return pointEasiness; }
    public Integer getLectureId() { return lectureId; }
    public Integer getProfessorId() { return professorId; }
    public List<String> getHashtags() { return hashtags; }
    public Integer getEvaluationCount() { return evaluation_count; }
    public String getDate() { return date; }

    public Course setId(Integer id) {
        this.id = id;
        return this;
    }

    public Course setUniversityId(Integer universityId) {
        this.universityId = universityId;
        return this;
    }

    public Course setName(String name) {
        this.name = name;
        return this;
    }

    public Course setProfessor(String professor) {
        this.professor = professor;
        return this;
    }

    public Course setProfessorId(Integer professorId) {
        this.professorId = professorId;
        return this;
    }

    public Course setLectureId(Integer lectureId) {
        this.lectureId = lectureId;
        return this;
    }

    public Course setPointOverall(Integer pointOverall) {
        this.pointOverall = pointOverall;
        return this;
    }

    public Course setPointGpaSatisfaction(Integer pointGpaSatisfaction) {
        this.pointGpaSatisfaction = pointGpaSatisfaction;
        return this;
    }

    public Course setPointEasiness(Integer pointEasiness) {
        this.pointEasiness = pointEasiness;
        return this;
    }

    public Course setPointClarity(Integer pointClarity) {
        this.pointClarity = pointClarity;
        return this;
    }

    public Course setEvaluationCount(Integer evaluation_count) {
        this.evaluation_count = evaluation_count;
        return this;
    }

    public Course setDate(String date) {
        this.date = date;
        return this;
    }

    public Course setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
        return this;
    }
}
