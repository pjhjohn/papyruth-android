package com.papyruth.android.model.unique;

import com.papyruth.android.model.EvaluationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SSS on 2015-05-24.
 */
public class Evaluation {
    private Integer id;
    private Integer user_id;
    private String user_nickname;
    private Integer course_id;
    private Integer point_overall;
    private Integer point_easiness;
    private Integer point_gpa_satisfaction;
    private Integer point_clarity;
    private String body;
    private String created_at;
    private String updated_at;
    private String professor_name;
    private String lecture_name;
    private Integer up_vote_count;
    private Integer down_vote_count;
    private Integer comment_count;
    private String avatar_url;
    private Integer request_user_vote; // 1 for up-vote, 0 for down-vote, null for neither.
    private List<String> hashTag;
    public Integer category;
    private Boolean university_confirmation_needed;

    private static Evaluation instance = null;
    private Evaluation() {
        this.hashTag = new ArrayList<>();
        this.clear();
    }
    public synchronized static Evaluation getInstance(){
        if ( Evaluation.instance == null ) Evaluation.instance = new Evaluation();
        return Evaluation.instance;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return user_id; }
    public void setUserId(Integer user_id) { this.user_id = user_id; }
    public Integer getCourseId() { return course_id; }
    public void setCourseId(Integer course_id) { this.course_id = course_id; }
    public Integer getPointOverall() { return point_overall; }
    public void setPointOverall(Integer point_overall) { this.point_overall = point_overall; }
    public Integer getPointEasiness() { return point_easiness; }
    public void setPointEasiness(Integer point_easiness) { this.point_easiness = point_easiness; }
    public Integer getPointGpaSatisfaction() { return point_gpa_satisfaction; }
    public void setPointGpaSatisfaction(Integer point_gpa_satisfaction) { this.point_gpa_satisfaction = point_gpa_satisfaction; }
    public Integer getPointClarity() { return point_clarity; }
    public void setPointClarity(Integer point_clarity) { this.point_clarity = point_clarity; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCreatedAt() { return created_at; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public String getUpdatedAt() { return updated_at; }
    public void setUpdatedAt(String updated_at) { this.updated_at = updated_at; }
    public String getProfessorName() { return professor_name; }
    public void setProfessorName(String professor_name) { this.professor_name = professor_name; }
    public String getLectureName() { return lecture_name; }
    public void setLectureName(String lecture_name) { this.lecture_name = lecture_name; }
    public String getUserNickname() { return user_nickname; }
    public void setUserNickname(String user_nickname) { this.user_nickname = user_nickname; }
    public Integer getUpVoteCount() { return up_vote_count; }
    public void setUpVoteCount(Integer up_vote_count) { this.up_vote_count = up_vote_count; }
    public Integer getDownVoteCount() { return down_vote_count; }
    public void setDownVoteCount(Integer down_vote_count) { this.down_vote_count = down_vote_count; }
    public Integer getCommentCount() { return comment_count; }
    public void setCommentCount(Integer comment_count) { this.comment_count = comment_count; }
    public String getAvatarUrl() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }
    public Integer getRequestUserVote() { return request_user_vote; }
    public void setRequestUserVote(Integer request_user_vote) { this.request_user_vote = request_user_vote; }
    public List<String> getHashTag() { return hashTag; }
    public void setHashTag(List<String> hashTag) {
        this.hashTag.clear();
        this.hashTag = hashTag;
    }
    public void addHashTag(String hashtag){ this.hashTag.add(hashtag); }
    public Integer getCategory() { return this.category; }
    public void setCategory(Integer category) { this.category = category; }
    public Boolean getUniversityConfirmationNeeded() {return university_confirmation_needed == null ? false : university_confirmation_needed;}
    public void setUniversityConfirmationNeeded(Boolean university_confirmation_needed) {this.university_confirmation_needed = university_confirmation_needed;}

    public boolean hasContents(){
        return this.body != null && this.id != null && this.user_id != null && this.course_id != null;
    }

    public void update(EvaluationData evaluation) {
        if(evaluation.id != null)                       this.id = evaluation.id;
        if(evaluation.user_id != null)                  this.user_id = evaluation.user_id;
        if(evaluation.course_id != null)                this.course_id = evaluation.course_id;
        if(evaluation.point_overall != null)            this.point_overall = evaluation.point_overall;
        if(evaluation.point_easiness != null)           this.point_easiness = evaluation.point_easiness;
        if(evaluation.point_gpa_satisfaction != null)   this.point_gpa_satisfaction = evaluation.point_gpa_satisfaction;
        if(evaluation.point_clarity != null)            this.point_clarity = evaluation.point_clarity;
        if(evaluation.body != null)                     this.body = evaluation.body;
        if(evaluation.created_at != null)               this.created_at = evaluation.created_at;
        if(evaluation.updated_at != null)               this.updated_at = evaluation.updated_at;
        if(evaluation.professor_name != null)           this.professor_name = evaluation.professor_name;
        if(evaluation.lecture_name != null)             this.lecture_name = evaluation.lecture_name;
        if(evaluation.user_nickname != null)            this.user_nickname = evaluation.user_nickname;
        if(evaluation.up_vote_count != null)            this.up_vote_count = evaluation.up_vote_count;
        if(evaluation.down_vote_count != null)          this.down_vote_count = evaluation.down_vote_count;
        if(evaluation.comment_count != null)            this.comment_count = evaluation.comment_count;
        if(evaluation.avatar_url != null)               this.avatar_url = evaluation.avatar_url;
        if(evaluation.lecture_category != null)         this.category = evaluation.lecture_category;
        if(evaluation.university_confirmation_needed != null) this.university_confirmation_needed = evaluation.university_confirmation_needed;
        if(!evaluation.hashtags.isEmpty()) {
            this.hashTag.clear();
            this.hashTag.addAll(evaluation.hashtags);
        }
        this.request_user_vote = evaluation.request_user_vote; // TODO : verify data consistency holds without null check
    }

    public void clear(){
        this.id = null;
        this.user_id = null;
        this.user_nickname = null;
        this.course_id = null;
        this.point_overall = null;
        this.point_clarity = null;
        this.point_easiness = null;
        this.point_gpa_satisfaction = null;
        this.body = null;
        this.created_at = null;
        this.updated_at = null;
        this.professor_name = null;
        this.lecture_name = null;
        this.up_vote_count = null;
        this.down_vote_count = null;
        this.comment_count = null;
        this.avatar_url = null;
        this.request_user_vote = null;
        this.hashTag.clear();
        this.category = null;
        this.university_confirmation_needed = null;
    }
}
