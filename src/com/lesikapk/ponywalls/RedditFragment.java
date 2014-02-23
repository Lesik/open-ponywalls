package com.lesikapk.ponywalls;


import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.etsy.android.grid.StaggeredGridView;
import com.lesikapk.ponywalls.library.SystemBarTintManager;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class RedditFragment extends Fragment implements OnScrollListener {
	
	// Class relative
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String ARG_SUBREDDIT_URL = "subreddit_url";
	public static final String TAG = "RedditReader";
	private static RedditFragment mThis;
	private Context mContext;
	
	// Utils
	public String url;
	private RequestQueue reqQueue;
	private ArrayList<RedditItem> itemArray;
	private RedditAdapter postAdapter;
	private static ImageManager imageManager;
	private int savedPosition;
	private int savedPositionOffset;
	private String lastPostId;
	private boolean currentlyLoading;

	
	// Views
	private StaggeredGridView posts;
	private View loadingLayout;
	private LinearLayout errorLayout;
	private ScrollView emptyLayout; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate our custom view
		View view = inflater.inflate(R.layout.activity_fragment, null);
		loadingLayout = inflater.inflate(R.layout.progress_bar, null);
		posts				= (StaggeredGridView)		view.findViewById(R.id.post);
		errorLayout 		= (LinearLayout)			view.findViewById(R.id.error_layout);
		emptyLayout			= (ScrollView)				view.findViewById(R.id.empty_layout);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// Utils
		mContext = getActivity();
		mThis = this;
		url = getArguments().getString(ARG_SUBREDDIT_URL) + ".json" + "?limit=10";
		
				
		// General UI
		getView().getRootView().setBackgroundColor(getResources().getColor(R.color.gnow_bg));
		posts.addFooterView(loadingLayout);
		
		// ImageLoader stuff
	    LoaderSettings settings = new SettingsBuilder()
	    	.withDisconnectOnEveryCall(true)
	    	// 50 is the percentage of the available cache to be used
	    	.withCacheManager(new LruBitmapCache(getActivity(), 50))
	    	.withConnectionTimeout(20000)
	    	.withReadTimeout(30000)
	    	.build(getActivity());
	    imageManager = new ImageManager(getActivity().getApplicationContext(), settings);
    	
		initiateParsing();
		populateList(url);
		parsingFinished();
	    
	    super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		posts.setSelection(savedPosition);
	}
	
	/*
	 * GridView stuff
	 */
	
	public void reloadPosts() {
		if(checkConnectionAndShowError()) {
			initiateParsing();
			populateList(url);
			parsingFinished();
		}
	}
	
	private void initiateParsing() {
		reqQueue = Volley.newRequestQueue(getActivity());
		itemArray = new ArrayList<RedditItem>();
		postAdapter = new RedditAdapter(itemArray, getActivity());
	}
	
	private void populateList(String urlToParse) {
		HomeActivity.getThis().onReloadPressed();
		showLoadingLayout();
		hideEmptyLayout();
		
		JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, urlToParse, null, new Response.Listener<JSONObject>() {
	
				@Override
				public void onResponse(JSONObject arg0) {
					parseJson(arg0);
					postAdapter.notifyDataSetChanged();
				}
				
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(final VolleyError arg0) {
				// Hide (Smooth)ProgressBar and show an error message
				arg0.printStackTrace();
				hideLoadingLayout();
				showErrorLayout();
				errorLayout.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						showStackTraceInDialog(arg0, getActivity().getApplicationContext());
					}
				});
			}
		});
		
		reqQueue.add(jsonReq);
	}

	private void parsingFinished() {
		posts.setOnScrollListener(this);
		posts.setAdapter(postAdapter);
	}
	
	public void parseJson(JSONObject jsonObject) {
		try {
			JSONObject value = jsonObject.getJSONObject("data");
			JSONArray children = value.getJSONArray("children");
			for(int i = 0; i< children.length(); i++) {
				JSONObject child = children.getJSONObject(i).getJSONObject("data");
				RedditItem item = new RedditItem();
				item.title = (String)child.opt("title");
				if(item.title != null) {
					item.url = child.optString("url");
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
					// Checks if the image is NSFW *or* if NSFW is allowed. If one of them true, display that post.
					if(!child.optBoolean("over_18") || prefs.getBoolean("nsfw_enabled", false)) {
						item.author = child.optString("author");
						item.points = child.optInt("score");
						item.commentsUrl = "https://reddit.com" + child.optString("permalink");
						itemArray.add(item);
		                
		                // Add the post ID as the last one
		                lastPostId = child.optString("name");
					}
					else {
//						itemArray.remove(item);
					}
				}
				else {
					//
					break;
				}
				currentlyLoading = false;
				hideErrorLayout();
				hideLoadingLayout();
			}
			
			// Check if no items are displayed
			if(children.length() == 0 && itemArray.isEmpty()) {
				showEmptyLayout();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			currentlyLoading = false;
//			showStackTraceInDialog(e, getActivity().getApplicationContext());
		}
	}

	/*
	 * Connection utils
	 */
	
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
	
	private boolean checkConnectionAndShowError() {
		ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
			hideErrorLayout();
			return true;
		}
		else {
			showErrorLayout();
			return false;
		}
	}
	
	/*
	 * Local listeners
	 */

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Save the current GridView state (to save the position)
		savedPosition = firstVisibleItem;
		savedPositionOffset = (posts.getChildAt(0) == null) ? 0 : posts.getChildAt(0).getTop();
		
		if(firstVisibleItem == 1) {
		}
		
		// Check if currently at the bottom of the GridView and whether currenlty loading something
	    if(firstVisibleItem + visibleItemCount == totalItemCount && !currentlyLoading) {
	    	// It is important to check if something is currently loading, otherwise this method is going to be called
	    	// multiple times in a row which results in a continuous loading of posts...which is just not what I want
	    	// and not what the user wants. Pretty cool trick. I am actually proud of me for thinking of this. :-)
	    	populateList(getArguments().getString(ARG_SUBREDDIT_URL) + ".json" + "?limit=10" + "&after=" + lastPostId);
	    	currentlyLoading = true;
	    }
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Listeners
	 */
	
	public void startDownload(String imageName, String imageUrl) {
		try {
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
		catch(Exception e) {
			e.printStackTrace();
			showStackTraceInDialog(e, getActivity());
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
	
	public void shareWallpaper(final String imageUrl) {
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUrl));
		shareIntent.setType("image/*");
		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.dialog_share_via)));
	}
	
	public void onThreeDotMenuClicked(final String commentsUrl, final String imageUrl, final String authorName) {
		View threeDotMenuView = getActivity().getLayoutInflater().inflate(R.layout.dialog_list, null);
		ListView threeDotMenuList = (ListView)threeDotMenuView.findViewById(android.R.id.list);
		threeDotMenuList.setPadding(5, 0, 5, 0);
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setView(threeDotMenuView);
        alertDialog.setTitle("Select action");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.threedotmenu_items));
        threeDotMenuList.setAdapter(adapter);
        final AlertDialog dialog = alertDialog.create();
        threeDotMenuList.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		Intent i;
        		switch (arg2) {
        		case 0:
        			// This is the first action - share the post/comments link
        			i = new Intent();
					i.setAction(Intent.ACTION_SEND);
					i.putExtra(Intent.EXTRA_TEXT, commentsUrl);
					i.setType("text/plain");
					startActivity(Intent.createChooser(i, getResources().getString(R.string.dialog_share_via)));
					break;
				case 1:
					// This is the second action - share the image link
					i = new Intent();
					i.setAction(Intent.ACTION_SEND);
					i.putExtra(Intent.EXTRA_TEXT, imageUrl);
					i.setType("text/plain");
					startActivity(Intent.createChooser(i, getResources().getString(R.string.dialog_share_via)));
					break;
				case 2:
					i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(commentsUrl));
					startActivity(i);
					break;
				case 3:
					i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("https://reddit.com/u/" + authorName));
					startActivity(i);
					break;
				}
        		dialog.cancel();
        	}
		});
        dialog.show();
	}
	
	/*
	 * Utils
	 */
	
	public static RedditFragment getThis() {
		return mThis;
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
	
	private void showErrorLayout() {
		errorLayout.setVisibility(View.VISIBLE);
	}

	private void hideErrorLayout() {
		errorLayout.setVisibility(View.GONE);
	}
	
	private void showEmptyLayout() {
		emptyLayout.setVisibility(View.VISIBLE);
	}

	private void hideEmptyLayout() {
		emptyLayout.setVisibility(View.GONE);
	}
	
	private void showLoadingLayout() {
		loadingLayout.findViewById(R.id.smoothprogressbar).setVisibility(View.VISIBLE);
	}

	private void hideLoadingLayout() {
		loadingLayout.findViewById(R.id.smoothprogressbar).setVisibility(View.GONE);
	}
	
	public void showStackTraceInDialog(Exception e, Context context) {
	    // Converts the stack trace into a string
	    StringWriter errors = new StringWriter();
	    e.printStackTrace(new PrintWriter(errors));

	    // Show the stack trace on Logcat
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity().getApplicationContext());
	    // Add the buttons
	    builder.setTitle(R.string.error_title);
	    builder.setMessage(getResources().getString(R.string.error_descr_dialog) + errors.toString());
	    // Create the AlertDialog
	    AlertDialog dialog = builder.create();
	    // Show the dialog
	    dialog.show();
	    // Set other dialog properties
	    TextView stackTrace = (TextView)dialog.findViewById(android.R.id.message);
	    stackTrace.setTextSize(7);
	    stackTrace.setTypeface(Typeface.MONOSPACE);
    }
	
	/* For displaying the image using the ImageManager */
	public static final ImageManager getImageManager() {
	    return imageManager;
	}
	
	public int getPosForView(View v) {
		// It is really cool and makes me so happy that this method works with the MultiColumnListView by GDG-Korea! I love you guys! 
		return posts.getPositionForView(v);
	}
	
	public boolean isCurrentlyLoading() {
		return currentlyLoading;
	}

}