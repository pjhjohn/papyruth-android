package com.montserrat.parts.main;

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
    private Float scoreOverall;
    private Float scoreSatifaction;
    private Float scoreEasiness;
    private Float scoreLectureQuality;

    /* STEP 3 */
    private CharSequence description;

    public void clear() {
        this.lectureTitle       = null;
        this.professorName      = null;
        this.scoreOverall       = null;
        this.scoreSatifaction   = null;
        this.scoreEasiness      = null;
        this.scoreLectureQuality= null;
        this.description        = null;
    }


    public void setLectureTitle (CharSequence lectureTitle) {
        this.lectureTitle = lectureTitle;
    }

    public void setProfessorName (CharSequence professorName) {
        this.professorName = professorName;
    }

    public void setScoreOverall (Float scoreOverall) {
        this.scoreOverall = scoreOverall;
    }

    public void setScoreSatifaction (Float scoreSatifaction) {
        this.scoreSatifaction = scoreSatifaction;
    }

    public void setScoreEasiness (Float scoreEasiness) {
        this.scoreEasiness = scoreEasiness;
    }

    public void setScoreLectureQuality (Float scoreLectureQuality) {
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
