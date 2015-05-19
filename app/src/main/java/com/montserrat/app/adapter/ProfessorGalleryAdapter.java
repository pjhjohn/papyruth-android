package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

/**
 * /**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.gallery.ProfessorGalleryFragment ProfessorGalleryFragment}
 * as an adapter for Grid-type {@link RecyclerView} to provide pictures of professor
 */
public class ProfessorGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int PROFESSOR_PICTURE = 1;
    }
    public static ProfessorGalleryAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        return new ProfessorGalleryAdapter(initItemList, listener);
    }

    private static RecyclerViewClickListener itemListener;
    private List<Holder.Data> items;
    private ProfessorGalleryAdapter(List<Holder.Data> initItemList, RecyclerViewClickListener listener) {
        this.items = initItemList;
        ProfessorGalleryAdapter.itemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.PROFESSOR_PICTURE: return Holder.newInstance(inflater.inflate(R.layout.cardview_grid_university, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
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
        return Type.PROFESSOR_PICTURE;
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView vUniv;
        private Holder(final View parent, TextView vUniv) {
            super(parent);
            this.vUniv = vUniv;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            TextView vUniv = (TextView) parent.findViewById(R.id.university_item_name);
            return new Holder(parent, vUniv);
        }

        public void bind(ProfessorGalleryAdapter.Holder.Data item) {
            this.vUniv.setText(item.univ);
        }

        @Override
        public void onClick (View view) {
            ProfessorGalleryAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

        public static class Data {
            public String univ;
            public int univCode;
            public Data(String univ, int univCode) {
                this.univ = univ;
                this.univCode = univCode;
            }
        }
    }
}
