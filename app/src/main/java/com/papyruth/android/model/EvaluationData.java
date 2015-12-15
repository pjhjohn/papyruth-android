package com.papyruth.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-05-10.
 */
public class EvaluationData {
    public Integer id = null;
    public Integer user_id = null;
    public Integer course_id = null;
    public Integer point_overall = null;
    public Integer point_easiness = null;
    public Integer point_gpa_satisfaction = null;
    public Integer point_clarity = null;
    public String body = null;
    public String created_at = null;
    public String updated_at = null;
    public String professor_name = null;
    public String lecture_name = null;
    public String user_nickname = null;
    public Integer up_vote_count = null;
    public Integer down_vote_count = null;
    public Integer comment_count = null;
    public String avatar_url = null;
    public Integer request_user_vote = null; // 1 for up-vote, 0 for down-vote, null for neither.
    public Integer category;
    public List<String> hashtags = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("eval/user/couse id:%d/%d/%d\nprofessor:%s, lecture:%s\noverall/easiness/gpa-satisfaction/clarity point:%d/%d/%d/%d\nbody : %s",
            id, user_id, course_id, professor_name, lecture_name, point_overall, point_easiness, point_gpa_satisfaction, point_clarity, body
        );
    }
}
