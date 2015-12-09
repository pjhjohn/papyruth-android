package com.papyruth.utils.view.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.papyruth.android.R;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by SSS on 2015-12-09.
 */
public class EmptyStateView extends RelativeLayout{
    private String mTitleString;
    private String mContentString;
    private int mIconDrawableRes;
    private int mIconColorRes;
    private int mBackgroundColorRes;
    private int mTitleColorRes;
    private int mContentColorRes;

    @Bind(R.id.empty_state_container) protected RelativeLayout mEmptyStateContainer;
    @Bind(R.id.empty_state_content) protected TextView mContentTextView;
    @Bind(R.id.empty_state_title) protected TextView mTitleTextView;
    @Bind(R.id.empty_state_icon) protected ImageView mIcon;

    public EmptyStateView(Context context) {
        super(context);
        this.init();
    }

    public EmptyStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public EmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init(){
        View view = inflate(getContext(), R.layout.empty_state_view, this);
        ButterKnife.bind(this, view);

        mIconColorRes = R.color.white;
        mContentColorRes = R.color.white;
        mTitleColorRes = R.color.white;
        mBackgroundColorRes = R.color.transparent;
        mEmptyStateContainer.setBackgroundColor(getResources().getColor(mBackgroundColorRes));
    }

    public EmptyStateView setTitleText(String title){
        this.mTitleString = title;
        return this;
    }
    public EmptyStateView setTitleText(int stringRes){
        this.mTitleString = getResources().getString(stringRes);
        return this;
    }

    public EmptyStateView setContentText(String content){
        this.mContentString = content;
        return this;
    }

    public EmptyStateView setContentText(int stringRes){
        this.mContentString = getResources().getString(stringRes);
        return this;
    }

    public EmptyStateView setTitleColor(int colorRes){
        this.mTitleColorRes = colorRes;
        return this;
    }

    public EmptyStateView setContentColor(int colorRes){
        this.mContentColorRes = colorRes;
        return this;
    }
    public EmptyStateView setIconDrawable(int drawableRes){
        this.mIconDrawableRes = drawableRes;
        return this;
    }
    public EmptyStateView setIconColor(int colorRes){
        this.mIconColorRes = colorRes;
        return this;
    }
    public EmptyStateView setBackground(int colorRes){
        this.mBackgroundColorRes = colorRes;
        return this;
    }
    public void show(){
        Picasso.with(getContext()).load(mIconDrawableRes).transform(new ColorFilterTransformation(getResources().getColor(mIconColorRes))).into(mIcon);
        this.setBackground(mBackgroundColorRes);
        this.mTitleTextView.setText(mTitleString);
        this.mTitleTextView.setTextColor(getResources().getColor(mTitleColorRes));
        this.mContentTextView.setText(mContentString);
        this.mContentTextView.setTextColor(getResources().getColor(mContentColorRes));
        this.setVisibility(View.VISIBLE);
    }
    public void hide(){
        this.setVisibility(View.GONE);
    }
}
