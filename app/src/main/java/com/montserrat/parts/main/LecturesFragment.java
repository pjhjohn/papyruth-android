package com.montserrat.parts.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.montserrat.app.R;
import com.montserrat.controller.AppConst;
import com.montserrat.parts.auth.User;
import com.montserrat.parts.navigation_drawer.NavFragment;
import com.montserrat.utils.request.ClientFragmentWithRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

public class LecturesFragment extends ClientFragmentWithRecyclerView<LecturesRecyclerAdapter, LecturesRecyclerAdapter.Holder.Data> {
    private NavFragment.OnCategoryClickListener callback;
    private long minLectureId, maxLectureId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (NavFragment.OnCategoryClickListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
        View view = super.onCreateView(inflater, container, args);

        this.swipeRefreshView.setEnabled(true);
        return view;
    }

    @Override
    protected LecturesRecyclerAdapter getAdapter (List<LecturesRecyclerAdapter.Holder.Data> items) {
        return LecturesRecyclerAdapter.newInstance(this.items, this);
    }

    @Override
    public void onRequestResponse(JSONObject response) {
        JSONArray lectures = response.optJSONArray("lectures");
        if ( lectures == null ) return;

        for (int i = 0; i < lectures.length(); i++) {
            JSONObject lecture = lectures.optJSONObject(i);
            if ( lecture == null ) continue;
            this.items.add(new LecturesRecyclerAdapter.Holder.Data(
                lecture.optString("name", "NO-NAME"),
                lecture.optString("professor", "<PROFESSOR>"),
                (float) lecture.optDouble("rating", 0.0)
            ));
        } this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshResponse(JSONObject response) {
        JSONArray lectures = response.optJSONArray("lectures");
        if ( lectures == null ) return;

        this.items.clear();
        for (int i = 0; i < lectures.length(); i++) {
            JSONObject lecture = lectures.optJSONObject(i);
            if ( lecture == null ) continue;
            this.items.add(new LecturesRecyclerAdapter.Holder.Data(
                    lecture.optString("name", "NO-NAME"),
                    lecture.optString("professor", "<PROFESSOR>"),
                    (float) lecture.optDouble("rating", 0.0)
            ));
        } this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        JSONObject params = new JSONObject();
        try {
            params.putOpt("university_id", User.getInstance().getUniversityId());
            params.putOpt("since_id", this.minLectureId);
            params.putOpt("max_id", this.minLectureId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.setParameters(params);
        this.submit();
    }

    @Override
    public void onAskMore (int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        JSONObject params = new JSONObject();
        try {
            params.putOpt("university_id", User.getInstance().getUniversityId());
            params.putOpt("since_id", this.minLectureId);
            params.putOpt("limit", 10);
        } catch (JSONException e) {
            Timber.e("JSONException Occured with possible null value UserInfo.university_id : %s", User.getInstance().getUniversityId());
        }
        this.setParameters(params);
        this.submit();
    }

    public static Fragment newInstance () {
        Fragment fragment = new LecturesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AppConst.Request.API_ROOT_URL, AppConst.API_ROOT);
        bundle.putString(AppConst.Request.API_VERSION, AppConst.API_VERSION);
        bundle.putString(AppConst.Request.ACTION, "lectures");
        bundle.putInt(AppConst.Resource.FRAGMENT, R.layout.fragment_home);
        bundle.putInt(AppConst.Resource.RECYCLER, R.id.main_recyclerview);
        bundle.putInt(AppConst.Resource.FAB, R.id.fab);
        bundle.putInt(AppConst.Resource.TOOLBAR, R.id.toolbar);
        bundle.putInt(AppConst.Resource.SWIPE_REFRESH, R.id.swipe);
        bundle.putInt(AppConst.Resource.CONTENT, R.id.content_main);
        bundle.putInt(AppConst.Resource.PROGRESS, R.id.progress_main);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void recyclerViewListClicked (View view, int position) {
        // To Lecture Page
    }

    @Override
    public RecyclerView.LayoutManager getRecyclerViewLayoutManager() {
        return new LinearLayoutManager(this.getActivity());
    }
}