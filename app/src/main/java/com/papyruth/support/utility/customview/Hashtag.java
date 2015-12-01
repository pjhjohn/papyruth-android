package com.papyruth.support.utility.customview;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.support.opensource.materialdialog.HashtagDeleteDialog;

import java.util.List;

import rx.functions.Func0;

/**
 * Created by pjhjohn on 2015-06-25.
 */
public class Hashtag{

    public static String addHashPrefix(String text){

        if(text.charAt(0) != '#')
            return "#" + text + " ";

        return text+" ";
    }

    public static String removeHashPrefix(String text){
        if(text.charAt(0) == '#')
            return text.substring(1);

        return text;
    }

    public static SpannableString getClickableHashtag(Context context, List<String> hashtags, Func0<Boolean> action){
        String hashtagString = "";

        for(String hashtagItem : EvaluationForm.getInstance().getHashtag()) {
            hashtagString += addHashPrefix(hashtagItem);
        }

        SpannableString spannableString = new SpannableString(hashtagString);

        for(String item : EvaluationForm.getInstance().getHashtag()){
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    HashtagDeleteDialog.show(context, item, action);
                }
            };
            spannableString.setSpan(span, hashtagString.indexOf(item)-1, hashtagString.indexOf(item)+item.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    public static String getHashtag(List<String> hashtags){
        String hashtagString = "";

        for(String hashtagItem : hashtags){
            hashtagString += addHashPrefix(hashtagItem);
        }

        return hashtagString;
    }
}