package com.montserrat.app.model;

/**
 * Created by pjhjohn on 2015-05-10.
 */
public class EvaluationData {
    public Integer id;
    public Integer user_id;
    public Integer course_id;
    public Integer point_overall;
    public Integer point_easiness;
    public Integer point_gpa_satisfaction;
    public Integer point_clarity;
    public String body;
    public String created_at;
    public String updated_at;
    public String professor_name;
    public String lecture_name;
    public String user_nickname;
    public Integer up_vote_count;
    public Integer down_vote_count;
    public Integer comment_count;

    @Override
    public String toString() {
        return String.format("eval/user/couse id:%d/%d/%d\nprofessor:%s, lecture:%s\noverall/easiness/gpa-satisfaction/clarity point:%d/%d/%d/%d\nbody : %s",
            id, user_id, course_id, professor_name, lecture_name, point_overall, point_easiness, point_gpa_satisfaction, point_clarity, body
        );
    }
}
