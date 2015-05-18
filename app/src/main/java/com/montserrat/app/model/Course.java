package com.montserrat.app.model;

/**
 * Created by SSS on 2015-05-18.
 */
public class Course {
    public int id; // lecture id
    public int unit; // lecture unit
    public String code; // lecture code
    public int university_id;
    public String name;
    public String professor;


    private static Course instance = null;
    public static synchronized Course getInstance() {
        if( Course.instance == null ) Course.instance = new Course();
        return Course.instance;
    }
}
