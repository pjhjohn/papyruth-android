package com.montserrat.controller;

/**
 * Created by pjhjohn on 2015-04-10.
 * Contains Various Parameters for the application : Should be singleton.
 */
public class ApplicationManager {
    private ApplicationManager() {}
    private static ApplicationManager instance;
    public static ApplicationManager getInstance() {
        if ( instance == null ) {
            return instance = new ApplicationManager();
        } else return instance;
    }
}
