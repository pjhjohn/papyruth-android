package com.montserrat.utils.view.search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.montserrat.app.AppConst;
import com.montserrat.app.AppManager;
import com.montserrat.app.R;
import com.montserrat.app.model.Candidate;
import com.montserrat.app.model.HistoryCandidatesData;
import com.montserrat.app.model.unique.User;
import com.montserrat.app.recyclerview.adapter.AutoCompleteAdapter;
import com.montserrat.utils.support.picasso.ColorFilterTransformation;
import com.montserrat.utils.support.retrofit.apis.Api;
import com.montserrat.utils.view.recycler.RecyclerViewItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
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
    @InjectView(R.id.toolbar_search_view) protected LinearLayout toolbarSearchViewContainer;
    @InjectView(R.id.search_view_back) protected ImageView btnBack;
    @InjectView(R.id.search_view_delete) protected ImageView btnClear;
    @InjectView(R.id.search_view_query) protected PreImeEditText query;
    @InjectView(R.id.search_view_result) protected RecyclerView searchResult;

    private LinearLayout searchView;

    private RecyclerViewItemClickListener itemClickListener;
    private RecyclerViewItemClickListener partialItemClickListener;
    private ToolbarSearchViewListener searchViewListener;
    private AutoCompleteAdapter autoCompleteAdapter;
    private ToolbarSearchViewSearchListener toolbarSearchListener;

    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;

    public void initializeToolbarSearchView(Context context, LinearLayout searchView, RecyclerViewItemClickListener listener){
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_search, searchView, true);
        ButterKnife.inject(this, view);
        if(candidates == null)
            this.candidates = new ArrayList<>();

        this.searchView = searchView;
        this.context = context;
        this.itemClickListener = listener;

        this.searchResult.setLayoutManager(new LinearLayoutManager(context));
        this.searchResult.setAdapter(getAdapter());
        this.query.setPreImeListener(() -> {
            if(this.searchView.getVisibility() == View.VISIBLE)
                this.hide();
        });
        this.searchResult.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener(){
            boolean touchDown = false;
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if(child != null) {
                    touchDown = false;
                    return super.onInterceptTouchEvent(rv, e);
                }else if(e.getAction() == KeyEvent.ACTION_DOWN){
                    touchDown = !touchDown;
                }else if(e.getAction() == KeyEvent.ACTION_UP) {
                    if(touchDown)
                        hide();
                    touchDown = false;
                    return true;
                }
                return false;
            }
        });


        Picasso.with(context).load(R.drawable.ic_light_clear).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(btnClear);
        Picasso.with(context).load(R.drawable.ic_light_back).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.icon_material))).into(btnBack);

        if(this.searchView.getVisibility() == View.VISIBLE)
            this.hide();

        if(this.subscription == null) {
            this.subscription = new CompositeSubscription();
        }
        initComponents();

    }

    private final long TEXTDEBOUNCE_MILLISEC = 400;
    private void initComponents(){
        this.subscription.add(
            WidgetObservable.text(this.query)
                .doOnNext(event -> {
                    if (event.text().length() > 0) {
                        this.btnClear.setVisibility(View.VISIBLE);
                    }else {
                        this.btnClear.setVisibility(View.GONE);
                        this.autoCompleteAdapter.setHistory(true);
                        this.notifyAutoCompleteDataChanged(getHistory());
                    }

                })
                .debounce(TEXTDEBOUNCE_MILLISEC, TimeUnit.MILLISECONDS)
                .filter(event -> event.text().toString().length() > 0)
                .map(event -> event.text().toString())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    query -> this.searchAutocomplete(query)
                    ,error -> {
                        Timber.d("ERROR : query text error");
                        error.printStackTrace();
                    }
                )
        );
        this.btnClear.setVisibility(View.GONE);

        this.subscription.add(
            ViewObservable.clicks(this.btnClear)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    this.query.getText().clear();
                    this.autoCompleteAdapter.setHistory(true);
                    this.notifyAutoCompleteDataChanged(getHistory());
                }, error -> {
                    Timber.e("ERROR : clear button click event error");
                    error.printStackTrace();
                })
        );

        this.subscription.add(
            ViewObservable.clicks(this.btnBack)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(event ->
                    this.hide()
                    , error -> {
                    Timber.e("ERROR : back button click event error");
                    error.printStackTrace();
                })
        );

        this.query.setOnKeyListener((v, keycode, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN && (keycode == KeyEvent.KEYCODE_ENTER || keycode == KeyEvent.KEYCODE_SEARCH)) {
                this.setSelectedQuery(this.query.getText().toString());
                if(toolbarSearchListener != null)
                    this.toolbarSearchListener.onSearchByQuery();
                else
                    this.searchViewListener.onSearchByQuery();
                this.hide();
                return true;
            }
            return false;
        });
    }

    private void searchAutocomplete(String query){
        Api.papyruth().search_autocomplete(
            User.getInstance().getAccessToken(),
            User.getInstance().getUniversityId(),
            query
        )
            .map(response -> response.candidates)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(candidates -> {
                this.autoCompleteAdapter.setHistory(false);
                notifyAutoCompleteDataChanged(candidates);
            }, error -> error.printStackTrace());
    }


    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(partialItemClickListener != null)
            this.partialItemClickListener.onRecyclerViewItemClick(view, position);
        else
            this.itemClickListener.onRecyclerViewItemClick(view, position);
        this.hide();
    }

    private void notifyAutoCompleteDataChanged(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();
    }

    public boolean back(){
        if(searchView.getVisibility() == View.VISIBLE) {
            this.hide();
            return true;
        }
        return false;
    }

    public ToolbarSearchView show(){
        this.searchView.setVisibility(View.VISIBLE);
        this.query.requestFocus();
        if(this.query.requestFocus()) {
            ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this.query, InputMethodManager.SHOW_IMPLICIT);
        }

        this.autoCompleteAdapter.setHistory(true);
        this.notifyAutoCompleteDataChanged(getHistory());

        if(searchViewListener != null)
            searchViewListener.onSearchViewShowChanged(true);
        return this;
    }

    public ToolbarSearchView hide(){
        this.searchView.setVisibility(View.GONE);
        this.query.clearFocus();
        if(searchViewListener != null)
            searchViewListener.onSearchViewShowChanged(false);
        this.notifyAutoCompleteDataChanged(new ArrayList<>());
        this.query.getText().clear();
        ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.query.getWindowToken(), 2);

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
    public ToolbarSearchView setToolbarSearchViewSearchListener(ToolbarSearchViewSearchListener listener){
        this.toolbarSearchListener = listener;
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
    private Candidate selectedCandidate;
    private String selectedQuery;
    public Candidate getSelectedCandidate(){
        return selectedCandidate;
    }
    public ToolbarSearchView setSelectedCandidate(int position){
        if(selectedCandidate == null)
            selectedCandidate = new Candidate();
        selectedCandidate = candidates.get(position);
        selectedQuery = null;
        return this;
    }

    public String getSelectedQuery(){
        return selectedQuery;
    }

    public ToolbarSearchView setSelectedQuery(String query){
        this.selectedQuery = query;
        this.selectedCandidate = new Candidate();
        return this;
    }



    public List<Candidate> getHistory(){
        List<Candidate> courseList = new ArrayList<>();
        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            //Todo : when history is empty, inform empty.
        }else {
             courseList = ((HistoryCandidatesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                HistoryCandidatesData.class
            )).candidates;
        }
        return courseList;
    }

    private static final int HISTORY_SIZE = 10;
    public boolean addHistory(Candidate candidate){
        if(candidate.lecture_id == null && candidate.professor_id == null)
            return false;
        List<Candidate> candidateList;
        HistoryCandidatesData historyCandidates = new HistoryCandidatesData();
        historyCandidates.candidates = new ArrayList<>();


        if(!AppManager.getInstance().contains(AppConst.Preference.HISTORY)){
            candidateList = new ArrayList<>();
        }else {
            candidateList  = ((HistoryCandidatesData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                HistoryCandidatesData.class
            )).candidates;
        }
        int index;
        if((index = containsCourse(candidateList, candidate)) >= 0) {
            candidateList.remove(index);
            candidateList.add(candidate);
        }else if (candidateList.size() > HISTORY_SIZE - 1) {
            candidateList.remove(0);
            candidateList.add(candidate);
            while(candidateList.size() > HISTORY_SIZE - 1){
                candidateList.remove(0);
            }
        }else{
            candidateList.add(candidate);
        }
        historyCandidates.candidates.clear();
        historyCandidates.candidates.addAll(candidateList);
        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, historyCandidates);
        return true;
    }

    public int containsCourse(List<Candidate> candidates, Candidate target) {
        for (Candidate course : candidates) {
            if ((course.lecture_id != null && course.lecture_id.equals(target.lecture_id)) || ( course.professor_id != null &&course.professor_id.equals(target.professor_id))) {
                return candidates.indexOf(course);
            }
        }
        return -1;
    }

    public interface ToolbarSearchViewListener extends ToolbarSearchViewSearchListener{
        void onSearchViewShowChanged(boolean show);
    }
    public interface ToolbarSearchViewSearchListener{
        void onSearchByQuery();
    }
}
