package com.codelite.kr4k3rz.samachar.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codelite.kr4k3rz.samachar.R;
import com.codelite.kr4k3rz.samachar.adapter.RvAdapter;
import com.codelite.kr4k3rz.samachar.handler.AsyncHelper;
import com.codelite.kr4k3rz.samachar.model.Entry;
import com.codelite.kr4k3rz.samachar.model.WhichCategory;
import com.codelite.kr4k3rz.samachar.util.CheckInternet;
import com.codelite.kr4k3rz.samachar.util.FeedLists;
import com.codelite.kr4k3rz.samachar.util.SnackMsg;
import com.orhanobut.hawk.Hawk;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AllFeedFrag extends Fragment {

    private static final String CACHE_NAME = "AllFeeds";
    private static final String TAG = BreakingNews.class.getSimpleName();
    private View rootView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public AllFeedFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_all_feed, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_All_Feeds);
        recyclerView.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());/**/
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_AllFeeds);
        initSwipeRefresh();
        return rootView;
    }

    private void initSwipeRefresh() {
        mSwipeRefreshLayout.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         Log.i(TAG, "SwipeRefresh post()");
                                         if (!Hawk.contains(CACHE_NAME) && CheckInternet.isNetworkAvailable(getContext())) {
                                             String[] rss = FeedLists.getFeedListCached(0);
                                             new AsyncHelper(rootView, mSwipeRefreshLayout, getContext(), CACHE_NAME, recyclerView, WhichCategory.BREAKING.ordinal()).execute(rss);
                                         } else {
                                             mSwipeRefreshLayout.setRefreshing(true);
                                             List<Entry> list = Hawk.get(CACHE_NAME);
                                             if (!Hawk.contains(CACHE_NAME)) {
                                                 SnackMsg.showMsgShort(rootView, "connect to internet");
                                             } else
                                                 recyclerView.setAdapter(new RvAdapter(getContext(), list));
                                             mSwipeRefreshLayout.setRefreshing(false);
                                         }

                                     }
                                 }
        );

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, " SwipeRefresh()  ");
                if (CheckInternet.isNetworkAvailable(getContext())) {
                    String[] rss_new = FeedLists.getFeedListLatest(0);
                    new AsyncHelper(rootView, mSwipeRefreshLayout, getContext(), CACHE_NAME, recyclerView, WhichCategory.BREAKING.ordinal()).execute(rss_new);
                } else {
                    SnackMsg.showMsgShort(rootView, "Couldn't connect to internet");
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

    }
}
