package com.papyruth.android.model.unique;

import com.papyruth.android.model.UniversityData;
import com.papyruth.android.model.response.StatisticsResponse;

/**
 * Created by pjhjohn on 2015-05-15.
 */
public class Statistics {
    private static Statistics instance = null;
    public static synchronized Statistics getInstance() {
        if(Statistics.instance == null) Statistics.instance = new Statistics();
        return Statistics.instance;
    }
    private Statistics() {
        this.clear();
    }

    public UniversityData university;
    public Integer university_count;
    public Integer user_count;
    public Integer evaluation_count;

    public void clear() {
        this.university       = null;
        this.university_count = null;
        this.user_count       = null;
        this.evaluation_count = null;
    }

    public void update(StatisticsResponse response) {
        if(response == null) return;
        this.university       = response.university;
        this.university_count = response.university_count;
        this.user_count       = response.user_count;
        this.evaluation_count = response.evaluation_count;
    }

    @Override
    public String toString() {
        return String.format("university : [%s], university_count:%d, user_count:%d, evaluation_count:%d", university, university_count, user_count, evaluation_count);
    }

    public UniversityData getUniversity() {
        return university;
    }

    public void setUniversity(UniversityData university) {
        this.university = university;
    }

    public Integer getUniversityCount() {
        return university_count;
    }

    public void setUniversityCount(Integer university_count) {
        this.university_count = university_count;
    }

    public Integer getUserCount() {
        return user_count;
    }

    public void setUserCount(Integer user_count) {
        this.user_count = user_count;
    }

    public Integer getEvaluationCount() {
        return evaluation_count;
    }

    public void setEvaluationCount(Integer evaluation_count) {
        this.evaluation_count = evaluation_count;
    }
}