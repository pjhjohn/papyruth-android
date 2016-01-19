package com.papyruth.support.utility.customview;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import com.papyruth.support.opensource.materialdialog.HashtagDeleteDialog;

import java.util.List;

import rx.functions.Func0;

/**
 * Created by pjhjohn on 2015-06-25.
 */
public class Hashtag {
    // TODO : Remove Trailing Spaces
    public static String appendPrefix(String text) {
        if(text.charAt(0) != '#') return "#" + text;
        return text;
    }

    // TODO : Remove Trailing Spaces
    public static String removePrefix(String text) {
        if(text.charAt(0) == '#') return text.substring(1);
        return text;
    }

    public static SpannableString clickableSpannableString(Context context, List<String> hashtags, Func0<Void> action) {
        String str = "";
        for(String hashtag : hashtags) str += appendPrefix(hashtag) + " ";

        SpannableString spannableString = new SpannableString(str);
        for(String hashtag : hashtags) {
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    HashtagDeleteDialog.show(context, hashtag, action);
                }
            };
            spannableString.setSpan(span, str.indexOf(hashtag) - 1, str.indexOf(hashtag) + hashtag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } return spannableString;
    }

    public static String plainString(List<String> hashtags) {
        String str = "";
        for(String hashtag : hashtags) {
            if(hashtag != null && !hashtag.isEmpty()) str += appendPrefix(hashtag) + " ";
        }
        return str;
    }
}