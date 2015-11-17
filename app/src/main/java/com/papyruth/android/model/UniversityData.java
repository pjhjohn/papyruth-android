package com.papyruth.android.model;

/**
 * Created by pjhjohn on 2015-05-10.
 */
public class UniversityData {
    public int id;
    public String name;
    public String image_url;
    public String email_domain;
    public Integer user_count;
    public Integer evaluation_count;

    @Override
    public String toString() {
        return String.format("id:%d, name:%s, image_url:%s, email_domain:%s, user_count:%d, evaluation_count:%d", id, name, image_url, email_domain, user_count, evaluation_count);
    }
}