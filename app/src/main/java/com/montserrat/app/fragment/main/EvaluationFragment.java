package com.montserrat.app.fragment.main;

import android.animation.ValueAnimator;
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
import com.montserrat.app.activity.MainActivity;
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
import com.montserrat.utils.view.viewpager.Page;
import com.montserrat.utils.view.viewpager.ViewPagerContainerController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.montserrat.utils.support.rx.RxValidator.isValidEvaluationBody;
import static com.montserrat.utils.support.rx.RxValidator.nonEmpty;
import static com.montserrat.utils.support.rx.RxValidator.toString;


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
    @InjectView(R.id.new_comment_close) protected Button newCommentClose;
    @InjectView(R.id.new_comment_body) protected EditText newCommentBody;
    private CompositeSubscription subscriptions;
    private CompositeSubscription subscriptionsFAB;
    private View view;
    private final long ANIMATION_SPEED = 600;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_evaluation, container, false);
        this.subscriptions = new CompositeSubscription();
        this.subscriptionsFAB = new CompositeSubscription();
        ButterKnife.inject(this, view);
        this.setupRecyclerView(commentList);

        setEvaluation();

        return view;
    }
    public void addComment(){
        changeFAB(COMMENT);
        commentWindow(true);

        this.subscriptions.add(
                ViewObservable.clicks(newCommentClose)
                        .subscribe(
                                unused -> {
                                    changeFAB(EVALUATION);
                                    commentWindow(false);
                                }, error ->
                                        Timber.d("new Comment Close error : %s", error)
                        )
        );
    }

    public void commentWindow(boolean open){
        if(open){
            ViewGroup.LayoutParams layoutParams = newCommentLayout.getLayoutParams();
            ValueAnimator animator = ValueAnimator.ofInt(0, (int)(this.getView().getHeight()*0.4));
            animator.setDuration(ANIMATION_SPEED);
            animator.addUpdateListener(animation ->{
                layoutParams.width = (int)(this.getView().getWidth() * 0.8);
                layoutParams.height = (int) animation.getAnimatedValue();
                newCommentLayout.setLayoutParams(layoutParams);
                newCommentLayout.setY(this.getView().getHeight() - (int) animation.getAnimatedValue());
            });
            animator.start();
        }else{
            ViewGroup.LayoutParams params = newCommentLayout.getLayoutParams();
            ValueAnimator animators = ValueAnimator.ofInt(newCommentLayout.getHeight(), 0);
            animators.setDuration(ANIMATION_SPEED);
            float y = newCommentLayout.getY();
            float h = newCommentLayout.getHeight();
            animators.addUpdateListener(animation -> {
                params.height = (int) animation.getAnimatedValue();
                newCommentLayout.setLayoutParams(params);
                newCommentLayout.setY(y + (h - (int) animation.getAnimatedValue()));
            });
            animators.start();
        }
    }


    private final int COMMENT = 0;
    private final int EVALUATION = 1;
    private final int COURSE = 2;

    public void changeFAB(Integer type){
        switch (type){
            case COMMENT :
                FloatingActionControl.getInstance().setControl(R.layout.fab_done);

                this.subscriptions.add(WidgetObservable
                                .text(this.newCommentBody)
                                .map(toString)
                                .map(nonEmpty)
                                .delay(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                                .subscribe(valid -> {
                                    boolean visible = FloatingActionControl.getButton().getVisibility() == View.VISIBLE;
                                    if (visible && !valid) FloatingActionControl.getInstance().hide(true);
                                    else if (!visible && valid) FloatingActionControl.getInstance().show(true);
                                })
                );
                this.subscriptionsFAB.add(FloatingActionControl
                            .clicks()
                            .observeOn(Schedulers.io())
                            .flatMap(click ->
                                            RetrofitApi.getInstance().comments(
                                                    User.getInstance().getAccessToken(),
                                                    Evaluation.getInstance().getId(),
                                                    this.newCommentBody.getText().toString()
                                            )
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_success), Toast.LENGTH_LONG).show();
                                        this.newCommentBody.setText("");
                                        getComments();
                                    },
                                    error -> {
                                        Toast.makeText(this.getActivity(), this.getResources().getString(R.string.submit_evaluation_fail), Toast.LENGTH_LONG).show();
                                        Timber.d("call add Comment error : %s", error);
                                    })
                            );
                break;

            case EVALUATION :
                FloatingActionControl.getInstance().setControl(R.layout.fam_comment).show(true, 200, TimeUnit.MILLISECONDS);
                this.subscriptionsFAB.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                            this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.EVALUATION_STEP2), true);
                                        },
                                        error -> Timber.d("add FAC fab_new_evaluation error : %s", error))
                );
                this.subscriptionsFAB.add(FloatingActionControl
                                .clicks(R.id.fab_comment)
                                .subscribe(unused -> {
                                            addComment();
                                        },
                                        error -> Timber.d("add FAC fab_comment error : %s", error))
                );
                break;

            case COURSE :
                FloatingActionControl.getInstance().setControl(R.layout.fam_home).show(true, 200, TimeUnit.MILLISECONDS);
                subscriptionsFAB.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                    EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                    EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                    EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                    this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.EVALUATION_STEP2), true);
                                }, error ->
                                        Timber.d("destroy view error : %s", error))
                );
                break;
            default:
                FloatingActionControl.getInstance().setControl(R.layout.fam_comment).show(true, 200, TimeUnit.MILLISECONDS);
                this.subscriptionsFAB.add(FloatingActionControl
                                .clicks(R.id.fab_new_evaluation)
                                .subscribe(unused -> {
                                            EvaluationForm.getInstance().setCourseId(Course.getInstance().getId());
                                            EvaluationForm.getInstance().setLectureName(Course.getInstance().getName());
                                            EvaluationForm.getInstance().setProfessorName(Course.getInstance().getProfessor());
                                            this.controller.setCurrentPage(Page.at(AppConst.ViewPager.Type.SEARCH, AppConst.ViewPager.Search.EVALUATION_STEP2), true);
                                        },
                                        error -> Timber.d("add FAC fab_new_evaluation error : %s", error))
                );
                this.subscriptionsFAB.add(FloatingActionControl
                                .clicks(R.id.fab_comment)
                                .subscribe(unused -> {
                                            addComment();
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
        Timber.d("onResume");
        getComments();
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
                                    Observable.timer(400, TimeUnit.MILLISECONDS)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(unused-> {
                                                        int maxHeight = 0;
                                                        for (int i = 0; i < commentList.getAdapter().getItemCount(); i++) {
                                                            maxHeight += commentList.getChildAt(i).getHeight();
                                                        }
                                                        ViewGroup.LayoutParams layoutParams = commentList.getLayoutParams();
                                                        layoutParams.height = maxHeight+((MainActivity) this.getActivity()).getActionbarHeight();
                                                        commentList.setLayoutParams(layoutParams);
                                                    }, error -> Timber.d("commentList error :%s",error)
                                    );
                                },
                                error -> Timber.d("get comments error : %s", error)
                        )
        );
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
    }
}
