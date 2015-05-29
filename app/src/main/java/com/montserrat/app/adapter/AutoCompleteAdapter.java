package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.response.Candidate;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-04-25.
 */
public class AutoCompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }

    private static RecyclerViewClickListener itemListener; // TODO : use if implemented.

    private List<Candidate> items;
    public AutoCompleteAdapter(List<Candidate> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        AutoCompleteAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM: return new AutoCompleteResponseHolder(inflater.inflate(R.layout.cardview_autocomplete, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((AutoCompleteResponseHolder) holder).bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }


    public static class AutoCompleteResponseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.content) protected TextView content;
        public AutoCompleteResponseHolder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(Candidate item) {
            if (item.course != null){
                this.content.setText(item.course.name + " - " +item.course.professor);
            }else if(item.lecture_name != null)
                this.content.setText(item.lecture_name);
            else if(item.professor_name != null)
                this.content.setText((item.professor_name));
        }

        @Override
        public void onClick(View v) {
            AutoCompleteAdapter.itemListener.recyclerViewListClicked(v, this.getPosition());
        }

    }
}
