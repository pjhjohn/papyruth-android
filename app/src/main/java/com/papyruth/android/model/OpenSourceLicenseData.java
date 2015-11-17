package com.papyruth.android.model;

/**
 * Created by pjhjohn on 2015-11-02.
 */
public class OpenSourceLicenseData {
    public final String name;
    public final String license;
    public final String repoUrl;
    public final Integer repoIconResId;

    public OpenSourceLicenseData(String name, String license, String repoUrl, Integer repoIconResId) {
        this.name = name;
        this.license = license;
        this.repoUrl = repoUrl;
        this.repoIconResId = repoIconResId;
    }
}
