package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by SSS on 2015-05-08.
 */
public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @InjectView(R.id.eval_prof) protected TextView vProfText;
    @InjectView(R.id.eval_title) protected TextView vTitleText;
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static EvaluationAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new EvaluationAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    public EvaluationAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        EvaluationAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.evaluation_list_item, parent, false));
            default : throw new RuntimeException("There is no type that matches the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Holder holder = (Holder) viewHolder;
        Holder.Data item = this.items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return this.items == null ? 0 : this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.ITEM;
    }

    /* Item of list-like recyclerview */

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.eval_title) protected TextView vTitleText;
        @InjectView(R.id.eval_prof) protected TextView vProfText;
        private Holder(final View parent, TextView viewTitle, TextView viewProf) {
            super(parent);
            this.vTitleText = viewTitle;
            this.vProfText = viewProf;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Holder(
                    parent,
                    (TextView)parent.findViewById(R.id.eval_title),
                    (TextView)parent.findViewById(R.id.eval_prof)
            );
        }

        public void bind(Data item) {
            this.vTitleText.setText(item.titleText);
            this.vProfText.setText(item.profText);
        }

        @Override
        public void onClick (View view) {
            EvaluationAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }


        public static class Data {
            public String titleText;
            public String profText;
            public int title_id;
            public int prof_id;
            public Data(String titleText, String profText, int title_id, int prof_id) {
                this.titleText = titleText;
                this.profText = profText;
                this.title_id = title_id;
                this.prof_id = prof_id;
            }
        }
    }
}