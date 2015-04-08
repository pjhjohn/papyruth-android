package com.montserrat.parts.main;

/**
 * Created by mrl on 2015-03-16.
 */
public class MainItem {
    String subject;
    String professor;
    float rate;

    public MainItem(String subject, String professor, float rate) {
        super();
        this.subject = subject;
        this.professor = professor;
        this.rate = rate;
    }

    public String getSubject() { return this.subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getProfessor() { return this.professor; }
    public void setProfessor(String professor) { this.professor = professor; }
    public float getRate() { return this.rate; }
    public void setRate(float rate) { this.rate = rate; }
}