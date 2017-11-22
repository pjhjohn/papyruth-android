package com.papyruth.android.model.unique;

import com.papyruth.android.model.UniversityData;
import com.papyruth.android.model.UserData;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by pjhjohn on 2015-04-17.
 * Used for passing data during sign up process.
 */
public class User {
    private static User instance = null;
    public static synchronized User getInstance () {
        if ( User.instance == null ) User.instance = new User();
        return User.instance;
    }

    private String  access_token;
    private Integer id;
    private String  realname;
    private String  nickname;
    private String  email;
    private Integer entrance_year;
    private Integer university_id;
    private Boolean is_boy;
    private Boolean confirmed;
    private Integer mandatory_evaluation_count;
    private String  university_email;
    private Boolean university_confirmed;
    private String  university_name;
    private String  university_image_url;
    private String  avatar_url;
    private UniversityData university_data;

    private BehaviorSubject<String> bsNickname;
    private BehaviorSubject<String> bsEmail;
    private BehaviorSubject<String> bsAvatarUrl;


    private User () {
        this.clear();
        this.bsNickname = BehaviorSubject.create(this.nickname);
        this.bsEmail    = BehaviorSubject.create(this.email);
        this.bsAvatarUrl= BehaviorSubject.create(this.avatar_url);
    }

    public void clear() {
        this.access_token               = null;
        this.id                         = null;
        this.realname                   = null;
        this.nickname                   = null;
        this.email                      = null;
        this.entrance_year              = null;
        this.university_id              = null;
        this.is_boy                     = null;
        this.confirmed                  = null;
        this.mandatory_evaluation_count = null;
        this.university_email           = null;
        this.university_confirmed       = null;
        this.university_name            = null;
        this.university_image_url       = null;
        this.avatar_url                 = null;
    }

    public String getAccessToken () { return this.access_token; }
    public void setAccessToken (String token) {
        if (token != null && !token.isEmpty()) this.access_token = String.format("Token token=\"%s\"", token);
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRealname () { return this.realname; }
    public void setRealname (String realname) { this.realname = realname; }
    public String getNickname() { return this.nickname; }
    public void setNickName(String nickname) {
        this.nickname = nickname;
        this.bsNickname.onNext(nickname);
    }
    public String getEmail() { return this.email; }
    public void setEmail(String email) {
        this.email = email;
        this.bsEmail.onNext(email);
    }
    public Integer getEntranceYear() { return this.entrance_year; }
    public void setEntranceYear (Integer entrance_year) { this.entrance_year = entrance_year; }
    public Integer getUniversityId() { return this.university_id; }
    public void setUniversityId (Integer university_id) { this.university_id = university_id; }
    public Boolean getGenderIsBoy() { return this.is_boy; }
    public void setGenderIsBoy (boolean is_boy) { this.is_boy = is_boy; }

    public Boolean getConfirmed() { return confirmed; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
    public Integer getMandatoryEvaluationCount() { return mandatory_evaluation_count; }
    public void setMandatoryEvaluationCount(Integer mandatory_evaluation_count) { this.mandatory_evaluation_count = mandatory_evaluation_count; }

    public String getUniversityEmail() { return university_email; }
    public void setUniversityEmail(String university_email) { this.university_email = university_email; }
    public Boolean getUniversityConfirmed() { return university_confirmed; }
    public void setUniversityConfirmed(Boolean university_confirmed) { this.university_confirmed = university_confirmed; }
    public String getUniversityName () { return this.university_name; }
    public void setUniversityName(String university_name) { this.university_name = university_name; }
    public String getUniversityImageUrl() { return this.university_image_url; }
    public void setUniversityImageUrl(String university_image_url) { this.university_image_url = university_image_url; }
    public String getAvatarUrl() { return avatar_url; }
    public void setAvatarUrl(String url) {
        this.avatar_url = url;
        this.bsAvatarUrl.onNext(url);
    }

    public UniversityData getUniversityData() { return university_data; }
    public void setUniversityData(UniversityData university_data) { this.university_data = university_data; }

    public boolean mandatoryEvaluationsRequired(){ return this.mandatory_evaluation_count > 0; }
    public boolean emailConfirmationRequired() {
        return this.confirmed == null || !this.confirmed;
    }

    public void update(UserData user) {
        this.update(user, null);
    }
    public void update(UserData user, String access_token) {
        if(access_token != null)                    this.setAccessToken(access_token);
        if(user.id != null)                         this.setId(user.id);
        if(user.realname != null)                   this.setRealname(user.realname);
        if(user.nickname != null)                   this.setNickName(user.nickname);
        if(user.email != null)                      this.setEmail(user.email);
        if(user.entrance_year != null)              this.setEntranceYear(user.entrance_year);
        if(user.university_id != null)              this.setUniversityId(user.university_id);
        if(user.is_boy != null)                     this.setGenderIsBoy(user.is_boy);
        if(user.confirmed != null)                  this.setConfirmed(user.confirmed);
        if(user.mandatory_evaluation_count != null) this.setMandatoryEvaluationCount(user.mandatory_evaluation_count);
        if(user.university_email != null)           this.setUniversityEmail(user.university_email);
        if(user.university_confirmed != null)       this.setUniversityConfirmed(user.university_confirmed);
        if(user.university_name != null)            this.setUniversityName(user.university_name);
        if(user.university_image_url != null)       this.setUniversityImageUrl(user.university_image_url);
        if(user.avatar_url != null)                 this.setAvatarUrl(user.avatar_url);
    }

    public Observable<String> getNicknameObservable() {
        return this.bsNickname;
    }
    public Observable<String> getEmailObservable() {
        return this.bsEmail;
    }
    public Observable<String> getAvatarUrlObservable() {
        return this.bsAvatarUrl;
    }

    @Override
    public String toString() {
        return String.format("[%s][%s][%s][%s][%d][%s][%d][%s]",
            this.access_token, this.realname, this.nickname, this.email, this.university_id, this.university_name, this.entrance_year, (this.is_boy != null ? (this.is_boy? "male" : "female") : "null"));
    }
}
