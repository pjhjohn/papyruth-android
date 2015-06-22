package com.montserrat.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.model.EvaluationData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.utils.view.recycler.RecyclerViewClickListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by SSS on 2015-04-25.
 */
public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final class Type {
        public static final int HEADER = 0;
        public static final int COURSE = 1;
        public static final int SIMPLE_EVALUATION = 2;
    }

    private RecyclerViewClickListener evaluationItemClickListener; // TODO : use if implemented.
    private List<EvaluationData> evaluations;
    public CourseAdapter(List<EvaluationData> initialEvaluations, RecyclerViewClickListener listener) {
        this.evaluations = initialEvaluations;
        this.evaluationItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType) {
            case Type.HEADER : return new HeaderViewHolder(inflater.inflate(R.layout.cardview_header, parent, false));
            case Type.COURSE : return new CourseViewHolder(inflater.inflate(R.layout.cardview_course, parent, false));
            case Type.SIMPLE_EVALUATION : return new SimpleEvaluationViewHolder(inflater.inflate(R.layout.cardview_evaluation_simple, parent, false));
            default : throw new RuntimeException("There is no ViewHolder availiable for type#" + viewType + " Make sure you're using valid type");
        }
    }

    /**
     * @param holder
     * @param position 0 for header, 1 for course, 2+ for evaluations
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position <= 0) return;
        else if (position == 1) ((CourseViewHolder) holder).bind(Course.getInstance());
        else ((SimpleEvaluationViewHolder) holder).bind(this.evaluations.get(position - 2));
    }

    @Override
    public int getItemCount() {
        return 2 + (this.evaluations == null ? 0 : this.evaluations.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0) return Type.HEADER;
        else if (position == 1) return Type.COURSE;
        else return Type.SIMPLE_EVALUATION;
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected class CourseViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.course_title) protected TextView title;
        @InjectView(R.id.course_professor) protected TextView professor;
        @InjectView(R.id.course_category) protected TextView category;
        @InjectView(R.id.course_thumbnail) protected ImageView thumbnail;
        @InjectView(R.id.course_point_overall) protected SeekBar overall;
        @InjectView(R.id.course_point_gpa_satisfaction) protected SeekBar satisfaction;
        @InjectView(R.id.course_point_clarity) protected SeekBar clarity;
        @InjectView(R.id.course_point_easiness) protected SeekBar easiness;
        @InjectView(R.id.course_tags) protected LinearLayout tags;
        public CourseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

        public void bind(Course course) {
            this.title.setText(course.getName());
            this.professor.setText(course.getProfessor());
            this.category.setText(""); // TODO : define it!
//            Picasso.with(this.itemView.getContext()).load("").transform(new CircleTransformation()).into(this.thumbnail);
            this.overall.setProgress(course.getPointOverall());
            this.satisfaction.setProgress(course.getPointGpaSatisfaction());
            this.clarity.setProgress(course.getPointClarity());
            this.easiness.setProgress(course.getPointEasiness());
            // TODO : tag

            this.overall.setEnabled(false);
            this.satisfaction.setEnabled(false);
            this.clarity.setEnabled(false);
            this.easiness.setEnabled(false);

        }
    }

    // TODO : UP / DOWN vote
    protected class SimpleEvaluationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        @InjectView(R.id.evaluation_user_avatar) protected ImageView avatar;
        @InjectView(R.id.evaluation_user_nickname) protected TextView nickname;
        @InjectView(R.id.evaluation_body) protected TextView body;
        @InjectView(R.id.evaluation_point_overall) protected RatingBar overall;
        public SimpleEvaluationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(EvaluationData evaluation) {
//            Picasso.with(this.itemView.getContext()).load("").transform(new CircleTransformation()).into(this.avatar); // TODO : Needs evaluation.user_avatar_url
//            this.nickname.setText(evaluation.user_name) // TODO : Needs evaluation.user_name
            this.body.setText(evaluation.body);
            this.overall.setRating((float)evaluation.point_overall); // TODO : More clear value
        }

        @Override
        public void onClick(View view) {
            evaluationItemClickListener.recyclerViewListClicked(view, this.getAdapterPosition() - 2);
        }
    }
}
