package com.papyruth.android.model.response;

import com.papyruth.android.model.CandidateData;

import java.util.List;

/**
 * Created by pjhjohn on 2015-05-28.
 */
public class CandidatesResponse {
    public List<CandidateData> candidates;

    @Override
    public String toString() {
        return String.format("CandidatesResponse.candidates : %s", candidates);
    }
}
