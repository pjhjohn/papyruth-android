package com.montserrat.parts.auth;

import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.montserrat.activity.R;
import com.montserrat.utils.etc.SquareImageView;
import com.montserrat.utils.recycler.RecyclerViewClickListener;

import java.util.List;

/**
 * Created by pjhjohn on 2015-04-13.
 */
public class UniversityRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int ITEM = 1;
    }
    public static UniversityRecyclerAdapter newInstance(List<Holder.Data> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
        return new UniversityRecyclerAdapter(initItemList, listener, fragment);
    }

    private static RecyclerViewClickListener itemListener;
    private static Fragment fragment;
    private List<Holder.Data> items;
    private UniversityRecyclerAdapter (List<Holder.Data> initItemList, RecyclerViewClickListener listener, Fragment fragment) {
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

    /* Item of list-like recyclerview : WILL BE DISPLAYED AS GRIDVIEW-ELEMENT */
    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView viewSchoolName;
        private SquareImageView viewSchoolImage;
        private Holder(final View parent, TextView viewSchoolName, SquareImageView viewSchoolImage) {
            super(parent);
            this.viewSchoolName = viewSchoolName;
            this.viewSchoolImage = viewSchoolImage;
            parent.setOnClickListener(this);
        }

        public static RecyclerView.ViewHolder newInstance(View parent) {
            return new Holder(parent,
                (TextView) parent.findViewById(R.id.university_item_school),
                (SquareImageView) parent.findViewById(R.id.university_item_image)
            );
        }

        public void bind(UniversityRecyclerAdapter.Holder.Data item) {
            this.viewSchoolName.setText(item.schoolName);
            Glide.with(fragment).load(item.schoolImageUrl).into(this.viewSchoolImage);
        }

        @Override
        public void onClick (View view) {
            UniversityRecyclerAdapter.itemListener.recyclerViewListClicked(view, this.getPosition());
        }

        public static class Data {
            public String schoolName;
            public String schoolDomain;
            public String schoolImageUrl;
            public int schoolId;
            public Data(String schoolName, String schoolDomain, String schoolImageUrl, int schoolId) {
                this.schoolName = schoolName;
                this.schoolDomain = schoolDomain;
                this.schoolImageUrl = schoolImageUrl;
                this.schoolId = schoolId;
            }
        }
    }
}
