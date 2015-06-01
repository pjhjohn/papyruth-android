package com.montserrat.app.model.response;

import com.montserrat.app.model.Comment;

import java.util.List;

/**
 * Created by SSS on 2015-05-31.
 */
public class CommentResponse {
    public Boolean success;
    public List<Comment> comments;

    @Override
    public String toString() {
        return String.format("%s with number of result = #%d", comments.size());
    }
}
