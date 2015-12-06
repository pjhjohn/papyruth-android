package com.papyruth.android.model.unique;

/**
 * Created by SSS on 2015-07-19.
 */
public class SignUpForm {
    private static SignUpForm instance = null;
    public static synchronized SignUpForm getInstance () {
        if ( SignUpForm.instance == null ) SignUpForm.instance = new SignUpForm();
        return SignUpForm.instance;
    }

    private Integer university_id;
    private String image_url;
    private Integer entrance_year;

    private SignUpFormData tempSavedFormData;
    private SignUpFormData validSignUpFormData;

    private SignUpForm(){
        this.tempSavedFormData = new SignUpFormData();
        this.validSignUpFormData = new SignUpFormData();
        this.clear();
    }
    public void clear(){
        this.university_id = null;
        this.entrance_year = null;
        this.image_url  = null;
        this.tempSavedFormData.clear();
        this.validSignUpFormData.clear();
    }
    public Integer getUniversityId() {return university_id;}
    public Integer getEntranceYear() {return entrance_year;}
    public String getImageUrl() {return image_url;}

    public void setUniversityId(Integer university_id) {this.university_id = university_id;}
    public void setEntranceYear(Integer entrance_year) {this.entrance_year = entrance_year;}
    public void setImageUrl(String image_url) {this.image_url = image_url;}


    public String getValidEmail()                       { return validSignUpFormData.email; }
    public String getValidRealname()                    { return validSignUpFormData.realname; }
    public String getValidNickname()                    { return validSignUpFormData.nickname; }
    public Boolean getValidIsBoy()                      { return validSignUpFormData.is_boy; }
    public String getValidPassword()                    { return validSignUpFormData.password; }

    public void setValidEmail()                         { this.validSignUpFormData.email = getTempSaveEmail(); }
    public void setValidRealname()                      { this.validSignUpFormData.realname = getTempSaveRealname(); }
    public void setValidNickname()                      { this.validSignUpFormData.nickname = getTempSaveNickname(); }
    public void setValidIsBoy()                         { this.validSignUpFormData.is_boy = getTempSaveIsBoy(); }
    public void setValidPassword()                      { this.validSignUpFormData.password = getTempSavePassword(); }


    public String getTempSaveEmail()                    { return tempSavedFormData.email; }
    public String getTempSaveRealname()                 { return tempSavedFormData.realname; }
    public String getTempSaveNickname()                 { return tempSavedFormData.nickname; }
    public Boolean getTempSaveIsBoy()                   { return tempSavedFormData.is_boy; }
    public String getTempSavePassword()                 { return tempSavedFormData.password; }

    public void setTempSaveEmail(String email){
        this.tempSavedFormData.email = email;
        this.validSignUpFormData.email = null;
    }
    public void setTempSaveRealname(String realname){
        this.tempSavedFormData.realname = realname;
        this.validSignUpFormData.realname = null;
    }
    public void setTempSaveNickname(String nickname){
        this.tempSavedFormData.nickname = nickname;
        this.validSignUpFormData.nickname = null;
    }
    public void setTempSaveIsBoy(Boolean is_boy){
        this.tempSavedFormData.is_boy = is_boy;
        this.validSignUpFormData.is_boy = null;
    }
    public void setTempSavePassword(String password){
        this.tempSavedFormData.password = password;
        this.validSignUpFormData.password = null;
    }

    @Override
    public String toString() {
        return String.format("entrance year : %d, university id : %d\ntemp saved data : %s\nvalid data : %s", entrance_year, university_id, tempSavedFormData.toString(), validSignUpFormData.toString());
    }

    private class SignUpFormData{
        String  email;
        String  nickname;
        String  realname;
        Boolean is_boy;
        String  password;

        public void clear(){
            this.email      = null;
            this.nickname   = null;
            this.realname   = null;
            this.is_boy     = null;
            this.password   = null;
        }

        @Override
        public String toString() {
            if(is_boy != null)
                return String.format("[%s][%s][%s][%s]", this.realname, this.nickname, this.email, this.is_boy? "male" : "female");
            else
                return String.format("[%s][%s][%s][%s]", this.realname, this.nickname, this.email, null);
        }
    }
}
