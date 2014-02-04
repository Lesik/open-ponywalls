package com.lesikapk.ponywalls;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;
import com.sothree.multiitemrowlistadapter.MultiItemRowListAdapter;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class RedditFragment extends SherlockListFragment implements OnRefreshListener {
	
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String ARG_SUBREDDIT_URL = "subreddit_url";
	private String url;
	private static RedditFragment mThis;
	private ListView list;
	private static ImageManager imageManager;
	private RequestQueue reqQueue;
	private ArrayList<RedditItem> itemArray;
	private RedditAdapter postAdapter;
	private Context mContext;
	private PullToRefreshLayout mPullToRefreshLayout;
	public static final String TAG = "RedditReader";
	public static final String URL = "URL";
	private LinearLayout errorLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_fragment, null);
		LoaderSettings settings = new SettingsBuilder()
	    	.withDisconnectOnEveryCall(true)
	    	// 50 is the percentage of the available cache to be used
	    	.withCacheManager(new LruBitmapCache(getActivity(), 50))
	    	.withConnectionTimeout(20000)
	    	.withReadTimeout(30000)
	    	.build(getActivity());
	    imageManager = new ImageManager(getActivity(), settings);
	    return view;
	}
	
	public static final ImageManager getImageManager() {
	    return imageManager;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mThis = this;
		prepareListView();
		url = getArguments().getString(ARG_SUBREDDIT_URL) + ".json";
		populateList(url);
		getView().getRootView().setBackgroundColor(getResources().getColor(R.color.gnow_bg));
		int spacing = (int)getResources().getDimension(R.dimen.spacing);
	    int itemsPerRow = getResources().getInteger(R.integer.items_per_row);
	    MultiItemRowListAdapter wrapperAdapter = new MultiItemRowListAdapter(getActivity(), postAdapter, itemsPerRow, spacing);
	    setListAdapter(wrapperAdapter);
		super.onActivityCreated(savedInstanceState);
	}
	
	private void prepareListView() {
		list = getListView();
		errorLayout = (LinearLayout)getView().findViewById(R.id.error_layout);
		checkConnectionAndShowError(list, errorLayout, null);
		list.setDivider(null);
		list.setDividerHeight(5);
		list.setPadding(5, 5, 5, 20);
		list.setClipToPadding(false);				// !important
		list.setVerticalScrollBarEnabled(false);
	}
	
	public static RedditFragment getThis() {
		return mThis;
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
		if(checkConnectionAndShowCrouton()) {
			setListAdapter(null);
			populateList(url);
			setListAdapter(postAdapter);
		}
//		checkConnectionAndShowError(list, errorLayout, null);
	}
	
	private void populateList(String urlToParse) {
		mContext = getActivity();
		reqQueue = Volley.newRequestQueue(getActivity());
		itemArray = new ArrayList<RedditItem>();
		postAdapter = new RedditAdapter(itemArray, getActivity());

		JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, urlToParse, null, 
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject arg0) {
						parseJson(arg0);
						postAdapter.notifyDataSetChanged();
					}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				showErrorCrouton(R.string.crouton_unexpected_error);
				arg0.printStackTrace();
			}
		});
		reqQueue.add(jsonReq);
		
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
//                                  itemArray.remove(postAdapter.getItem(position));
//                                	Report post code!
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
					item.url = child.optString("url");
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
					// Checks if the image is NSFW *or* if NSFW is allowed. If one of them true, display that post.
					if(!child.optBoolean("over_18") || prefs.getBoolean("nsfw_enabled", false)) {
						item.author = child.optString("author");
						item.points = child.optInt("score");
		                itemArray.add(item);
					}
					else {
//						itemArray.remove(item);
					}
				}
				else {
					//
					break;
				}
			}
		}
		catch (Exception e) {
			showErrorCrouton(R.string.crouton_unexpected_error);
			e.printStackTrace();
		}
	}

	public void showInfoCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.INFO).show();
	}
	
	public void showConfirmCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.CONFIRM).show();
	}
	
	public void showErrorCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.ALERT).show();
	}
	
	public void startDownload(String imageName, String imageUrl) {
		if(checkConnectionAndShowCrouton()) {
			if(imageUrl.lastIndexOf(".") != -1) {
				showInfoCrouton(R.string.crouton_download);
				DownloadManager mgr = (DownloadManager)getActivity().getSystemService(HomeActivity.DOWNLOAD_SERVICE);
		        String filenameArray[] = imageUrl.split("\\.");
		        String ext = filenameArray[filenameArray.length-1];
		        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(imageUrl));
				req.setTitle(getResources().getString(R.string.downloading_in_progress)).setDescription(imageName);
				req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/Pony Wallpaper", imageName + "." + ext);
				mgr.enqueue(req);
			}
			else {
				showErrorCrouton(R.string.crouton_unexpected_error);
			}
		}	
	}
	
	public int getPosForView(View v) {
		return getListView().getPositionForView(v);
	}
	
	private boolean checkConnectionAndShowCrouton() {
		ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
			return true;
		}
		else {
			showErrorCrouton(R.string.crouton_network_error);
			return false;
		}
	}
	
	private boolean checkConnectionAndShowError(ListView listView, LinearLayout errorLayout, LinearLayout loadingLayout) {
		ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
			listView.setVisibility(View.VISIBLE);
			errorLayout.setVisibility(View.GONE);
//			loadingLayout.setVisibility(View.GONE);
			return true;
		}
		else {
//			listView.setVisibility(View.GONE);
			errorLayout.setVisibility(View.VISIBLE);
//			loadingLayout.setVisibility(View.GONE);
			return false;
		}
	}
	
	public void setWallpaper(final String imageUrl) {
		if(checkConnectionAndShowCrouton()) {
			showInfoCrouton(R.string.crouton_set_wallpaper);
			try {
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	WallpaperManager wpm = WallpaperManager.getInstance(getActivity().getApplicationContext());
				    		InputStream ins;
							ins = new URL(imageUrl).openStream();
							wpm.setStream(ins);
							showConfirmCrouton(R.string.crouton_set_wallpaper_success);
				        }
				        catch (Exception e) {
				            showErrorCrouton(R.string.crouton_unexpected_error);
				            e.printStackTrace();
				        }
				    }
				});
				thread.start();
	
			}
			catch (Exception e) {
				showErrorCrouton(R.string.crouton_unexpected_error);
				e.printStackTrace();
			}
		}
	}
	
	private void showAlertDialog(String title, String message, Drawable icon) {
		new AlertDialog.Builder(getActivity().getApplicationContext())
		    .setTitle(title)
		    .setMessage(message)
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            dialog.dismiss();
		        }
		    })
		    .setIcon(icon)
		    .show();
	}
}