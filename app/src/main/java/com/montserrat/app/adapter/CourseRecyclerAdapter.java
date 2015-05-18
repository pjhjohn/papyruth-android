package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.PartialEvaluation;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final class Type {
        public static final int HEADER = 1;
        public static final int ITEM = 2;
    }
    public static CourseRecyclerAdapter newInstance(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        return new CourseRecyclerAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;

    private List<PartialEvaluation> items;
    private CourseRecyclerAdapter(List<PartialEvaluation> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        CourseRecyclerAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return Header.newInstance(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.cardview_evaluation_reply, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (!isPositionOfHeader(position)) {
            Holder holder = (Holder) viewHolder;
            PartialEvaluation item = this.items.get(position - 1);
            holder.bind(item);
        }
    }

    // old item count
    public int getBasicItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemCount() {
        return getBasicItemCount() + 1; // 1 is for header.
    }

    @Override
    public int getItemViewType(int position) {
        return isPositionOfHeader(position)? Type.HEADER : Type.ITEM;
    }

    private boolean isPositionOfHeader (int position) {
        return position == 0;
    }


    /* Header of list-list recyclerview */
    public static class Header extends RecyclerView.ViewHolder {
        private Header(View itemView) {
            super(itemView);
        }
        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Header(parent);
        }
    }

    /* Item of list-like recyclerview */

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView userId;
        private final TextView comment;
        private final Button pointOverall;
        private Holder(final View parent, TextView viewLecture, TextView viewcontent, Button viewLike) {
            super(parent);
            this.userId = viewLecture;
            this.comment = viewcontent;
            this.pointOverall = viewLike;
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            TextView vLecture = (TextView) parent.findViewById(R.id.reply_lecTitle);
            TextView vContent = (TextView) parent.findViewById(R.id.reply_Contents);
            Button vLike = (Button) parent.findViewById(R.id.reply_like);
            return new Holder(parent, vLecture, vContent, vLike);
        }

        public void bind(PartialEvaluation item) {
            this.userId.setText(item.user_id);
            this.comment.setText(item.comment);
            this.pointOverall.setText(item.point_overall);
        }

        @Override
        public void onClick(View v) {
            CourseRecyclerAdapter.itemListener.recyclerViewListClicked(v, this.getPosition());
        }

    }
}
