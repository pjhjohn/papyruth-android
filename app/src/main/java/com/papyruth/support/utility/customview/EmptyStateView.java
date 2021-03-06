package com.papyruth.support.utility.customview;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
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

    @BindView(R.id.empty_state_container)   protected RelativeLayout mEmptyStateContainer;
    @BindView(R.id.empty_state_icon)        protected ImageView mIcon;
    @BindView(R.id.empty_state_title)       protected TextView mTitle;
    @BindView(R.id.empty_state_body)        protected TextView mBody;

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
        mIconDrawableResId = 0;
        mIconColorResId = R.color.white;
        mBodyTextColorResId = R.color.white;
        mTitleTextColorResId = R.color.white;
        mBackgroundColorResId = R.color.background_empty_state;
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
        if(mIconDrawableResId != 0) Picasso.with(getContext()).load(mIconDrawableResId).transform(new ColorFilterTransformation(getResources().getColor(mIconColorResId))).into(mIcon);
        mTitle.setText(mTitleText);
        mTitle.setTextColor(getResources().getColor(mTitleTextColorResId));
        mTitle.setPaintFlags(mTitle.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
        mBody.setText(mBodyText);
        mBody.setTextColor(getResources().getColor(mBodyTextColorResId));
        this.setBackgroundResource(mBackgroundColorResId);
        this.mEmptyStateContainer.setVisibility(View.VISIBLE);
        AnimatorHelper.FADE_IN(this).start();
    }
    public void hide() {
        this.mEmptyStateContainer.setVisibility(View.GONE);
        AnimatorHelper.FADE_OUT(this).start();
    }
}
