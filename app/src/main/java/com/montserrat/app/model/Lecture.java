package com.montserrat.app.model;

import org.json.JSONObject;

/**
 * Created by pjhjohn on 2015-05-03.
 */
public class Lecture {
    public String name;
    public int id;

    public Lecture(JSONObject json) {
        this.name = json.optString("name", null);
        this.id = json.optInt("id", -1);
    }
}
