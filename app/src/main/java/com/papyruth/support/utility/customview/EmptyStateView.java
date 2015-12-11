package com.papyruth.support.utility.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by SSS on 2015-12-09.
 */
public class EmptyStateView extends RelativeLayout {
    private int     mIconDrawableResId;
    private int     mIconColorResId;
    private String  mTitleText;
    private int     mTitleTextColorResId;
    private String  mBodyText;
    private int     mBodyTextColorResId;
    private int     mBackgroundColorResId;

    @Bind(R.id.empty_state_container)   protected RelativeLayout mEmptyStateContainer;
    @Bind(R.id.empty_state_icon)        protected ImageView mIcon;
    @Bind(R.id.empty_state_title)       protected TextView mTitle;
    @Bind(R.id.empty_state_body)        protected TextView mBody;

    public EmptyStateView(Context context) {
        super(context);
        init();
    }
    public EmptyStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public EmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = inflate(getContext(), R.layout.empty_state_view, this);
        ButterKnife.bind(this, view);
        mTitleText = "";
        mBodyText = "";
        mIconColorResId = R.color.white;
        mBodyTextColorResId = R.color.white;
        mTitleTextColorResId = R.color.white;
        mBackgroundColorResId = R.color.white_40p;
        mEmptyStateContainer.setBackgroundColor(getResources().getColor(mBackgroundColorResId));
    }

    public EmptyStateView setTitle(String text) {
        mTitleText = text;
        return this;
    }
    public EmptyStateView setTitle(int resid){
        mTitleText = getResources().getString(resid);
        return this;
    }
    public EmptyStateView setTitleColorResId(int resid) {
        mTitleTextColorResId = resid;
        return this;
    }

    public EmptyStateView setBody(String text) {
        mBodyText = text;
        return this;
    }
    public EmptyStateView setBody(int resid) {
        this.mBodyText = getResources().getString(resid);
        return this;
    }
    public EmptyStateView setBodyColorResId(int resid) {
        mBodyTextColorResId = resid;
        return this;
    }

    public EmptyStateView setIconDrawable(int resid) {
        mIconDrawableResId = resid;
        return this;
    }
    public EmptyStateView setIconColorResId(int resid) {
        mIconColorResId = resid;
        return this;
    }
    public EmptyStateView setBackgroundResId(int resid) {
        mBackgroundColorResId = resid;
        return this;
    }

    public void show() {
        Picasso.with(getContext()).load(mIconDrawableResId).transform(new ColorFilterTransformation(getResources().getColor(mIconColorResId))).into(mIcon);
        mTitle.setText(mTitleText);
        mTitle.setTextColor(getResources().getColor(mTitleTextColorResId));
        mBody.setText(mBodyText);
        mBody.setTextColor(getResources().getColor(mBodyTextColorResId));
        this.setBackgroundResource(mBackgroundColorResId);
        AnimatorHelper.FADE_IN(this).start();
    }
    public void hide() {
        AnimatorHelper.FADE_OUT(this).start();
    }
}
