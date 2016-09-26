package com.codelite.kr4k3rz.samachar.handler;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.codelite.kr4k3rz.samachar.adapter.RvAdapter;
import com.codelite.kr4k3rz.samachar.model.Entry;
import com.codelite.kr4k3rz.samachar.util.Parse;
import com.codelite.kr4k3rz.samachar.util.SnackMsg;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AsyncHelper extends AsyncTask<String, Void, Void> {

    private final SwipeRefreshLayout refreshLayout;
    private final String cacheName;
    private final Context context;
    private final RecyclerView recyclerView;
    private final View rootView;
    private int newsFeedsSize = 0;

    public AsyncHelper(View rootView, SwipeRefreshLayout refreshLayout, Context context, String cacheName, RecyclerView recyclerView) {
        this.refreshLayout = refreshLayout;
        this.context = context;
        this.cacheName = cacheName;
        this.recyclerView = recyclerView;
        this.rootView = rootView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        refreshLayout.setRefreshing(true);
    }

    @Override
    protected Void doInBackground(String... rss) {
        List<Entry> list = new ArrayList<>();
        try {
            for (String rs : rss) {
                if (!rs.equalsIgnoreCase("")) {
                    String jsonStr = new PullData().run(rs);    //loads JSON String feeds by Okhttp
                    Log.i("TAG", " : " + jsonStr);
                    JSONObject mainNode = new JSONObject(jsonStr);
                    JSONObject responseData = mainNode.getJSONObject("responseData");
                    JSONObject feeds = responseData.getJSONObject("feed");
                    JSONArray entries = feeds.getJSONArray("entries");

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Entry>>() {
                    }.getType();
                    List<Entry> posts = gson.fromJson(String.valueOf(entries), listType);
                    list.addAll(posts);
                }

            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.i("TAG", "Internet not working or failed to download / 200 error");
        }
        newsFeedsSize = Parse.filterCategories(list).get(3);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        List<Entry> feeds = Hawk.get(cacheName);
        int limitSize = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("feedsToStore", String.valueOf(20)));
        Log.i("TAG", "limitSize : " + limitSize);

        if (feeds.size() > limitSize) {
            feeds.subList(limitSize, feeds.size()).clear();
        }

        recyclerView.setAdapter(new RvAdapter(context, feeds));
        refreshLayout.setRefreshing(false);
        SnackMsg.showMsgShort(rootView, "feeds loaded " + newsFeedsSize);

    }
}