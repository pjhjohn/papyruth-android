package com.montserrat.app.model.unique;

import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;

/**
 * Created by SSS on 2015-05-30.
 */
public class Search {
    private static Search instance = null;
    private Search() {
        clear();
    }
    public static synchronized Search getInstance() {
        if(Search.instance == null) Search.instance = new Search();
        return Search.instance;
    }

    private String professor_name;
    private Integer professor_id;
    private String lecture_name;
    private Integer lecture_id;
    private CourseData course;
    private String query;

    public String getProfessorName() {
        return professor_name;
    }
    public Search setProfessorName(String professor_name) {
        this.professor_name = professor_name;
        return this;
    }
    public Integer getProfessorId() { return professor_id;

    }
    public Search setProfessorId(Integer professor_id) {
        this.professor_id = professor_id;
        return this;
    }
    public String getLectureName() {
        return lecture_name;
    }
    public Search setLectureName(String lecture_name) {
        this.lecture_name = lecture_name;
        return this;
    }
    public Integer getLectureId() {
        return lecture_id;
    }
    public Search setLectureId(Integer lecture_id) {
        this.lecture_id = lecture_id;
        return this;
    }
    public CourseData getCourse() {
        return course;
    }
    public Search setCourse(CourseData course) {
        this.course = course;
        return this;
    }
    public String getQuery() {
        return query;
    }
    public Search setQuery(String query) {
        this.query = query;
        return this;
    }

    public Search clear(){
        this.professor_name = null;
        this.professor_id = null;
        this.lecture_name = null;
        this.lecture_id = null;
        this.course = null;
        this.query = null;
        return this;
    }

    public void fromCandidate(Candidate candidate){
        clear()
                .setCourse(candidate.course)
                .setLectureId(candidate.lecture_id)
                .setLectureName(candidate.lecture_name)
                .setProfessorId(candidate.professor_id)
                .setProfessorName(candidate.professor_name);
    }

    public boolean isEmpty() {
        return
            this.professor_name == null &&
            this.professor_id == null &&
            this.lecture_name == null &&
            this.lecture_id == null &&
            this.course == null &&
            this.query == null;
    }

    @Override
    public String toString() {
        return String.format("Search/lecture : %s, %d professor : %s %d query : %s",
                this.lecture_name, this.lecture_id, this.professor_name, this.professor_id, this.query);
    }
}
