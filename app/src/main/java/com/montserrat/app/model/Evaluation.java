package com.montserrat.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 */
public class Evaluation {
    private static Evaluation instance = null;

    public static synchronized Evaluation getInstance() {
        if( Evaluation.instance == null ) Evaluation.instance = new Evaluation();
        return Evaluation.instance;
    }

    /* STEP 1*/
    private CharSequence lectureTitle;
    private CharSequence professorName;

    /* STEP 2 */
    private Integer scoreOverall;
    private Integer scoreSatifaction;
    private Integer scoreEasiness;
    private Integer scoreLectureQuality;

    public CharSequence getLectureTitle() { return lectureTitle; }
    public CharSequence getProfessorName() { return professorName; }
    public Integer getScoreOverall() { return scoreOverall; }
    public Integer getScoreSatifaction() { return scoreSatifaction; }
    public Integer getScoreEasiness() { return scoreEasiness; }
    public Integer getScoreLectureQuality() {  return scoreLectureQuality;  }
    public CharSequence getDescription() {  return description;  }
    public JSONObject getData(){
        JSONObject data = new JSONObject();
        try {
            data.put("course_id", 1)
                    .put("score_overall", this.scoreOverall)
                    .put("score_satisfaction", this.scoreSatifaction)
                    .put("score_easiness", this.scoreEasiness)
                    .put("score_lecture_quality", this.scoreLectureQuality)
                    .put("description", this.description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    /* STEP 3 */
    private CharSequence description;

    public void clear() {
        this.lectureTitle       = null;
        this.professorName      = null;
        this.scoreOverall       = null;
        this.scoreSatifaction   = -1;
        this.scoreEasiness      = -1;
        this.scoreLectureQuality= -1;
        this.description        = null;
    }


    public void setLectureTitle (CharSequence lectureTitle) {
        this.lectureTitle = lectureTitle;
    }

    public void setProfessorName (CharSequence professorName) {
        this.professorName = professorName;
    }

    public void setScoreOverall (Integer scoreOverall) {
        this.scoreOverall = scoreOverall;
    }

    public void setScoreSatifaction (Integer scoreSatifaction) {
        this.scoreSatifaction = scoreSatifaction;
    }

    public void setScoreEasiness (Integer scoreEasiness) {
        this.scoreEasiness = scoreEasiness;
    }

    public void setScoreLectureQuality (Integer scoreLectureQuality) {
        this.scoreLectureQuality = scoreLectureQuality;
    }

    public void setDescription (CharSequence description) {
        this.description = description;
    }

    /* TODO : link to Viewpager last page memorization */
    public int getCompletionLevel() {
        /* STEP 1 */
        if ( this.lectureTitle == null || this.professorName == null ) return 0;
        /* STEP 2 */
        if ( this.scoreOverall == null || this.scoreSatifaction == null || this.scoreEasiness == null || this.scoreLectureQuality == null ) return 1;
        /* STEP 3*/
        if ( this.description == null ) return 2;
        return 3;
    }
}
