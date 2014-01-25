package com.lesikapk.ponywalls;


import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class RedditFragment extends SherlockListFragment implements OnRefreshListener, OnItemClickListener {
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String ARG_SUBREDDIT_URL = "subreddit_url";
	private ListView list;
	private RequestQueue reqQueue;
	private String url = "http://www.reddit.com/r/ponywalls/.json";
	private ArrayList<RedditItem> itemArray;
	private RedditAdapter postAdapter;
	private Context mContext;
	private ProgressDialog loadingProgress;
	private int progressStatus;
	private PullToRefreshLayout mPullToRefreshLayout;
	public static final String TAG = "RedditReader";
	public static final String URL = "URL";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		postAdapter = new RedditAdapter(new ArrayList<RedditItem>(), getActivity());
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		showProgressDialog();
		list = getListView();
		list.setDivider(null);
		list.setDividerHeight(20);
		list.setPadding(20, 20, 20, 20);
		list.setClipToPadding(false);				// !important
		list.setVerticalScrollBarEnabled(false);
		getView().getRootView().setBackgroundColor(getResources().getColor(R.color.gnow_bg));
		populateList();
		setListAdapter(postAdapter);
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    // This is the View which is created by ListFragment
        ViewGroup viewGroup = (ViewGroup) view;
        // We need to create a PullToRefreshLayout manually
        mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
                .insertLayoutInto(viewGroup)
                // We need to mark the ListView and it's Empty View as pullable
                // This is because they are not dirent children of the ViewGroup
                .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                // We can now complete the setup as desired
                .listener(this)
                .options(Options.create()
                        .scrollDistance(.5f)
                        .build())
                .setup(mPullToRefreshLayout);
	}						
	
	@Override
	public void onRefreshStarted(View view) {
		new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
            	getActivity().runOnUiThread(new Runnable() {
            		@Override
            		public void run() {
            			setListAdapter(null);
            			RedditAdapter.i = 0;
            			populateList();
            			setListAdapter(postAdapter);
            		}
            	});
            	return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                new Handler().postDelayed(new Runnable() {
                	@Override
                	public void run() {
                		mPullToRefreshLayout.setRefreshComplete();
                	}
                }, 4000);
            }
		}.execute();
	}
	
	private void showProgressDialog() {
		progressStatus = 0;
		loadingProgress = new ProgressDialog(getActivity());
		loadingProgress.setCancelable(false);
		loadingProgress.setMessage("Loading wallpapers...");
		loadingProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		loadingProgress.setProgress(progressStatus);
		loadingProgress.setMax(25);
		loadingProgress.show();
	}
	
	private void populateList() {
//		url = getArguments().getString(ARG_SUBREDDIT_URL);
//		url = url+".json";
		mContext = getActivity();
		reqQueue = Volley.newRequestQueue(getActivity());
		itemArray = new ArrayList<RedditItem>();
		postAdapter = new RedditAdapter(itemArray, getActivity());

		JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, url, null, 
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject arg0) {
						parseJson(arg0);
						postAdapter.notifyDataSetChanged();
					}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				//TODO LOL
			}
		});
		reqQueue.add(jsonReq);
		
		getListView().setOnItemClickListener(this);
	}
	
	
	
	public void parseJson(JSONObject jsonObject) {
		try {
			JSONObject value = jsonObject.getJSONObject("data");
			JSONArray children = value.getJSONArray("children");
			for(int i = 0; i< children.length(); i++) {
				JSONObject child = children.getJSONObject(i).getJSONObject("data");
				RedditItem item = new RedditItem();
				item.title = (String) child.opt("title");
				if(item.title != null) {
					item.nsfw = (Boolean) child.optBoolean("over_18");
					if(!item.nsfw) {
						item.url = child.optString("url");
						item.points = child.optInt("score");
						item.subreddit = child.optString("subreddit");
						item.thumbnail = child.optString("thumbnail");
		                itemArray.add(item);
					}
				}
				progressStatus++;
				loadingProgress.setProgress(progressStatus);
			}
			loadingProgress.dismiss();

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent = new Intent(mContext, DetailView.class);
		intent.putExtra(URL, itemArray.get(arg2).url);
		startActivity(intent);		
		
	}
}
