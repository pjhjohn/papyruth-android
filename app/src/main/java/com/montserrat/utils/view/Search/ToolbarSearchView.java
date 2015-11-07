package com.montserrat.utils.view.search;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    @InjectView(R.id.search_view_outside) protected View outsideView;
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

    private CompositeSubscription subscription;
    private Context context;

    private List<Candidate> candidates;

    public void initializeToolbarSearchView(Context context, LinearLayout searchView, RecyclerViewItemClickListener listener){
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_search, searchView);
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

        Picasso.with(context).load(R.drawable.ic_light_clear).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.nav_filter))).into(btnClear);
        Picasso.with(context).load(R.drawable.ic_light_back).transform(new ColorFilterTransformation(context.getResources().getColor(R.color.nav_filter))).into(btnBack);

        if(this.searchView.getVisibility() == View.VISIBLE)
            this.hide();
        outsideView.setOnClickListener(v->{
            this.hide();
        });

        if(this.subscription == null) {
            this.subscription = new CompositeSubscription();
            initComponents();
        }

    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(partialItemClickListener != null)
            this.partialItemClickListener.onRecyclerViewItemClick(view, position);
        else
            this.itemClickListener.onRecyclerViewItemClick(view, position);
        this.addHistory(this.candidates.get(position));

        this.hide();
    }

    public interface ToolbarSearchViewListener{
        public void onSearchViewShowChanged(boolean show);
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
                notifyAutoCompleteDataChanged(candidates);
            }, error -> error.printStackTrace());
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
                        this.notifyAutoCompleteDataChanged(getHistory());
                    }

                })
                .debounce(TEXTDEBOUNCE_MILLISEC, TimeUnit.MILLISECONDS)
                .filter(event -> event.text().toString().length() > 0)
                .map(event -> event.text().toString())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    query -> this.searchAutocomplete(query)
                    ,error -> error.printStackTrace()
                )
        );
        this.btnClear.setVisibility(View.GONE);

        this.subscription.add(
            ViewObservable.clicks(this.btnClear)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    this.query.setText("");
                    this.notifyAutoCompleteDataChanged(getHistory());
                }, error -> error.printStackTrace())
        );

        this.subscription.add(
            ViewObservable.clicks(this.btnBack)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event ->
                    this.hide()
                    , error -> error.printStackTrace())
        );

//        this.query.setOnKeyListener((v, keycode, e) -> {
//            if (e.getAction() == KeyEvent.ACTION_UP && keycode == KeyEvent.KEYCODE_ENTER) {
//                this.search.submitQuery();
//                return true;
//            }
//            return false;
//        });
    }

    private void notifyAutoCompleteDataChanged(List<Candidate> candidates){
        this.candidates.clear();
        this.candidates.addAll(candidates);
        this.autoCompleteAdapter.notifyDataSetChanged();

        ViewGroup.LayoutParams param;
        param =  searchResult.getLayoutParams();
        if(!this.candidates.isEmpty()) {
            if (this.candidates.size() < 5) {
                param.height = (int) (56 * this.candidates.size() * this.context.getResources().getDisplayMetrics().density);
            } else {
                param.height = (int) (240 * this.context.getResources().getDisplayMetrics().density);
            }
        }else{
            param.height = 0;
        }
        this.searchResult.setLayoutParams(param);
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
        this.query.setText("");
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
        Timber.d("hash : %s", target.hashCode());
        for (Candidate course : candidates) {
            Timber.d("hash : %s", course.hashCode());
            if ((course.lecture_id != null && course.lecture_id.equals(target.lecture_id)) || ( course.professor_id != null &&course.professor_id.equals(target.professor_id))) {
                Timber.d("&& collect!!");
                return candidates.indexOf(course);
            }
        }
        return -1;
    }


}
