package com.papyruth.android.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.papyruth.android.R;
import com.papyruth.android.model.TermData;
import com.papyruth.android.recyclerview.viewholder.FooterViewHolder;
import com.papyruth.android.recyclerview.viewholder.HeaderViewHolder;
import com.papyruth.android.recyclerview.viewholder.TermViewHolder;
import com.papyruth.android.recyclerview.viewholder.ViewHolderFactory;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TermsOfServiceAdapter extends TrackerAdapter {
    private List<TermData> mTerms;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private int mIndexHeader; // INDEX 0
    private int mIndexContent;// UNDER SHADOW if exists.
    private int mIndexFooter; // AT LAST
    private View mFooterBorder;
    private RelativeLayout mFooterMaterialProgressBar;

    public TermsOfServiceAdapter(Context context, RecyclerViewItemObjectClickListener listener) {
        mTerms = new ArrayList<>();
        mRecyclerViewItemObjectClickListener = listener;
        mIndexHeader = 0;
        mIndexContent= 1;
        mIndexFooter = mTerms.size() + mIndexContent;
        refresh();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = ViewHolderFactory.getInstance().create(parent, viewType, (view, position)-> {
            if(viewType == ViewHolderFactory.ViewType.TERM)
                if(position - mIndexContent >= 0 && position - mIndexContent < mTerms.size()) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, mTerms.get(position - mIndexContent));
        });
        if (viewHolder instanceof FooterViewHolder) {
            mFooterBorder = viewHolder.itemView.findViewById(R.id.footer_border);
            mFooterMaterialProgressBar = (RelativeLayout) viewHolder.itemView.findViewById(R.id.material_progress_medium);
        } return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= mIndexHeader) {((HeaderViewHolder) holder).bind(R.color.toolbar_blue); return;}
        if (position == mIndexFooter) return;
        ((TermViewHolder) holder).bind(mTerms.get(position - mIndexContent));
    }

    @Override
    public int getItemCount() {
        return mIndexFooter + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= mIndexHeader) return ViewHolderFactory.ViewType.HEADER;
        if (position == mIndexFooter) return ViewHolderFactory.ViewType.FOOTER;
        return ViewHolderFactory.ViewType.TERM;
    }

    public void refresh() {
        Api.papyruth().get_terms()
            .map(response -> response.terms)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(terms -> {
                mTerms.addAll(terms);
                mIndexFooter = mTerms.size() + mIndexContent;
                notifyItemRangeInserted(mIndexContent, mTerms.size());
                AnimatorHelper.FADE_IN(mFooterBorder).start();
            }, error -> ErrorHandler.handle(error, this.getFragment(), true));
    }
}