package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-04-26.
 */
public class EvaluationForm {
    private static EvaluationForm instance = null;
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
    private String comment;

    public String  getLectureName () { return lectureName; }
    public String  getProfessorName() { return professorName; }
    public Integer getCourseId() { return courseId; }
    public Integer getPointOverall () { return pointOverall; }
    public Integer getPointGpaSatisfaction () { return pointGpaSatisfaction; }
    public Integer getPointEasiness () { return pointEasiness; }
    public Integer getPointClarity () {  return pointClarity;  }
    public String  getComment () {  return comment;  }

    public void setLectureName (String lectureName) { this.lectureName = lectureName; }
    public void setProfessorName (String professorName) { this.professorName = professorName; }
    public void setCourseId (Integer courseId) { this.courseId = courseId; }
    public void setPointOverall (Integer pointOverall) { this.pointOverall = pointOverall; }
    public void setPointGpaSatisfaction (Integer pointGpaSatisfaction) { this.pointGpaSatisfaction = pointGpaSatisfaction; }
    public void setPointEasiness (Integer pointEasiness) { this.pointEasiness = pointEasiness; }
    public void setPointClarity (Integer pointClarity) { this.pointClarity = pointClarity; }
    public void setComment (String comment) { this.comment = comment; }

    public void clear() {
        this.lectureName            = null;
        this.professorName          = null;
        this.courseId               = null;
        this.pointOverall           = null;
        this.pointGpaSatisfaction   = null;
        this.pointEasiness          = null;
        this.pointClarity           = null;
        this.comment                = null;
    }

    /* TODO : link to Viewpager last page memorization */
    public int getCompletionLevel() {
        /* STEP 1 */
        if ( this.lectureName == null || this.professorName == null ) return 0;
        /* STEP 2 */
        if ( this.pointOverall == null || this.pointGpaSatisfaction == null || this.pointEasiness == null || this.pointClarity == null ) return 1;
        /* STEP 3*/
        if ( this.comment == null ) return 2;
        return 3;
    }

    @Override
    public String toString() {
        return String.format("<lecture:%s> <professor:%s> <course-id:%d> <overall:%d> <gpa satisfaction:%d> <easiness:%d> <clarity:%d> <body:%s>",
            lectureName, professorName, courseId, pointOverall, pointGpaSatisfaction, pointEasiness, pointClarity, comment);
    }

    public class ResponseData{
        public Boolean success;
        public Integer evaluation_id;

        @Override
        public String toString() {
            return String.format("%s with evaluation_id = #%d", success? "Succeed" : "Failed", evaluation_id);
        }
    }
}
