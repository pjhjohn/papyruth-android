package com.papyruth.support.utility.search;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.papyruth.android.AppConst;
import com.papyruth.android.AppManager;
import com.papyruth.android.R;
import com.papyruth.android.model.CandidateData;
import com.papyruth.android.model.HistoryData;
import com.papyruth.android.recyclerview.adapter.AutoCompleteAdapter;
import com.papyruth.support.opensource.materialprogressbar.MaterialProgressBar;
import com.papyruth.support.opensource.picasso.ColorFilterTransformation;
import com.papyruth.support.utility.helper.AnimatorHelper;
import com.papyruth.support.utility.recyclerview.RecyclerViewItemObjectClickListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.android.widget.WidgetObservable;
import rx.subscriptions.CompositeSubscription;

public class SearchToolbar implements RecyclerViewItemObjectClickListener {
    private static SearchToolbar instance;
    private SearchToolbar() {}
    public static synchronized SearchToolbar getInstance() {
        if(SearchToolbar.instance == null) return SearchToolbar.instance = new SearchToolbar();
        return SearchToolbar.instance;
    }

    @Bind(R.id.search_toolbar_root)                   protected LinearLayout mRootView;
    @Bind(R.id.search_toolbar_back_icon)              protected ImageView mBackIcon;
    @Bind(R.id.search_toolbar_material_progressbar)   protected MaterialProgressBar mMaterialProgressBar;
    @Bind(R.id.search_toolbar_query_text)             protected EditText mQueryText;
    @Bind(R.id.search_toolbar_query_clear_icon)       protected ImageView mQueryClearIcon;
    @Bind(R.id.search_toolbar_query_result)           protected RecyclerView mQueryResult;

    private RecyclerViewItemObjectClickListener mDefaultRecyclerViewItemObjectClickListener;
    private RecyclerViewItemObjectClickListener mRecyclerViewItemObjectClickListener;
    private OnVisibilityChangedListener mOnVisibilityChangedListener;
    private OnSearchByQueryListener mDefaultOnSearchByQueryListener;
    private OnSearchByQueryListener mOnSearchByQueryListener;
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private CompositeSubscription mCompositeSubscription;
    private Context mContext;
    private Resources mResources;

    private static final long THROTTLE_MILLISECONDS = 600;

    public void init(Context context, ViewGroup root, RecyclerViewItemObjectClickListener defaultRecyclerViewItemObjectClickListener, OnSearchByQueryListener searchByQueryListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar_search, root, true);
        ButterKnife.bind(this, view);
        mContext = context;
        mResources = context.getResources();
        mDefaultRecyclerViewItemObjectClickListener = defaultRecyclerViewItemObjectClickListener;
        mDefaultOnSearchByQueryListener = searchByQueryListener;
        if(mCompositeSubscription == null || mCompositeSubscription.isUnsubscribed()) mCompositeSubscription = new CompositeSubscription();

        Picasso.with(mContext).load(R.drawable.ic_clear_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mQueryClearIcon);
        Picasso.with(mContext).load(R.drawable.ic_back_24dp).transform(new ColorFilterTransformation(mResources.getColor(R.color.icon_material))).into(mBackIcon);

        mRootView.setAlpha(0);
        mRootView.setVisibility(View.GONE);
        mBackIcon.setVisibility(View.VISIBLE);
        mMaterialProgressBar.setVisibility(View.GONE);
        mQueryClearIcon.setVisibility(View.GONE);
        mQueryResult.setLayoutManager(new LinearLayoutManager(context));
        mAutoCompleteAdapter = new AutoCompleteAdapter(mContext, mBackIcon, mMaterialProgressBar, this);
        mQueryResult.setAdapter(mAutoCompleteAdapter);
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
                    if (isActionDown) {
                        if(mAnimator != null && mAnimator.isRunning() && mRootView.getVisibility() == View.VISIBLE){
                            mAnimator.cancel();
                        }
                        hide();
                    }
                    isActionDown = false;
                    return true;
                }
                return false;
            }
        });

        mCompositeSubscription.add(WidgetObservable.text(mQueryText)
            .map(event -> event.text().toString())
            .observeOn(AndroidSchedulers.mainThread())
            .map(query -> {
                AnimatorSet animators = new AnimatorSet();
                if (query.isEmpty()) {
                    animators.playTogether(
                            AnimatorHelper.FADE_OUT(mQueryClearIcon),
                            AnimatorHelper.FADE_OUT(mMaterialProgressBar),
                            AnimatorHelper.FADE_IN(mBackIcon)
                    );
                    mAutoCompleteAdapter.history();
                } else animators.play(AnimatorHelper.FADE_IN(mQueryClearIcon));
                animators.start();
                return query;
            })
            .throttleLast(THROTTLE_MILLISECONDS, TimeUnit.MILLISECONDS)
            .filter(query -> !query.isEmpty())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::searchAutocomplete, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(ViewObservable.clicks(mQueryClearIcon)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(event -> {
                mQueryText.getText().clear();
                mAutoCompleteAdapter.history();
            }, Throwable::printStackTrace)
        );

        mCompositeSubscription.add(ViewObservable.clicks(mBackIcon)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(event -> {
                if(mAnimator != null && mAnimator.isRunning()) mAnimator.cancel();
                hide();
            }, Throwable::printStackTrace)
        );

        mQueryText.setOnKeyListener((v, keycode, e) -> {
            if (e.getAction() == KeyEvent.ACTION_DOWN && (keycode == KeyEvent.KEYCODE_ENTER || keycode == KeyEvent.KEYCODE_SEARCH)) {
                setSelectedQuery(mQueryText.getText().toString());
                if (mOnSearchByQueryListener != null) mOnSearchByQueryListener.onSearchByQuery();
                else mDefaultOnSearchByQueryListener.onSearchByQuery();
                hide();
                return true;
            }
            return false;
        });
    }

    private void searchAutocomplete(String query) {
        mAutoCompleteAdapter.search(query);
    }

    @Override
    public void onRecyclerViewItemObjectClick(View view, Object object) {
        if(object instanceof CandidateData) {
            SearchToolbar.getInstance().setSelectedCandidate(((CandidateData) object));
            SearchToolbar.getInstance().addToHistory(SearchToolbar.getInstance().getSelectedCandidate());
        }
        if(mRecyclerViewItemObjectClickListener != null) mRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, object);
        else mDefaultRecyclerViewItemObjectClickListener.onRecyclerViewItemObjectClick(view, object);
        hide();
    }

    public boolean back() {
        if(mRootView.getVisibility() != View.VISIBLE) return false;
        if(mAnimator != null && mAnimator.isRunning()) mAnimator.cancel();
        hide();
        return true;
    }


    private boolean mSearchToolbarOpened = false;
    public boolean isOpened() {
        return mSearchToolbarOpened;
    }
    public void showSoftKeyboard() {
        mQueryText.requestFocus();
        ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mQueryText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setVisibility(boolean visible) {
        if(mRootView != null) mRootView.setVisibility(visible? View.VISIBLE : View.GONE);
        if(mOnVisibilityChangedListener != null) mOnVisibilityChangedListener.onVisibilityChanged(visible);
    }

    private ValueAnimator mAnimator;
    public void show() {
        mAnimator = AnimatorHelper.FADE_IN(mRootView);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            boolean canceled = false;

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                canceled = true;
                hide();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!canceled) {
                    mQueryText.setFocusable(true);
                    mRootView.setAlpha(1.0f);
                    showSoftKeyboard();
                    mSearchToolbarOpened = true;
                } else canceled = false;
            }
        });
        mAnimator.start();

        mAutoCompleteAdapter.history();
    }
    public void hide() {
        mAnimator = AnimatorHelper.FADE_OUT(mRootView);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mQueryText.clearFocus();
                mQueryText.getText().clear();
                mSearchToolbarOpened = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(false);
                mQueryText.getText().clear();
                ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mQueryText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        mAnimator.start();
        this.mAutoCompleteAdapter.clear();
    }

    private CandidateData mSelectedCandidate;
    private String mSelectedQuery;
    public CandidateData getSelectedCandidate() {
        return mSelectedCandidate;
    }
    public SearchToolbar setSelectedCandidate(CandidateData candidateData) {
        mSelectedCandidate = candidateData;
        mSelectedQuery = null;
        return this;
    }
    public String getSelectedQuery() {
        return mSelectedQuery;
    }
    public SearchToolbar setSelectedQuery(String query) {
        mSelectedQuery = query;
        mSelectedCandidate = new CandidateData();
        return this;
    }
    public boolean isReadyToSearch(){
        return (mSelectedCandidate != null && ( mSelectedCandidate.lecture_id != null || mSelectedCandidate.professor_id != null))|| mSelectedQuery != null;
    }

    private static final int HISTORY_SIZE = 10;
    public boolean addToHistory(CandidateData newCandidate) {
        if(newCandidate.lecture_id == null && newCandidate.professor_id == null) return false;
        List<CandidateData> candidates;
        if(AppManager.getInstance().contains(AppConst.Preference.HISTORY)) {
            candidates = ((HistoryData) AppManager.getInstance().getStringParsed(
                AppConst.Preference.HISTORY,
                HistoryData.class
            )).candidates;
        } else candidates = new ArrayList<>();

        final int iOldCandidate = candidates.indexOf(newCandidate); // Candidate equal check rule is overridden for this
        if(iOldCandidate >= 0) candidates.remove(iOldCandidate);
        while(candidates.size() >= HISTORY_SIZE) candidates.remove(candidates.size() - 1);
        candidates.add(0, newCandidate);

        AppManager.getInstance().putStringParsed(AppConst.Preference.HISTORY, new HistoryData(candidates));
        return true;
    }

    /* Listener Setter */
    public SearchToolbar setItemObjectClickListener(RecyclerViewItemObjectClickListener listener) {
        mRecyclerViewItemObjectClickListener = listener;
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
