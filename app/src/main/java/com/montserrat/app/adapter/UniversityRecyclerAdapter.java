package com.montserrat.app.adapter;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.montserrat.app.R;
import com.montserrat.app.model.University;
import com.montserrat.utils.etc.SquareImageView;
import com.montserrat.utils.recycler.RecyclerViewClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class UniversityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static UniversityRecyclerAdapter newInstance(List<University> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
        return new UniversityRecyclerAdapter(initItemList, listener, fragment);
    }

    private static RecyclerViewClickListener itemListener;
    private static Fragment fragment;
    private List<University> items;
    private UniversityRecyclerAdapter (List<University> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
        this.items = initItemList;
        UniversityRecyclerAdapter.itemListener = listener;
        UniversityRecyclerAdapter.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.ITEM   : return Holder.newInstance(inflater.inflate(R.layout.recycler_item_university, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Holder holder = (Holder) viewHolder;
        University item = this.items.get(position);
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

    /* Item of list-like recyclerview : WILL BE DISPLAYED AS GRIDVIEW-ELEMENT */
    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView vUniversityName;
        private SquareImageView vUniversitySquareImage;
        private Holder(final View parent, TextView vUniversityName, SquareImageView vUniversitySquareImage) {
            super(parent);
            this.vUniversityName = vUniversityName;
            this.vUniversitySquareImage = vUniversitySquareImage;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Holder(parent,
                (TextView) parent.findViewById(R.id.university_item_name),
                (SquareImageView) parent.findViewById(R.id.university_item_image)
            );
        }

        public void bind(University university) {
            this.vUniversityName.setText(university.name);
            Picasso.with(fragment.getActivity()).load(university.image_url).into(this.vUniversitySquareImage);
        }

        @Override
        public void onClick (View view) {
            UniversityRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }


    }
}
