package com.papyruth.android.model.unique;

import com.papyruth.android.model.EvaluationData;
import com.papyruth.support.opensource.rx.RxValidator;

import java.util.ArrayList;
import java.util.List;

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

    /* FLAG */
    private boolean isEdited;
    private boolean editMode;
    private Integer evaluationId;

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
    private List<String> hashtag;

    public Integer getEvaluationId() { return evaluationId; }
    public String  getLectureName () { return lectureName; }
    public String  getProfessorName() { return professorName; }
    public Integer getCourseId() { return courseId; }
    public Integer getPointOverall () { return pointOverall; }
    public Integer getPointGpaSatisfaction () { return pointGpaSatisfaction; }
    public Integer getPointEasiness () { return pointEasiness; }
    public Integer getPointClarity () {  return pointClarity; }
    public String getBody() {  return body; }
    public List<String> getHashtag() {  return hashtag; }
    public boolean isEdited() { return isEdited; }
    public boolean isEditMode() { return editMode; }

    public void setEvaluationId(Integer evaluationId) { this.evaluationId = evaluationId; }

    public void setLectureName (String lectureName) { this.lectureName = lectureName; }
    public void setProfessorName (String professorName) { this.professorName = professorName; }
    public void setCourseId (Integer courseId) { this.courseId = courseId; }
    public void setPointOverall (Integer pointOverall) { this.pointOverall = pointOverall; }
    public void setPointGpaSatisfaction (Integer pointGpaSatisfaction) { this.pointGpaSatisfaction = pointGpaSatisfaction; }
    public void setPointEasiness (Integer pointEasiness) { this.pointEasiness = pointEasiness; }
    public void setPointClarity (Integer pointClarity) { this.pointClarity = pointClarity; }
    public void setBody(String body) { this.body = body; }
    public void setHashtag(List<String> hashtag){
        this.hashtag.clear();
        this.hashtag.addAll(hashtag);
    }
    public void setEdited(boolean isEdit) { this.isEdited = isEdit; }

    public void addHashtag(String hashtag){ this.hashtag.add(hashtag); }
    public boolean removeHashtag(String text){
        if(this.hashtag.contains(text)) {
            this.hashtag.remove(text);
            return true;
        }
        return false;
    }

    public void initForEdit(Evaluation evaluation) {
        this.editMode = true;
        this.evaluationId = evaluation.getId();
        this.courseId = evaluation.getCourseId();
        this.lectureName = evaluation.getLectureName();
        this.professorName = evaluation.getProfessorName();
        this.pointOverall = evaluation.getPointOverall();
        this.pointGpaSatisfaction = evaluation.getPointGpaSatisfaction();
        this.pointEasiness = evaluation.getPointEasiness();
        this.pointClarity = evaluation.getPointClarity();
        this.body = evaluation.getBody();
        this.hashtag = evaluation.getHashTag();
    }
    public void initForEdit(EvaluationData evaluation) {
        this.editMode = true;
        this.evaluationId = evaluation.id;
        this.courseId = evaluation.course_id;
        this.lectureName = evaluation.lecture_name;
        this.professorName = evaluation.professor_name;
        this.pointOverall = evaluation.point_overall;
        this.pointGpaSatisfaction = evaluation.point_gpa_satisfaction;
        this.pointEasiness = evaluation.point_easiness;
        this.pointClarity = evaluation.point_clarity;
        this.body = evaluation.body;
        this.hashtag.addAll(evaluation.hashtags);
    }

    public void free(){
        clear();
        EvaluationForm.instance = null;
    }
    public EvaluationForm clear() {
        this.lectureName            = null;
        this.professorName          = null;
        this.courseId               = null;
        this.pointOverall           = null;
        this.pointGpaSatisfaction   = null;
        this.pointEasiness          = null;
        this.pointClarity           = null;
        this.body                   = null;
        if(hashtag == null) this.hashtag = new ArrayList<>();
        else this.hashtag.clear();
        this.isEdited = false;
        this.editMode = false;
        return this;
    }

    public boolean isNextStep(){
        return
            this.lectureName    != null &&
            this.professorName  != null &&
            this.courseId       != null &&
            RxValidator.isIntegerValueInRange.call(this.pointOverall) &&
            RxValidator.isIntegerValueInRange.call(this.pointGpaSatisfaction) &&
            RxValidator.isIntegerValueInRange.call(this.pointEasiness) &&
            RxValidator.isIntegerValueInRange.call(this.pointClarity);
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
            this.body                  != null &&
            ( !this.editMode || this.isEdited);

    }

    @Override
    public String toString() {
        return String.format("<lecture:%s> <professor:%s> <course-id:%d> <overall:%d> <gpa satisfaction:%d> <easiness:%d> <clarity:%d> <body:%s>",
            lectureName, professorName, courseId, pointOverall, pointGpaSatisfaction, pointEasiness, pointClarity, body);
    }
}
