package com.papyruth.utils.support.retrofit;

import android.util.Log;

import java.util.regex.Pattern;

import retrofit.RestAdapter;

/** A {@link RestAdapter.Log logger} for Android.
 * Customized to match pattern.
 */
public class RetrofitLogger implements RestAdapter.Log {
    private static final int LOG_CHUNK_SIZE = 4000;
    private static final String DEFAULT_FILTER = ".*";

    private final String tag;
    private final Pattern pattern;

    public RetrofitLogger(String tag) {
        this.tag = tag;
        this.pattern = Pattern.compile(DEFAULT_FILTER);;
    }
    public RetrofitLogger(String tag, String regex) {
        this.tag = tag;
        this.pattern = Pattern.compile(regex);
    }

    @Override public final void log(String message) {
        for (int i = 0, len = message.length(); i < len; i += LOG_CHUNK_SIZE) {
            int end = Math.min(len, i + LOG_CHUNK_SIZE);
            logChunk(message.substring(i, end));
        }
    }

    /**
     * Called one or more times for each call to {@link #log(String)}. The length of {@code chunk}
     * will be no more than 4000 characters to support Android's {@link Log} class.
     */
    public void logChunk(String chunk) {
        if (this.pattern.matcher(chunk).matches()) Log.d(getTag(), chunk);
    }

    public String getTag() {
        return tag;
    }
}

