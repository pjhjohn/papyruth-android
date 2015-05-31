package com.montserrat.app.fragment.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montserrat.app.AppConst;
import com.montserrat.app.R;
import com.montserrat.app.adapter.CommentAdapter;
import com.montserrat.app.fragment.nav.NavFragment;
import com.montserrat.app.model.Comment;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;
import com.montserrat.app.model.unique.EvaluationForm;
import com.montserrat.app.model.unique.User;
import com.montserrat.utils.support.fab.FloatingActionControl;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.fragment.RecyclerViewFragment;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


/**
 * Created by SSS on 2015-05-22.
 */
public class EvaluationFragment extends RecyclerViewFragment<CommentAdapter, Comment> {
    private ViewPagerContainerController controller;
    private NavFragment.OnCategoryClickListener callback;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.controller = (ViewPagerContainerController) activity;
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @InjectView(R.id.nickname) protected TextView name;
    @InjectView(R.id.body) protected TextView body;
    @InjectView(R.id.comment_list) protected RecyclerView commentList;
    @InjectView(R.id.new_comment_layout) protected LinearLayout newCommentLayout;
    @InjectView(R.id.new_comment_submit) protected Button newCommentSubmit;
    @InjectView(R.id.new_comment_body) protected EditText newCommentBody;
    private CompositeSubscription subscriptions;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        this.subscriptions = new CompositeSubscription();
        ButterKnife.inject(this, view);
        this.setupRecyclerView(commentList);

        setEvaluation();
        getComments();

        return view;
    }
    public void addComment(Integer height){
        Timber.d("call by course %s", height);
        ViewGroup.LayoutParams layoutParams = newCommentLayout.getLayoutParams();
        layoutParams.height = (int)(height * 0.08);
        newCommentLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionControl.getInstance().setControl(R.layout.fam_comment).show(true, 200, TimeUnit.MILLISECONDS);
        this.subscriptions.add(FloatingActionControl
                        .clicks(R.id.fab_new_evaluation)
                        .subscribe(unused -> {
                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                            pagerController.setCurrentPage(AppConst.ViewPager.Search.EVALUATION_STEP2, true);
                        },
                                error -> Timber.d("error : %s", error))
        );
        this.subscriptions.add(FloatingActionControl
                        .clicks(R.id.fab_comment)
                        .subscribe(unused -> {
                                    addComment(this.getView().getHeight());
                                },
                                error -> Timber.d("error : %s", error))
        );
        this.subscriptions.add(
                ViewObservable.clicks(newCommentSubmit)
                .subscribe(
                        unused -> RetrofitApi.getInstance()
                                .comments(
                                        User.getInstance().getAccessToken(),
                                        Evaluation.getInstance().getId(),
                                        this.newCommentBody.getText().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> Timber.d("success : %s", response),
                                error -> Timber.d("error : %s", error))
                )
        );
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
        Timber.d("is gone!");
        Evaluation.getInstance().clear();
        FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
        subscriptions.add(FloatingActionControl
                        .clicks(R.id.fab_new_evaluation)
                        .subscribe(unused -> {
                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                            pagerController.setCurrentPage(AppConst.ViewPager.Search.EVALUATION_STEP2, true);
                        })
        );
    }

    public void setEvaluation() {
        this.name.setText(Evaluation.getInstance().getLecture_name());
        this.body.setText(Evaluation.getInstance().getBody());
    }

    public void getComments(){
        this.subscriptions.add(
            RetrofitApi.getInstance().comments(
                User.getInstance().getAccessToken(),
                Evaluation.getInstance().getId(),
                null,
                null
            )
            .map(response -> response.comments)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    comments -> {
                        this.items.clear();
                        this.items.addAll(comments);
                        this.adapter.notifyDataSetChanged();
                    },
                    error -> Timber.d("error : %s", error)
            )
        );
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
        recyclerviewHeightChanged();
    }
}
