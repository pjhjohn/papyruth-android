package com.montserrat.app.fragment.auth;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.montserrat.app.R;
import com.montserrat.utils.support.retrofit.RetrofitApi;
import com.montserrat.utils.view.viewpager.OnPageFocus;
import com.montserrat.utils.view.viewpager.OnPageUnfocus;
import com.montserrat.utils.view.viewpager.ViewPagerController;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by SSS on 2015-07-26.
 */
public class SignUpTermFragment extends Fragment implements OnPageFocus, OnPageUnfocus {
    private ViewPagerController pagerController;

    @InjectView(R.id.agree_term) protected TextView term;
    @InjectView(R.id.confirm) protected Button confirm;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pagerController = (ViewPagerController) activity;
    }

    private CompositeSubscription subscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup_term, container, false);
        ButterKnife.inject(this, view);
        this.subscription = new CompositeSubscription();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPageFocused() {
        if(this.subscription.isUnsubscribed())
            this.subscription = new CompositeSubscription();

        this.subscription.add(
            RetrofitApi.getInstance().terms(1)
                .map(terms -> terms.term)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    term -> {
                        ViewGroup.LayoutParams param = this.term.getLayoutParams();
                        if (term != null && term.size() > 0) {
                            this.term.setText(term.get(0).body);
                            param.height = 500;
                        }else {
                            this.term.setText("");
                            param.height = 500;
                        }
                        this.term.setLayoutParams(param);
                    }, error -> {
                        Timber.d("get Term error", error);
                        error.printStackTrace();
                    }
            )
        );

        this.subscription.add(
            ViewObservable.clicks(this.confirm).subscribe(
                unused ->
                    this.pagerController.onBack()
            )
        );
    }

    @Override
    public void onPageUnfocused() {
        if(this.subscription !=null && !this.subscription.isUnsubscribed()) this.subscription.unsubscribe();
    }
}
