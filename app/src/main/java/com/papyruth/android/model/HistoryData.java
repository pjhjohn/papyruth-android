package com.papyruth.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-07-05.
 */
public class HistoryData {
    public List<CandidateData> candidates;

    public HistoryData() {
        this.candidates = new ArrayList<>();
    }

    public HistoryData(List<CandidateData> candidates) {
        this.candidates = candidates;
    }
}
