package com.montserrat.app.model;

import java.util.List;

/**
 * Created by SSS on 2015-05-22.
 */
public class CommentData {
    public Integer id;
    public Integer evaluation_id;
    public Integer user_id;
    public String body;
    public String created_at;
    public String updated_at;
    public String user_nickname;
    public String up_vote_count;
    public List<VoteData> up_votes;
    public String down_vote_count;
    public List<VoteData> down_votes;
    public String avatar_url;
}
