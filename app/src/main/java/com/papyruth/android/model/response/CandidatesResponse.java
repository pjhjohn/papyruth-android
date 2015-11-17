package com.papyruth.android.model.response;

import com.papyruth.android.model.Candidate;

import java.util.List;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class CandidatesResponse {
    public Boolean success;
    public List<Candidate> candidates;

    @Override
    public String toString() {
        return String.format("%s with number of result = #%d", success ? "Succeed" : "Failed", candidates.size());
    }
}
