package com.papyruth.android.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.papyruth.android.AppConst;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.Candidate;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.papyruth;
import com.papyruth.android.recyclerview.adapter.CourseItemsAdapter;
import com.papyruth.support.utility.error.ErrorHandler;
import com.papyruth.support.opensource.fab.FloatingActionControl;
import com.papyruth.support.opensource.materialdialog.AlertDialog;
import com.papyruth.support.opensource.retrofit.apis.Api;
import com.papyruth.support.utility.helper.ToolbarHelper;
import com.papyruth.support.utility.fragment.RecyclerViewFragment;
import com.papyruth.support.utility.navigator.Navigator;
import com.papyruth.support.utility.search.SearchToolbar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches SimpleCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends RecyclerViewFragment<CourseItemsAdapter, CourseData> {
    private Navigator mNavigator;
    private Context mContext;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mNavigator = (Navigator) activity;
        mContext = activity;
    }

    @InjectView(R.id.evaluation_form_query_button) protected Button mQueryButton;
    @InjectView(R.id.evaluation_form_query_result) protected RecyclerView mQueryResult;
    private CompositeSubscription mCompositeSubscription;
    private Toolbar mToolbar;
    private Tracker mTracker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = ((papyruth) getActivity().getApplication()).getTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        mCompositeSubscription = new CompositeSubscription();
        mQueryButton.setText(R.string.toolbar_search);
        mToolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);
        this.setupRecyclerView(mQueryResult);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        adapter = null;
        SearchToolbar.getInstance().setItemClickListener(null).setOnSearchByQueryListener(null);
        if(mCompositeSubscription == null || this.mCompositeSubscription.isUnsubscribed()) return;
        this.mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(items.size() -1 < position) {
            Toast.makeText(mContext, "please wait for loading", Toast.LENGTH_LONG).show();
            return;
        }
        final CourseData course = items.get(position);

        this.nextEvaluatonStep(course);

        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    private void nextEvaluatonStep(CourseData course){
        EvaluationForm.getInstance().setCourseId(course.id);
        EvaluationForm.getInstance().setLectureName(course.name);
        EvaluationForm.getInstance().setProfessorName(course.professor_name);
        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), course.id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    this.mNavigator.navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.show(mContext, mNavigator, AlertDialog.Type.EVALUATION_POSSIBLE);
                }
            }, error -> ErrorHandler.handle(error, this));
    }

    @Override
    protected CourseItemsAdapter getAdapter() {
        if(this.adapter != null) return this.adapter;
        return new CourseItemsAdapter(this.items, this, R.layout.cardview_white_0dp, R.string.no_data_search);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTracker.setScreenName(getResources().getString(R.string.ga_fragment_main_write_evaluation1));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mToolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarHelper.getColorTransitionAnimator(mToolbar, R.color.toolbar_green).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.getInstance().clear();

        SearchToolbar.getInstance()
            .setItemClickListener((v, position) -> searchCourse(SearchToolbar.getInstance().getCandidates().get(position), null))
            .setOnVisibilityChangedListener(visible -> mQueryButton.setVisibility(visible ? View.GONE : View.VISIBLE))
            .setOnSearchByQueryListener(() -> searchCourse(new Candidate(), SearchToolbar.getInstance().getSelectedQuery()));

        mCompositeSubscription.add(ViewObservable.clicks(mQueryButton)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> SearchToolbar.getInstance().show(), error -> ErrorHandler.handle(error, this))
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        SearchToolbar.getInstance().setOnVisibilityChangedListener((MainActivity) getActivity());
    }

    public void searchCourse(Candidate candidate, String query) {
        if(candidate.lecture_id != null){
            mQueryButton.setText(candidate.lecture_name);
        } else if(candidate.professor_id != null) {
            mQueryButton.setText(candidate.professor_name);
        }else if(query != null){
            mQueryButton.setText(query);
        }
        Api.papyruth().search_search(
            User.getInstance().getAccessToken(),
            User.getInstance().getUniversityId(),
            candidate.lecture_id,
            candidate.professor_id,
            query
        )
        .map(response -> response.courses)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
            if(candidate.lecture_id != null && candidate.professor_id != null & response.size() > 0){
                nextEvaluatonStep(response.get(0));
            }else {
                notifyAutoCompleteDataChanged(response);
            }
        }
            , error -> ErrorHandler.handle(error, this));
    }

    public void notifyAutoCompleteDataChanged(List<CourseData> courses){
        items.clear();
        items.addAll(courses);
        adapter.notifyDataSetChanged();
    }
}