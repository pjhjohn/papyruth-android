package com.montserrat.app.model.unique;

import rx.subjects.BehaviorSubject;

/**
 * Created by SSS on 2015-07-19.
 */
public class Signup {
    private static Signup instance = null;
    public static synchronized Signup getInstance () {
        if ( Signup.instance == null ) Signup.instance = new Signup();
        return Signup.instance;
    }

    private String  realname;
    private String  nickname;
    private String  email;
    private Boolean is_boy;
    private Integer university_id;
    private String image_url;
    private String  password;
    private Integer entrance_year;
    public Signup(){
        this.clear();
    }
    public void clear(){
        this.realname = null;
        this.nickname = null;
        this.email = null;
        this.is_boy = null;
        this.university_id = null;
        this.password = null;
        this.entrance_year = null;
    }
    public String getRealname() {return realname;}
    public String getNickname() {return nickname;}
    public String getEmail() {return email;}
    public Boolean getIs_boy() {return is_boy;}
    public Integer getUniversity_id() {return university_id;}
    public String getPassword() {return password;}
    public Integer getEntrance_year() {return entrance_year;}
    public String getImage_url() {return image_url;}

    public void setRealname(String realname) {this.realname = realname;}
    public void setNickname(String nickname) {this.nickname = nickname;}
    public void setEmail(String email) {this.email = email;}
    public void setIs_boy(Boolean is_boy) {this.is_boy = is_boy;}
    public void setUniversity_id(Integer university_id) {this.university_id = university_id;}
    public void setPassword(String password) {this.password = password;}
    public void setEntrance_year(Integer entrance_year) {this.entrance_year = entrance_year;}
    public void setImage_url(String image_url) {this.image_url = image_url;}

    @Override
    public String toString() {
        if(is_boy != null)
            return String.format("[%s][%s][%s][%d][%s][%s]", this.realname, this.nickname, this.email, this.university_id, this.is_boy? "male" : "female", entrance_year);
        else
            return String.format("[%s][%s][%s][%d][%s][%s]", this.realname, this.nickname, this.email, this.university_id, null, entrance_year);
    }
}
