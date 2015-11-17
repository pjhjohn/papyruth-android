package com.papyruth.android.model;

/**
 * Created by pjhjohn on 2015-05-27.
 */
public class Candidate {
    public String professor_name;
    public Integer professor_id;
    public String lecture_name;
    public Integer lecture_id;

    public Candidate(){
        this.professor_id = null;
        this.professor_name = null;
        this.lecture_id = null;
        this.lecture_name = null;
    }

    public Candidate(String lecture_name, Integer lecture_id, String professor_name, Integer professor_id, CourseData course){
        this.professor_name = professor_name;
        this.professor_id = professor_id;
        this.lecture_name = lecture_name;
        this.lecture_id = lecture_id;
    }
    public void clear(){
        this.professor_id = null;
        this.professor_name = null;
        this.lecture_id = null;
        this.lecture_name = null;
    }

    @Override
    public String toString() {
        return String.format("professor : id-<%s>, name-<%s>  lecture : id-<%s>, name-<%s>", professor_id, professor_name, lecture_id, lecture_name);
    }
}
