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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.sothree.multiitemrowlistadapter.MultiItemRowListAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class RedditFragment extends SherlockListFragment implements OnRefreshListener, OnItemClickListener {
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String ARG_SUBREDDIT_URL = "subreddit_url";
	private String url;
	private static RedditFragment mThis;
	private ListView list;
	private RequestQueue reqQueue;
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
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mThis = this;
//		showProgressDialog();
		list = getListView();
		list.setDivider(null);
		list.setDividerHeight(5);
		list.setPadding(5, 5, 5, 20);
		list.setClipToPadding(false);				// !important
		list.setVerticalScrollBarEnabled(false);
		getView().getRootView().setBackgroundColor(getResources().getColor(R.color.gnow_bg));
		populateList();
		int spacing = (int)getResources().getDimension(R.dimen.spacing);
	    int itemsPerRow = getResources().getInteger(R.integer.items_per_row);
	    MultiItemRowListAdapter wrapperAdapter = new MultiItemRowListAdapter(getActivity(), postAdapter, itemsPerRow, spacing);
	    setListAdapter(wrapperAdapter);
//		setListAdapter(postAdapter);
		super.onActivityCreated(savedInstanceState);
	}
	
	public static RedditFragment getThis() {
		return mThis;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		url = getArguments().getString(ARG_SUBREDDIT_URL) + ".json";
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
            			reloadPosts();
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
	
	public void reloadPosts() {
//		setListAdapter(null);
//		RedditAdapter.i = 0;
//		populateList();
//		setListAdapter(postAdapter);
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
		Crouton.makeText(getActivity(), "Loading...", Style.INFO).show();
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
		
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        getListView(),
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    itemArray.remove(postAdapter.getItem(position));
                                }
                                postAdapter.notifyDataSetChanged();
                            }
                        });
        getListView().setOnTouchListener(touchListener);
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
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
					item.nsfw = (Boolean) child.optBoolean("over_18");
					if(!item.nsfw || prefs.getBoolean("nsfw_enabled", false)) {
						item.url = child.optString("url");
						item.points = child.optInt("score");
						item.subreddit = child.optString("subreddit");
						item.thumbnail = child.optString("thumbnail");
		                itemArray.add(item);
					}
				}
				else {
					//
					break;
				}
				progressStatus++;
//				loadingProgress.setProgress(progressStatus);
			}
//			loadingProgress.dismiss();

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
	
//	private void showCrouton() {
//		LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = mInflater.inflate(R.layout.carddemo_extras_crouton_card, null);
//		
//		CardView cardView= (CardView)view.findViewById(R.id.carddemo_card_crouton_id);
//		
//		Card card = new Card(getActivity());
//		card.setTitle("Crouton Card");
//		card.setBackgroundResourceId(R.color.demoextra_card_background_color2);
//		
//		CardThumbnail thumb = new CardThumbnail(getActivity());
//		thumb.setDrawableResource(R.drawable.ic_action_bulb);
//		card.addCardThumbnail(thumb);
//		
//		cardView.setCard(card);
//		
//		final Crouton crouton;
//		crouton = Crouton.make(getActivity(), view);
//		crouton.show();
//	}
}
