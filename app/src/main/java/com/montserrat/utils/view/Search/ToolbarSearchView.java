package com.montserrat.utils.view.search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.CourseData;
import com.montserrat.app.model.CoursesData;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.WidgetObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by SSS on 2015-11-06.
 */
public class ToolbarSearchView implements RecyclerViewItemClickListener {
    private static ToolbarSearchView instance;
    public static synchronized ToolbarSearchView getInstance(){
        if(ToolbarSearchView.instance == null) return ToolbarSearchView.instance = new ToolbarSearchView();
        return ToolbarSearchView.instance;
    }
    @InjectView(R.id.search_view_toolbar) protected RelativeLayout searchViewHeader;
    @InjectView(R.id.search_view_outside) protected View outsideView;
    @InjectView(R.id.toolbar_search_view) protected LinearLayout toolbarSearchViewContainer;
    @InjectView(R.id.search_view_back) protected ImageView btnBack;
    @InjectView(R.id.search_view_delete) protected ImageView btnClear;
    @InjectView(R.id.search_view_query) protected EditText query;
    @InjectView(R.id.search_view_result) protected RecyclerView searchResult;

    private LinearLayout searchView;

    private RecyclerViewItemClickListener itemClickListener;
    private RecyclerViewItemClickListener partialItemClickListener;
    private ToolbarSearchViewListener searchViewListener;
    private AutoCompleteAdapter autoCompleteAdapter;

    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;

    public void initializeToolbarSearchView(Context context, LinearLayout searchView, RecyclerViewItemClickListener listener){
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_search, searchView);
        ButterKnife.inject(this, view);

        this.searchView = searchView;
        this.searchView.setVisibility(View.GONE);
        this.context = context;
        outsideView.setOnClickListener(v->{
            this.hide();
        });
        this.candidates = new ArrayList<>();
        this.itemClickListener = listener;
        this.searchResult.setLayoutManager(new LinearLayoutManager(context));
        this.searchResult.setAdapter(getAdapter());

        this.subscription = new CompositeSubscription();
        searchAutoComplete();
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(partialItemClickListener != null)
            this.partialItemClickListener.onRecyclerViewItemClick(view, position);
        else
            this.itemClickListener.onRecyclerViewItemClick(view, position);
//        this.addHistory(this.candidates.get(position));
        this.hide();
    }

    public interface ToolbarSearchViewListener{
        public void onSearchViewShowChanged(boolean show);
    }

    private final long TEXTDEBOUNCE_MILLISEC = 400;
    private void searchAutoComplete(){
        this.subscription.add(
            WidgetObservable.text(this.query)
                .debounce(TEXTDEBOUNCE_MILLISEC, TimeUnit.MILLISECONDS)
                .filter(event -> event.text().toString().length() > 0)
                .map(event -> {
                    Timber.d("api call?");
                    return event;
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(event ->
                        Api.papyruth().search_autocomplete(
                            User.getInstance().getAccessToken(),
                            User.getInstance().getUniversityId(),
                            event.text().toString()
                        )
                )
                .map(response -> response.candidates)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(candidates -> {
                    notifyAutoCompleteDataChanged(candidates);
                }, error -> error.printStackTrace())
        );
    }

    private void notifyAutoCompleteDataChanged(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();


        ViewGroup.LayoutParams param;
        param =  searchResult.getLayoutParams();
        if(!this.candidates.isEmpty()) {
            if (this.candidates.size() < 5) {
                param.height = (int) (48 * this.candidates.size() * this.context.getResources().getDisplayMetrics().density);
            } else {
                param.height = (int) (240 * this.context.getResources().getDisplayMetrics().density);
            }
        }else{
            param.height = 0;
        }
        this.searchResult.setLayoutParams(param);
    }

    public ToolbarSearchView show(){
        this.searchView.setVisibility(View.VISIBLE);
        this.query.requestFocus();
        if(searchViewListener != null)
            searchViewListener.onSearchViewShowChanged(true);
        return this;
    }

    public ToolbarSearchView hide(){
        this.searchView.setVisibility(View.GONE);
        this.query.clearFocus();
        if(searchViewListener != null)
            searchViewListener.onSearchViewShowChanged(false);

        return this;
    }

    public ToolbarSearchView setPartialItemClickListener(RecyclerViewItemClickListener listener){
        this.partialItemClickListener = listener;
        return this;
    }
    public ToolbarSearchView setSearchViewListener(ToolbarSearchViewListener searchViewListener){
        this.searchViewListener = searchViewListener;
        return this;
    }

    public AutoCompleteAdapter getAdapter(){
        if(autoCompleteAdapter == null)
            return autoCompleteAdapter = new AutoCompleteAdapter(candidates, this);
        return this.autoCompleteAdapter;
    }

    public List<Candidate> getCandidates(){
        return this.candidates;
    }
    Candidate candidate;
    public Candidate getSelectedCandidate(){
        return candidate;
    }
    public ToolbarSearchView setSelectedCandidate(int position){
        if(candidate == null)
            candidate = new Candidate();
        candidate = candidates.get(position);
        return this;
    }



//    public void searchHistory(){
//        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
//            //Todo : when history is empty, inform empty.
//            this.courseItemsAdapter.setIsEmptyData(true);
//            this.courseItemsAdapter.setResIdNoDataText(R.string.no_data_history);
//        }else {
//            this.courseItemsAdapter.setIsEmptyData(false);
//            List<CourseData> courseList = ((CoursesData)AppManager.getInstance().getStringParsed(
//                AppConst.Preference.HISTORY,
//                CoursesData.class
//            )).courses;
//            searchView.notifyChangedCourseAsynchronized(courseList);
//        }
//    }

    private static final int HISTORY_SIZE = 10;
    public boolean addHistory(CourseData course){
        if(course.id == null)
            return false;
        List<CourseData> courseDataList;
        CoursesData coursesData = new CoursesData();
        coursesData.courses = new ArrayList<>();


        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            courseDataList = new ArrayList<>();
        }else {
            courseDataList  = ((CoursesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                CoursesData.class
            )).courses;
        }
        int index;
        if((index = containsCourse(courseDataList, course)) >= 0) {
            courseDataList.remove(index);
            courseDataList.add(course);
        }else if (courseDataList.size() > HISTORY_SIZE - 1) {
            courseDataList.remove(0);
            courseDataList.add(course);
            while(courseDataList.size() > HISTORY_SIZE - 1){
                courseDataList.remove(0);
            }
        }else{
            courseDataList.add(course);
        }
        coursesData.courses.clear();
        coursesData.courses.addAll(courseDataList);
        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, coursesData);
        return true;
    }
    public int containsCourse(List<CourseData> courses, CourseData target) {
        Timber.d("hash : %s", target.hashCode());
        for (CourseData course : courses) {
            Timber.d("hash : %s", course.hashCode());
            if (course.id.equals(target.id)) return courses.indexOf(course);
        }
        return -1;
    }
}
