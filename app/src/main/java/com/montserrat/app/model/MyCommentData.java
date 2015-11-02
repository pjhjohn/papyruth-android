package com.montserrat.app.model;

/**
 * Created by SSS on 2015-05-22.
 */
public class MyCommentData{
    public Integer id;
    public Integer evaluation_id;
    public Integer user_id;
    public String body;
    public String created_at;
    public String updated_at;
    public String user_nickname;
    public Integer up_vote_count;
    public Integer down_vote_count;
    public Integer request_user_vote; // 1 for up-vote, 0 for down-vote, null for neither.
    public String avatar_url;

    public String lecture_name;
    public String professor_name;
    public String category;
}
