package com.papyruth.android.model;

/**
 * Created by pjhjohn on 2015-05-27.
 */
public class CandidateData {
    public Integer course_id;
    public String professor_name;
    public Integer professor_id;
    public String lecture_name;
    public Integer lecture_id;

    public CandidateData() {
        this.course_id      = null;
        this.professor_id   = null;
        this.professor_name = null;
        this.lecture_id     = null;
        this.lecture_name   = null;
    }

    public CandidateData(String lecture_name, Integer lecture_id, String professor_name, Integer professor_id, CourseData course){
        this.professor_name = professor_name;
        this.professor_id   = professor_id;
        this.lecture_name   = lecture_name;
        this.lecture_id     = lecture_id;
    }

    public void clear() {
        this.course_id = null;
        this.professor_id = null;
        this.professor_name = null;
        this.lecture_id = null;
        this.lecture_name = null;
    }

    public boolean isEmpty(){
        return this.course_id == null && this.professor_id == null && this.professor_name == null && this.lecture_id == null && this.lecture_name == null;
    }

    @Override
    public String toString() {
        return String.format("course : id - <%s>  professor : id-<%s>, name-<%s>  lecture : id-<%s>, name-<%s>", course_id, professor_id, professor_name, lecture_id, lecture_name);
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        CandidateData candidate = (CandidateData) object;
        return (course_id != null && course_id.equals(candidate.course_id))
            || (this.lecture_id!= null && this.lecture_id.equals(candidate.lecture_id))
            || (this.professor_id!=null && this.professor_id.equals(candidate.professor_id));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = (37 * hash) + (null == course_id    ? 0 :    course_id.hashCode());
        hash = (37 * hash) + (null == lecture_id   ? 0 :   lecture_id.hashCode());
        hash = (37 * hash) + (null == professor_id ? 0 : professor_id.hashCode());
        return hash;
    }
}
