package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.app.adapter.CommentAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.Comment;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


/**
 * Created by SSS on 2015-05-22.
 */
public class EvaluationFragment extends RecyclerViewFragment<CommentAdapter, Comment> {
    private ViewPagerController pagerController;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView(R.id.nickname) protected TextView name;
    @InjectView(R.id.body) protected TextView body;
    @InjectView(R.id.comment_list) protected RecyclerView commentList;
    private CompositeSubscription subscription;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        this.subscription = new CompositeSubscription();
        ButterKnife.inject(this, view);
        this.setupRecyclerView(commentList);

        setEvaluation();
        this.items.clear();
        this.items.add(newComment("1", "commentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcommentcomment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.items.add(newComment("1", "comment"));
        this.adapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        recyclerviewHeightChanged();
        Timber.d("isnull - %s", commentList.getChildCount());
    }


    private void recyclerviewHeightChanged() {
        int maxHeight = 0;
        for (int i = 0; i < commentList.getAdapter().getItemCount(); i++) {
            maxHeight += commentList.getChildAt(i).getHeight();
            Timber.d("max - %s", maxHeight);
        }
        commentList.setMinimumHeight(maxHeight);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        Evaluation.getInstance().clear();
    }

    public void setEvaluation() {
        this.name.setText(Evaluation.getInstance().getLecture_name());
        this.body.setText(Evaluation.getInstance().getBody());
    }

    public Comment newComment(String name, String content) {
        Comment comment = new Comment();
        comment.user_name = name;
        comment.body = content;

        return comment;
    }

    @Override
    protected CommentAdapter getAdapter(List<Comment> comments) {

        return CommentAdapter.newInstance(this.items, this);
    }

    @Override
    protected RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {
//        Timber.d("hgh : %s", commentList.get);
//        commentList.getLayoutManager().geth
        recyclerviewHeightChanged();
    }
}
