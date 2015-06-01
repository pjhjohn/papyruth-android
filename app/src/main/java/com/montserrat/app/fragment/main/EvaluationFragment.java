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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.montserrat.utils.view.viewpager.ViewPagerController;

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
    @InjectView(R.id.new_comment_layout) protected LinearLayout newCommentLayout;
    @InjectView(R.id.new_comment_close) protected Button newCommentClose;
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
    public void addComment(Integer width, Integer height){
        changeFAB(COMMENT);
        ViewGroup.LayoutParams layoutParams = newCommentLayout.getLayoutParams();
        layoutParams.width = (int)(width * 0.8);
        layoutParams.height = (int)(height * 0.4);
        newCommentLayout.setLayoutParams(layoutParams);
        this.subscriptions.add(
                ViewObservable.clicks(newCommentClose)
                        .subscribe(
                                unused -> {
                                    ViewGroup.LayoutParams params = newCommentLayout.getLayoutParams();
                                    params.height = 0;
                                    newCommentLayout.setLayoutParams(params);
                                    changeFAB(EVALUATION);
                                }, error ->
                                        Timber.d("new Comment Close error : %s", error)
                        )
        );
    }

    private final int COMMENT = 0;
    private final int EVALUATION = 1;
    private final int COURSE = 2;

    public void changeFAB(Integer type){
        switch (type){
            case COMMENT :
                FloatingActionControl.getInstance().setControl(R.layout.fab_done).show(true, 200, TimeUnit.MILLISECONDS);
                this.subscriptions.add(
                        FloatingActionControl.clicks(R.id.fab_done)
                            .observeOn(Schedulers.io())
                            .map(click ->
                                    RetrofitApi.getInstance().comments(
                                            User.getInstance().getAccessToken(),
                                            Evaluation.getInstance().getId(),
                                            this.newCommentBody.getText().toString()
                                    )
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(pass ->{
                                        Timber.d("success : %s", pass);
                                    },
                                    error -> {
                                        Timber.d("call add Comment error : %s", error);
                                        error.printStackTrace();})
                            );
                break;

            case EVALUATION :
                FloatingActionControl.getInstance().setControl(R.layout.fam_comment).show(true, 200, TimeUnit.MILLISECONDS);
                this.subscriptions.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                            pagerController.setCurrentPage(AppConst.ViewPager.Search.EVALUATION_STEP2, true);
                                        },
                                        error -> Timber.d("add FAC fab_new_evaluation error : %s", error))
                );
                this.subscriptions.add(FloatingActionControl
                                .clicks(R.id.fab_comment)
                                .subscribe(unused -> {
                                            addComment(this.getView().getWidth(),this.getView().getHeight());
                                        },
                                        error -> Timber.d("add FAC fab_comment error : %s", error))
                );
                break;

            case COURSE :
                FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
                subscriptions.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                    EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                    EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                    EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                    pagerController.setCurrentPage(AppConst.ViewPager.Search.EVALUATION_STEP2, true);
                                }, error ->
                                        Timber.d("destroy view error : %s", error))
                );
                break;
            default:
                FloatingActionControl.getInstance().setControl(R.layout.fam_comment).show(true, 200, TimeUnit.MILLISECONDS);
                this.subscriptions.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                            pagerController.setCurrentPage(AppConst.ViewPager.Search.EVALUATION_STEP2, true);
                                        },
                                        error -> Timber.d("add FAC fab_new_evaluation error : %s", error))
                );
                this.subscriptions.add(FloatingActionControl
                                .clicks(R.id.fab_comment)
                                .subscribe(unused -> {
                                            addComment(this.getView().getWidth(),this.getView().getHeight());
                                        },
                                        error -> Timber.d("add FAC fab_comment error : %s", error))
                );
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFAB(EVALUATION);
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
        changeFAB(COURSE);
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
                    error -> Timber.d("get comments error : %s", error)
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
