package com.montserrat.utils.view.search;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Search;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.app.recyclerview.adapter.CourseItemsAdapter;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AutoCompletableSearchView2 {

    /* view */
    private EditText queryView;
    private RecyclerView autocompleteListView;
    private RecyclerView resultCourseListView;
    private View outsideTouchableView;

    /* listener */
    private RecyclerViewItemClickListener itemClickListener;
    private SearchViewListener searchViewListener;

    /* adapter */
    private AutoCompleteAdapter autoCompleteAdapter;
    private CourseItemsAdapter courseItemsAdapter;


    private CompositeSubscription subscription;
    private Context context;

    /* data */
    private List<Candidate> candidates;
    private List<CourseData> courseDatas;
    //ONLY use EVALUATION
    private Candidate evaluationCandidate;
    private String evaluationQuery;

    /*flag*/
    private boolean isOpen;
    private boolean setOpen;
    private boolean reserveCandidateListOnClose;

    public interface SearchViewListener{
        void onTextChange(String query);
        void onShowChange(boolean open);
        void submitQuery(String query);
    }
    public void setSearchViewListener(SearchViewListener listener){
        this.searchViewListener = listener;
    }

    public AutoCompletableSearchView2(RecyclerViewItemClickListener listener, Context context){
        this.itemClickListener = listener;
        this.context = context;
        reserveCandidateListOnClose = false;
        isOpen = false;
        setOpen = false;
    }

    public void initAutocompleteView(RecyclerView autocompleteListView, View outsideTouchableView, EditText editText,AutoCompleteAdapter adapter){
        this.autocompleteListView = autocompleteListView;
        this.outsideTouchableView = outsideTouchableView;

        this.autoCompleteAdapter = adapter;
        this.autocompleteListView.setLayoutManager(new LinearLayoutManager(context));
        this.autocompleteListView.setAdapter(this.autoCompleteAdapter);
        this.outsideTouchableView.setOnClickListener(view -> showCandidates(false));

        this.queryView = editText;
    }
    public void initResultView(RecyclerView resultCouresListView, CourseItemsAdapter adapter){
        this.resultCourseListView = resultCouresListView;
        this.courseItemsAdapter = adapter;
        this.resultCourseListView.setLayoutManager(new LinearLayoutManager(context));
        this.resultCourseListView.setAdapter(courseItemsAdapter);
    }

    public void notifyChangedAutocomplete(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
        this.updateAutoCompleteViewHeight();
    }


    public void notifyChangedCourse(List<CourseData> courses){
        this.courseDatas.clear();
        this.courseDatas.addAll(courses);
        this.courseItemsAdapter.notifyDataSetChanged();
    }
    public void notifyChangedCourseAsynchronized(List<CourseData> courses){
        this.courseDatas.clear();
        for (int i = 0; i < courses.size(); i++) {
            final int j = courses.size() - i - 1;
            this.courseDatas.add(CourseData.Sample());
            this.subscription.add(
                RetrofitApi.getInstance().search_search(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    courses.get(i).lecture_id,
                    courses.get(i).professor_id,
                    null
                )
                    .map(response -> response.courses)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        course -> {
                            this.courseDatas.set(j, course.get(0));
                            this.courseItemsAdapter.notifyDataSetChanged();
                        },
                        error -> error.printStackTrace()
                    )
            );
        }
    }

    public void autocomplete(){
        this.queryView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                this.showCandidates(true);
        });

        this.subscription.add(
            WidgetObservable
                .text(this.queryView)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> this.setOpen = true)
        );

        this.subscription.add(
            WidgetObservable
                .text(this.queryView)
                .map(listener -> {
                    if (searchViewListener != null)
                        searchViewListener.onTextChange(listener.text().toString());
                    return listener.text().toString();
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(text -> !text.isEmpty())
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(query -> RetrofitApi.getInstance().search_autocomplete(
                    User.getInstance().getAccessToken(),
                    User.getInstance().getUniversityId(),
                    query
                ))
                .map(response -> response.candidates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    /* removed flag setOpen*/
                    if (isOpen && setOpen)
                        this.showCandidates(true);
                    this.notifyChangedAutocomplete(result);
                }, error -> {
                    showCandidates(setOpen);
//                    if (error instanceof RetrofitError)
                    error.printStackTrace();
                })
        );
    }

    public void submitQuery(){
        this.submitQuery(this.queryView.getText().toString());
    }

    public void submitQuery(String query){
        this.searchViewListener.submitQuery(query);
        this.showCandidates(false);
        this.queryView.clearFocus();
    }

    public void setOpen(boolean open){
        this.setOpen = open;
    }

    public void showCandidates(boolean show){
        ViewGroup.LayoutParams param;
        this.isOpen = show;
        if(show){
            if(this.queryView.getText().toString().length() == 0)
                this.candidates.clear();
            this.updateAutoCompleteViewHeight();

            this.outsideTouchableView.setAlpha((float)0.7);
            this.outsideTouchableView.setBackgroundColor(Color.GRAY);

//            param = this.outsideTouchableView.getLayoutParams();
            this.outsideTouchableView.setVisibility(View.VISIBLE);
        }else{
            this.setOpen = false;
            this.queryView.clearFocus();
            this.updateAutoCompleteViewHeight();

            ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.queryView.getWindowToken(), 2);

            this.outsideTouchableView.setVisibility(View.GONE);
            if(!reserveCandidateListOnClose)
                this.candidates.clear();
        }

        if(this.searchViewListener != null)
            this.searchViewListener.onShowChange(show);
    }

    private void updateAutoCompleteViewHeight() {
        ViewGroup.LayoutParams param;
        param =  autocompleteListView.getLayoutParams();
        if(isOpen) {
            if (this.candidates.size() < 5) {
                param.height = (int) (48 * this.candidates.size() * this.context.getResources().getDisplayMetrics().density);
            } else {
                param.height = (int) (240 * this.context.getResources().getDisplayMetrics().density);
            }
        }else{
            param.height = 0;
        }
        this.autocompleteListView.setLayoutParams(param);
    }

    public void searchCourse(){
        Integer lectureId, professorId;
        String query;

        if(this.hasEvaluationData()){
            lectureId = this.evaluationCandidate.lecture_id;
            professorId = this.evaluationCandidate.professor_id;
            query = this.evaluationQuery;
        }else{// if(!Search.getInstance().isEmpty()){
            lectureId = Search.getInstance().getLectureId();
            professorId = Search.getInstance().getProfessorId();
            query = Search.getInstance().getQuery();
        }
        searchCourse(lectureId, professorId, query);
    }

    public void searchCourse(Integer lectureId, Integer professorId, String query){

        this.setOpen = false;

        this.subscription.add(
            RetrofitApi.getInstance().search_search(
                User.getInstance().getAccessToken(),
                User.getInstance().getUniversityId(),
                lectureId,
                professorId,
                query
            )
                .map(response -> response.courses)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> this.showCandidates(false))
                .subscribe(
                    this::notifyChangedCourse,
                    error -> error.printStackTrace())
        );
    }

    public boolean onRecyclerViewItemClick(View view, int position) {
        if(autocompleteListView != null && ((RecyclerView)view.getParent()).getId() == autocompleteListView.getId()) {
            Search.getInstance().clear();
            Search.getInstance().fromCandidate(candidates.get(position));
        }else if(resultCourseListView != null && ((RecyclerView)view.getParent()).getId() == resultCourseListView.getId()){
            Timber.d("items data : <%s><%s><%s><%s><%s>", courseDatas.get(position).name, courseDatas.get(position).id, courseDatas.get(position).professor_name, courseDatas.get(position).professor_photo_url, courseDatas.get(position).is_favorite);
            if(courseDatas.get(position).id == null || courseDatas.get(position).id < 0){
                Toast.makeText(context, context.getResources().getText(R.string.wait_to_loading), Toast.LENGTH_SHORT).show();
                return false;
            }
            Course.getInstance().clear().update(courseDatas.get(position));
//            this.addHistory(courseDatas.get(position));
        }
        return true;
    }

    public CourseData getCourseItem(int position){
        return this.courseDatas.get(position);
    }

    public boolean onBack(){
        /* before use flag isautocompleteViewOpen */
        if(isOpen){
            this.showCandidates(false);
            return true;
        }

        return false;
    }


    public void setEvaluationCandidateByPosition(int position) {
        this.evaluationCandidate.clear();

        this.evaluationQuery = null;
        this.evaluationCandidate = candidates.get(position);
    }
    public void setEvaluationCandidateQuery(String query){
        this.evaluationCandidate.clear();
        this.evaluationQuery = query;
    }
    public void clearCandidates(){
        this.evaluationCandidate = null;
        this.evaluationQuery = null;
    }
    public boolean hasEvaluationData(){
        if(this.evaluationCandidate.lecture_id != null || this.evaluationCandidate.professor_id != null || evaluationQuery != null){
            return true;
        }
        return false;
    }
    // not implement hasData();
//
//    private static class onother{
//
//        private Candidate evaluationCandidate;
//        private String evaluationQuery;
//
//        public void setEvaluationCandidate(int position) {
//            this.evaluationCandidate.clear();
//
//            this.evaluationQuery = null;
//            this.evaluationCandidate = candidates.get(position);
//        }
//        public void setEvaluationCandidate(String query){
//            this.evaluationCandidate.clear();
//            this.evaluationQuery = query;
//        }
//
//
//
//        public void setSearchMode(boolean searchMode){
//            this.searchMode = searchMode;
//        }
//    }
}
