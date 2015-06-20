package com.montserrat.app.adapter;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.UniversityData;
import com.montserrat.utils.view.SquareImageView;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author : JoonHo Park &lt;pjhjohn@gmail.com&gt;<br>
 * Used in {@link com.montserrat.app.fragment.auth.SignUpStep1Fragment SignUpStep1Fragment}
 * as an adapter for Grid-type {@link RecyclerView} to provide university selection to user
 */
public class UniversityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int UNIVERSITY = 1;
    }
    public static UniversityAdapter newInstance(List<UniversityData> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
        return new UniversityAdapter(initItemList, listener, fragment);
    }

    private static RecyclerViewClickListener itemListener;
    private static Fragment fragment;
    private List<UniversityData> items;
    private UniversityAdapter(List<UniversityData> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
        this.items = initItemList;
        UniversityAdapter.itemListener = listener;
        UniversityAdapter.fragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.UNIVERSITY: return new Holder(inflater.inflate(R.layout.cardview_grid_university, parent, false));
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
        return Type.UNIVERSITY;
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

        public void bind(UniversityData universityData) {
            this.name.setText(universityData.name);
            Picasso.with(fragment.getActivity()).load(universityData.image_url).into(this.image);
        }

        @Override
        public void onClick (View view) {
            UniversityAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }
    }
}
