package com.papyruth.support.opensource.materialdialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.papyruth.android.R;
import com.papyruth.android.model.UserData;
import com.papyruth.android.model.VotersData;
import com.papyruth.support.opensource.picasso.CircleTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by pjhjohn on 2015-07-11.
 */
public class VotersDialog {
    public static void show(Context context, String title, VotersData users) {
        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(context);
        for(UserData user : users.users) {
            MaterialSimpleListItem.Builder builder = new MaterialSimpleListItem.Builder(context).content(user.nickname);
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    adapter.add(builder.icon(new BitmapDrawable(context.getResources(), bitmap)).build());
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    adapter.add(builder.icon(errorDrawable).build());
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {}
            };
            Picasso.with(context).load(user.avatar_url).placeholder(R.drawable.avatar_dummy).error(R.drawable.avatar_dummy).transform(new CircleTransformation()).into(target);
        }
        new MaterialDialog.Builder(context)
            .title(String.format("%s (%d)", title, users.counts))
            .adapter(adapter, null)
            .show();
    }
}
