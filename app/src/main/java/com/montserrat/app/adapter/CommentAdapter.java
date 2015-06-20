package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.CommentData;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-05-22.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    public static final class Type {
        public static final int ITEM = 0;
    }
    public static CommentAdapter newInstance(List<CommentData> initItemList, RecyclerViewClickListener listener) {
        return new CommentAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<CommentData> items;

    public CommentAdapter (List<CommentData> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        CommentAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return new CommentHolder(inflater.inflate(R.layout.cardview_evaluation_comment, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CommentHolder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }


    public static class CommentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.nickname) protected TextView username;
        @InjectView(R.id.body) protected TextView body;
        public CommentHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }



        public void bind(CommentData item) {
            this.username.setText(item.user_name); // lecture represents the name of course
            this.body.setText(item.body);
        }



        @Override
        public void onClick (View view) {
            CommentAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

    }
}
