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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.papyruth.android.AppConst;
import com.papyruth.android.model.CourseData;
import com.papyruth.android.model.unique.EvaluationForm;
import com.papyruth.android.R;
import com.papyruth.android.activity.MainActivity;
import com.papyruth.android.model.Candidate;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.CourseItemsAdapter;
import com.papyruth.utils.support.error.ErrorHandler;
import com.papyruth.utils.support.fab.FloatingActionControl;
import com.papyruth.utils.support.materialdialog.AlertDialog;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.ToolbarUtil;
import com.papyruth.utils.view.fragment.RecyclerViewFragment;
import com.papyruth.utils.view.navigator.Navigator;
import com.papyruth.utils.view.search.ToolbarSearchView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.RetrofitError;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by pjhjohn on 2015-04-26.
 * Searches SimpleCourse for Evaluation on Step 1.
 */
public class EvaluationStep1Fragment extends RecyclerViewFragment<CourseItemsAdapter, CourseData> {
    private Navigator navigator;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.navigator = (Navigator) activity;
    }

    @InjectView(R.id.query_text_view) protected TextView queryTextView;
    @InjectView(R.id.search_icon) protected ImageView searchIcon;
    @InjectView(R.id.course_list) protected RecyclerView courseList;
    private CompositeSubscription subscriptions;

    private Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = inflater.inflate(R.layout.fragment_evaluation_step1, container, false);
        ButterKnife.inject(this, view);
        this.subscriptions = new CompositeSubscription();
        this.setupRecyclerView(this.courseList);

        toolbar = (Toolbar) this.getActivity().findViewById(R.id.toolbar);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        this.adapter = null;
        ToolbarSearchView.getInstance().setPartialItemClickListener(null).setToolbarSearchViewSearchListener(null);
        if(this.subscriptions!=null&&!this.subscriptions.isUnsubscribed())this.subscriptions.unsubscribe();
    }


    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(this.items.size() -1 < position){
            Toast.makeText(getActivity().getBaseContext(), "please wait for loading", Toast.LENGTH_LONG).show();
            return;
        }
        EvaluationForm.getInstance().setLectureName(this.items.get(position).name);
        EvaluationForm.getInstance().setProfessorName(this.items.get(position).professor_name);
        EvaluationForm.getInstance().setCourseId(this.items.get(position).id);

        Api.papyruth().post_evaluation_possible(User.getInstance().getAccessToken(), this.items.get(position).id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(response -> {
                if (response.success) {
                    this.navigator.navigate(EvaluationStep2Fragment.class, true);
                } else {
                    EvaluationForm.getInstance().setEvaluationId(response.evaluation_id);
                    AlertDialog.build(getActivity(), navigator, AlertDialog.Type.EVALUATION_POSSIBLE)
                        .show();
                }
            }, error -> {
                ErrorHandler.throwError(error, this);
            });
        ((InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }


    @Override
    protected CourseItemsAdapter getAdapter() {
        if(this.adapter != null) return this.adapter;
        return new CourseItemsAdapter(this.items, this, R.layout.cardview_white_0dp, R.string.no_data_search);
    }

    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(R.string.toolbar_title_new_evaluation);
        ToolbarUtil.getColorTransitionAnimator(toolbar, R.color.colorchip_green).start();
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SETTING, false);
        ((MainActivity) getActivity()).setMenuItemVisibility(AppConst.Menu.SEARCH, false);
        FloatingActionControl.getInstance().clear();
        Picasso.with(getActivity()).load(R.drawable.ic_light_search).transform(new ColorFilterTransformation(R.color.primary_material_dark)).into(searchIcon);

        ToolbarSearchView.getInstance()
            .setPartialItemClickListener((v, position) -> {
            searchCourse(ToolbarSearchView.getInstance().getCandidates().get(position), null);
        })
            .setToolbarSearchViewSearchListener(()->{
            searchCourse(new Candidate(), ToolbarSearchView.getInstance().getSelectedQuery());
        });

        this.subscriptions.add(
            ViewObservable.clicks(queryTextView)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    event ->
                        ToolbarSearchView.getInstance().show()
                    ,error ->
                        ErrorHandler.throwError(error, this)
                )
        );
    }

    public void searchCourse(Candidate candidate, String query){
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
            .subscribe(courses -> {
                notifyAutoCompleteDataChanged(courses);
            }, error -> ErrorHandler.throwError(error, this));
    }

    public void notifyAutoCompleteDataChanged(List<CourseData> courses){
        this.items.clear();
        this.items.addAll(courses);
        this.adapter.notifyDataSetChanged();
    }
}