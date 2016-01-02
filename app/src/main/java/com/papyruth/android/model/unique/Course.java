package com.papyruth.android.model.unique;

import com.papyruth.android.model.CourseData;

import java.util.List;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    private Integer id;
    private Boolean isFavorite;
    private Integer pointGpaSatisfaction;
    private Integer lectureId;
    private Integer professorId;
    private String createdAt;
    private Integer evaluationCount;
    private Integer universityId;
    private Integer pointClarity;
    private Integer pointOverall;
    private String professorName;
    private String updatedAt;
    private Integer pointEasiness;
    private List<String> hashtags;
    private String professorPhotoUrl;
    private String name;
    private Integer category;

    private static Course instance = null;
    public static synchronized Course getInstance() {
        if( Course.instance == null ) Course.instance = new Course();
        return Course.instance;
    }
    public Course clear(){
        this.id = null;
        this.isFavorite = null;
        this.pointGpaSatisfaction = null;
        this.lectureId = null;
        this.professorId = null;
        this.createdAt = null;
        this.evaluationCount = null;
        this.universityId = null;
        this.pointClarity = null;
        this.pointOverall = null;
        this.professorName = null;
        this.updatedAt = null;
        this.pointEasiness = null;
        this.hashtags = null;
        this.professorPhotoUrl = null;
        this.name = null;
        return this;
    }

    public void update(CourseData courseData){
        this.setId(courseData.id);
        this.setIsFavorite(courseData.is_favorite);
        this.setPointGpaSatisfaction(courseData.point_gpa_satisfaction);
        this.setLectureId(courseData.lecture_id);
        this.setProfessorId(courseData.professor_id);
        this.setCreatedAt(courseData.created_at);
        this.setEvaluationCount(courseData.evaluation_count);
        this.setUniversityId(courseData.university_id);
        this.setPointClarity(courseData.point_clarity);
        this.setPointOverall(courseData.point_overall);
        this.setProfessorName(courseData.professor_name);
        this.setUpdatedAt(courseData.updated_at);
        this.setPointEasiness(courseData.point_easiness);
        this.setHashtags(courseData.hashtags);
        this.setProfessorPhotoUrl(courseData.professor_photo_url);
        this.setName(courseData.name);
    }

    public boolean needToUpdateData(){
        return this.lectureId == null || this.professorId == null || this.pointOverall == null;
    }

    public boolean isValidData(){
        return (pointOverall > 0
                && pointEasiness > 0
                && pointClarity > 0
                && pointGpaSatisfaction > 0
                && evaluationCount > 0 );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Integer getPointGpaSatisfaction() {
        return pointGpaSatisfaction;
    }

    public void setPointGpaSatisfaction(Integer pointGpaSatisfaction) {
        this.pointGpaSatisfaction = pointGpaSatisfaction;
    }

    public Integer getLectureId() {
        return lectureId;
    }

    public void setLectureId(Integer lectureId) {
        this.lectureId = lectureId;
    }

    public Integer getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Integer professorId) {
        this.professorId = professorId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(Integer evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public Integer getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Integer universityId) {
        this.universityId = universityId;
    }

    public Integer getPointClarity() {
        return pointClarity;
    }

    public void setPointClarity(Integer pointClarity) {
        this.pointClarity = pointClarity;
    }

    public Integer getPointOverall() {
        return pointOverall;
    }

    public void setPointOverall(Integer pointOverall) {
        this.pointOverall = pointOverall;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getPointEasiness() {
        return pointEasiness;
    }

    public void setPointEasiness(Integer pointEasiness) {
        this.pointEasiness = pointEasiness;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getProfessorPhotoUrl() {
        return professorPhotoUrl;
    }

    public void setProfessorPhotoUrl(String professorPhotoUrl) {
        this.professorPhotoUrl = professorPhotoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }
}
