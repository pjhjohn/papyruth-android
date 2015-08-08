package com.montserrat.app.model.unique;

/**
 * Created by pjhjohn on 2015-04-26.
 */
public class EvaluationForm {
    private static EvaluationForm instance = null;
    private EvaluationForm() {
        this.clear();
    }
    public static synchronized EvaluationForm getInstance() {
        if( EvaluationForm.instance == null ) EvaluationForm.instance = new EvaluationForm();
        return EvaluationForm.instance;
    }

    /* STEP 1*/
    private String lectureName;
    private String professorName;
    private Integer courseId;

    /* STEP 2 */
    private Integer pointOverall;
    private Integer pointGpaSatisfaction;
    private Integer pointEasiness;
    private Integer pointClarity;

    /* STEP 3 */
    private String body;

    public String  getLectureName () { return lectureName; }
    public String  getProfessorName() { return professorName; }
    public Integer getCourseId() { return courseId; }
    public Integer getPointOverall () { return pointOverall; }
    public Integer getPointGpaSatisfaction () { return pointGpaSatisfaction; }
    public Integer getPointEasiness () { return pointEasiness; }
    public Integer getPointClarity () {  return pointClarity; }
    public String getBody() {  return body; }

    public void setLectureName (String lectureName) { this.lectureName = lectureName; }
    public void setProfessorName (String professorName) { this.professorName = professorName; }
    public void setCourseId (Integer courseId) { this.courseId = courseId; }
    public void setPointOverall (Integer pointOverall) { this.pointOverall = pointOverall; }
    public void setPointGpaSatisfaction (Integer pointGpaSatisfaction) { this.pointGpaSatisfaction = pointGpaSatisfaction; }
    public void setPointEasiness (Integer pointEasiness) { this.pointEasiness = pointEasiness; }
    public void setPointClarity (Integer pointClarity) { this.pointClarity = pointClarity; }
    public void setBody(String body) { this.body = body; }

    public void clear() {
        this.clear(false);
    }
    public EvaluationForm clear(boolean soft) {
        if(!soft) {
            this.lectureName        = null;
            this.professorName      = null;
            this.courseId           = null;
        }
        this.pointOverall           = null;
        this.pointGpaSatisfaction   = null;
        this.pointEasiness          = null;
        this.pointClarity           = null;
        this.body                   = null;
        return this;
    }

    public boolean isCompleted() {
        return
            this.lectureName           != null &&
            this.professorName         != null &&
            this.courseId              != null &&
            this.pointOverall          != null &&
            this.pointGpaSatisfaction  != null &&
            this.pointEasiness         != null &&
            this.pointClarity          != null &&
            this.body                  != null;
    }

    @Override
    public String toString() {
        return String.format("<lecture:%s> <professor:%s> <course-id:%d> <overall:%d> <gpa satisfaction:%d> <easiness:%d> <clarity:%d> <body:%s>",
            lectureName, professorName, courseId, pointOverall, pointGpaSatisfaction, pointEasiness, pointClarity, body);
    }
}
