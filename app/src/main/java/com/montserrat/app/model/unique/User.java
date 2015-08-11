package com.montserrat.app.model.unique;

import com.montserrat.app.model.FavoriteData;
import com.montserrat.app.model.UserData;

import java.util.List;

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
    private String  realname;
    private String  nickname;
    private String  email;
    private Boolean is_boy;
    private Integer university_id;
    private String  university_name;
    private Integer entrance_year;
    private String  avatar_url;
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
        this.access_token    = null;
        this.realname        = null;
        this.nickname        = null;
        this.email           = null;
        this.is_boy          = null;
        this.university_id   = null;
        this.university_name = null;
        this.entrance_year   = null;
        this.avatar_url      = null;
    }

    public String getAccessToken () {
        return this.access_token;
    }
    public void setAccessToken (String token) {
        if (token != null && !token.isEmpty()) this.access_token = String.format("Token token=\"%s\"", token);
    }
    public String getRealname () {
        return this.realname;
    }
    public void setRealname (String realname) {
        this.realname = realname;
    }
    public String getNickname() {
        return this.nickname;
    }
    public void setNickName(String nickname) {
        this.nickname = nickname;
        this.bsNickname.onNext(nickname);
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
        this.bsEmail.onNext(email);
    }
    public Integer getUniversityId() {
        return this.university_id;
    }
    public void setUniversityId (Integer university_id) {
        this.university_id = university_id;
    }
    public String getUniversityName () {
        return this.university_name;
    }
    public void setUniversityName(String university_name) {
        this.university_name = university_name;
    }
    public Integer getEntranceYear() {
        return this.entrance_year;
    }
    public void setEntranceYear (Integer admission_year) {
        this.entrance_year = admission_year;
    }
    public boolean getGenderIsBoy() {
        return this.is_boy;
    }
    public void setGenderIsBoy (boolean is_boy) {
        this.is_boy = is_boy;
    }
    public String getAvatarUrl() {
        return avatar_url;
    }
    public void setAvatarUrl(String url) {
        this.avatar_url = url;
        this.bsAvatarUrl.onNext(url);
    }

    public void update(UserData user) {
        this.update(user, null);
    }
    public void update(UserData user, String access_token) {
        if(user.email != null) this.setEmail(user.email);
        if(user.realname != null) this.setRealname(user.realname);
        if(user.nickname != null) this.setNickName(user.nickname);
        if(user.is_boy != null) this.setGenderIsBoy(user.is_boy);
        if(user.university_id != null) this.setUniversityId(user.university_id);
        if(user.entrance_year != null) this.setEntranceYear(user.entrance_year);
        if(user.university_name != null) this.setUniversityName(user.university_name);
        if(access_token!=null) this.setAccessToken(access_token);
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
        return String.format("[%s][%s][%s][%s][%d][%s][%d][%s]", this.access_token, this.realname, this.nickname, this.email, this.university_id, this.university_name, this.entrance_year, this.is_boy? "male" : "female");
    }
}
