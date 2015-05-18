package com.montserrat.app.adapter;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.University;
import com.montserrat.utils.view.SquareImageView;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
            case Type.ITEM : return new Holder(inflater.inflate(R.layout.cardview_grid_university, parent, false));
            default : throw new RuntimeException("There is no type that matche the type " + viewType + " + make sure you're using types correctly");
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

    /* Item of list-like recyclerview : WILL BE DISPLAYED AS GRIDVIEW-ELEMENT */
    protected class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.university_item_name) protected TextView name;
        @InjectView (R.id.university_item_image) protected SquareImageView image;
        public Holder(View parent) {
            super(parent);
            ButterKnife.inject(this, parent);
            parent.setOnClickListener(this);
        }

        public void bind(University university) {
            this.name.setText(university.name);
            Picasso.with(fragment.getActivity()).load(university.image_url).into(this.image);
        }

        @Override
        public void onClick (View view) {
            UniversityRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
