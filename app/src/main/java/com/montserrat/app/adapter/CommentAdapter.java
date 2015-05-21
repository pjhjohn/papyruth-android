package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.Comment;
import com.montserrat.app.model.PartialCourse;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-05-22.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static CommentAdapter newInstance(List<Comment> initItemList, RecyclerViewClickListener listener) {
        return new CommentAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Comment> items;

    public CommentAdapter (List<Comment> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        CommentAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return new Holder(inflater.inflate(R.layout.cardview_evaluation_reply, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Holder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nickname) protected TextView username;
        @InjectView(R.id.content) protected TextView comment;
        public Holder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(Comment item) {
            this.username.setText(item.user_name); // lecture represents the name of course
            this.comment.setText(item.comment);
        }

        @Override
        public void onClick (View view) {
            CommentAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

    }
}
