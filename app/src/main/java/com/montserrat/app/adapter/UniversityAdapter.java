package com.montserrat.app.adapter;

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

    private RecyclerViewClickListener universityItemClickListener;
    private List<UniversityData> universities;
    public UniversityAdapter(List<UniversityData> initialUniversities, RecyclerViewClickListener listener) {
        this.universities = initialUniversities;
        this.universityItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.UNIVERSITY: return new UniversityViewHolder(inflater.inflate(R.layout.cardview_grid_university, parent, false));
            default : throw new RuntimeException("There is no ViewHolder availiable for type#" + viewType + " Make sure you're using valid type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UniversityViewHolder) holder).bind(this.universities.get(position));
    }

    @Override
    public int getItemCount() {
        return this.universities == null ? 0 : this.universities.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Type.UNIVERSITY;
    }

    protected class UniversityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView (R.id.university_item_name) protected TextView name;
        @InjectView (R.id.university_item_image) protected SquareImageView image;
        public UniversityViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(UniversityData universityData) {
            this.name.setText(universityData.name);
            Picasso.with(this.itemView.getContext()).load(universityData.image_url).into(this.image);
        }

        @Override
        public void onClick (View view) {
            universityItemClickListener.onRecyclerViewItemClick(view, this.getAdapterPosition());
        }
    }
}
