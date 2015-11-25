package com.papyruth.utils.view.search;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.Candidate;
import com.papyruth.android.model.HistoryData;
import com.papyruth.android.model.unique.User;
import com.papyruth.android.recyclerview.adapter.AutoCompleteAdapter;
import com.papyruth.utils.support.picasso.ColorFilterTransformation;
import com.papyruth.utils.support.retrofit.apis.Api;
import com.papyruth.utils.view.recycler.RecyclerViewItemClickListener;
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

/**
 * Created by SSS on 2015-11-06.
 */
public class SearchToolbar implements RecyclerViewItemClickListener {
    private static SearchToolbar instance;
    private SearchToolbar() {}
    public static synchronized SearchToolbar getInstance() {
        if(SearchToolbar.instance == null) return SearchToolbar.instance = new SearchToolbar();
        return SearchToolbar.instance;
    }

    @InjectView(R.id.search_toolbar_root)               protected LinearLayout mRootView;
    @InjectView(R.id.search_toolbar_back_icon)          protected ImageView mBackIcon;
    @InjectView(R.id.search_toolbar_query_text)         protected PreImeEditText mQueryText;
    @InjectView(R.id.search_toolbar_query_clear_icon)   protected ImageView mQueryClearIcon;
    @InjectView(R.id.search_toolbar_query_result)       protected RecyclerView mQueryResult;

    private RecyclerViewItemClickListener mDefaultRecyclerViewItemClickListener;
    private RecyclerViewItemClickListener mRecyclerViewItemClickListener;
    private OnVisibilityChangedListener mOnVisibilityChangedListener;
    private OnSearchByQueryListener mOnSearchByQueryListener;
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private CompositeSubscription mCompositeSubscription;
    private Context mContext;
    private Resources mResources;
    private List<Candidate> mCandidates;

    public void init(Context context, ViewGroup root, RecyclerViewItemClickListener defaultRecyclerViewItemClickListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_search, root, true);
        ButterKnife.inject(this, view);
        mContext = context;
        mResources = context.getResources();
        mDefaultRecyclerViewItemClickListener = defaultRecyclerViewItemClickListener;
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();
        if(mCandidates == null) mCandidates = new ArrayList<>();

        Picasso.with(mContext).load(R.drawable.ic_light_clear).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mQueryClearIcon);
        Picasso.with(mContext).load(R.drawable.ic_light_back).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mBackIcon);

        mQueryText.setPreImeListener(() -> {
            if (root.getVisibility() == View.VISIBLE) hide();
        });
        mQueryClearIcon.setVisibility(View.GONE);
        mQueryResult.setLayoutManager(new LinearLayoutManager(context));
        mQueryResult.setAdapter(getAdapter());
        mQueryResult.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            boolean isActionDown = false;
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerview, MotionEvent event) {
                View childView = recyclerview.findChildViewUnder(event.getX(), event.getY());
                if (childView != null) {
                    isActionDown = false;
                    return super.onInterceptTouchEvent(recyclerview, event);
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    isActionDown = true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (isActionDown) hide();
                    isActionDown = false;
                    return true;
                }
                return false;
            }
        });
        if(root.getVisibility() == View.VISIBLE) hide();
        bindEvents();
    }

    private static final long DEBOUNCE_MILLISECONDS = 400;
    private void bindEvents() {
        mCompositeSubscription.add(WidgetObservable.text(mQueryText)
            .doOnNext(event -> {
                if (event.text().length() > 0) mQueryClearIcon.setVisibility(View.VISIBLE);
                else {
                    mQueryClearIcon.setVisibility(View.GONE);
                    mAutoCompleteAdapter.setIsHistory(true);
                    notifyAutoCompleteDataChanged(getHistory());
                }
            })
            .debounce(DEBOUNCE_MILLISECONDS, TimeUnit.MILLISECONDS)
            .filter(event -> event.text().toString().length() > 0)
            .map(event -> event.text().toString())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(this::searchAutocomplete, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(ViewObservable.clicks(mQueryClearIcon)
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(event -> {
                mQueryText.getText().clear();
                mAutoCompleteAdapter.setIsHistory(true);
                notifyAutoCompleteDataChanged(getHistory());
            }, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(ViewObservable.clicks(mBackIcon)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(event ->hide(), Throwable::printStackTrace)
        );

        mQueryText.setOnKeyListener((v, keycode, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN && (keycode == KeyEvent.KEYCODE_ENTER || keycode == KeyEvent.KEYCODE_SEARCH)) {
                setSelectedQuery(mQueryText.getText().toString());
                if (mOnSearchByQueryListener != null) mOnSearchByQueryListener.onSearchByQuery();
                hide();
                return true;
            }
            return false;
        });
    }

    private void searchAutocomplete(String query) {
        Api.papyruth().search_autocomplete(
            User.getInstance().getAccessToken(),
            User.getInstance().getUniversityId(),
            query
        )
        .map(response -> response.candidates)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(candidates -> {
            mAutoCompleteAdapter.setIsHistory(false);
            notifyAutoCompleteDataChanged(candidates);
        }, Throwable::printStackTrace);
    }

    @Override
    public void onRecyclerViewItemClick(View view, int position) {
        if(mRecyclerViewItemClickListener != null) mRecyclerViewItemClickListener.onRecyclerViewItemClick(view, position);
        else mDefaultRecyclerViewItemClickListener.onRecyclerViewItemClick(view, position);
        hide();
    }

    private void notifyAutoCompleteDataChanged(List<Candidate> candidates) {
        mCandidates.clear();
        mCandidates.addAll(candidates);
        mAutoCompleteAdapter.notifyDataSetChanged();
    }

    public boolean back() {
        if(mRootView.getVisibility() != View.VISIBLE) return false;
        hide();
        return true;
    }
    public SearchToolbar show() {
        mRootView.setVisibility(View.VISIBLE);
        mQueryText.requestFocus();
        if(mQueryText.requestFocus()) ((InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mQueryText, InputMethodManager.SHOW_IMPLICIT);
        mAutoCompleteAdapter.setIsHistory(true);
        notifyAutoCompleteDataChanged(getHistory());
        if(mOnVisibilityChangedListener != null) mOnVisibilityChangedListener.onVisibilityChanged(true);
        return this;
    }
    public SearchToolbar hide() {
        mRootView.setVisibility(View.GONE);
        mQueryText.clearFocus();
        mQueryText.getText().clear();
        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mQueryText.getWindowToken(), 2);
        notifyAutoCompleteDataChanged(new ArrayList<>());
        if(mOnVisibilityChangedListener != null) mOnVisibilityChangedListener.onVisibilityChanged(false);
        return this;
    }

    public AutoCompleteAdapter getAdapter() {
        if(mAutoCompleteAdapter == null) mAutoCompleteAdapter = new AutoCompleteAdapter(mCandidates, this);
        return mAutoCompleteAdapter;
    }
    public List<Candidate> getCandidates() {
        return mCandidates;
    }

    private Candidate mSelectedCandidate;
    private String mSelectedQuery;
    public Candidate getSelectedCandidate() {
        return mSelectedCandidate;
    }
    public SearchToolbar setSelectedCandidate(int position) {
        mSelectedCandidate = mCandidates.get(position);
        mSelectedQuery = null;
        return this;
    }
    public String getSelectedQuery() {
        return mSelectedQuery;
    }
    public SearchToolbar setSelectedQuery(String query) {
        mSelectedQuery = query;
        mSelectedCandidate = new Candidate();
        return this;
    }
    public int getMarginTop() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mRootView.getLayoutParams();
        return params.topMargin;
    }
    public SearchToolbar setMarginTop(int pixels) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mRootView.getLayoutParams();
        params.topMargin = pixels;
        mRootView.setLayoutParams(params);
        return this;
    }

    public List<Candidate> getHistory() {
        List<Candidate> courses = new ArrayList<>();
        if(AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
            courses = ((HistoryData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                HistoryData.class
            )).candidates;
        } // TODO : Otherwise, Inform history is empty whenever history is empty.
        return courses;
    }

    private static final int HISTORY_SIZE = 10;
    public boolean addToHistory(Candidate newCandidate) {
        if(newCandidate.lecture_id == null && newCandidate.professor_id == null) return false;
        HistoryData history = new HistoryData();
        List<Candidate> candidates;

        if(AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
            candidates = ((HistoryData)AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                HistoryData.class
            )).candidates;
        } else candidates = new ArrayList<>();

        final int iOldCandidate = candidates.indexOf(newCandidate); // Candidate equal check rule is overrided for this
        if(iOldCandidate >= 0) {
            candidates.remove(iOldCandidate);
            candidates.add(0, newCandidate);
        } else if (candidates.size() >= HISTORY_SIZE) {
            while(candidates.size() >= HISTORY_SIZE) candidates.remove(candidates.size() - 1);
            candidates.add(0, newCandidate);
        } else candidates.add(0, newCandidate);
        history.candidates.clear();
        history.candidates.addAll(candidates);
        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, history);
        return true;
    }

    /* Listener Setter */
    public SearchToolbar setItemClickListener(RecyclerViewItemClickListener listener) {
        mRecyclerViewItemClickListener = listener;
        return this;
    }
    public SearchToolbar setOnVisibilityChangedListener(OnVisibilityChangedListener listener) {
        mOnVisibilityChangedListener = listener;
        return this;
    }
    public SearchToolbar setOnSearchByQueryListener(OnSearchByQueryListener listener) {
        mOnSearchByQueryListener = listener;
        return this;
    }

    /* Nested Interfaces */
    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(boolean visible);
    }
    public interface OnSearchByQueryListener {
        void onSearchByQuery();
    }
}
