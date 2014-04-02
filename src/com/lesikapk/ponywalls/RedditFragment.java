package com.lesikapk.ponywalls;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RedditFragment extends ListFragment implements OnScrollListener {
	
	// Utils
	private static RedditFragment mThis;
	public static final String ARG_SUBREDDIT_URL = "subreddit_url";
	private ClipboardManager clipboardManager;
	private static ImageManager imageManager;
	
	// Parsing and displaying
	private ListView list;
	private ArrayList<RedditItem> itemArray;
	private String lastPostId;							// So I can parse everything after the last post
	private boolean currentlyLoading = true;			// So that posts are not being loaded again when they're already loading
	private boolean enoughLoadingForToday = false;
	private RequestQueue reqQueue;
	private RedditAdapter postAdapter;
	private View progressBarHolder;
	private SmoothProgressBar progressBar;
	
	private ProgressDialog loadingProgress;
	
	private static final Configuration INFINITE = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		progressBarHolder = inflater.inflate(R.layout.progress_bar, null);
		progressBar = (SmoothProgressBar)progressBarHolder.findViewById(R.id.smoothprogressbar);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mThis = this;
		list = getListView();
		clipboardManager = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		if(!prefs.getBoolean("dark_mode_enabled", false))
			getView().setBackgroundColor(getResources().getColor(R.color.activity_background_holo_dark));
		initializeListView();
		initializeImageManager();
		initializeProgressBar();
		reqQueue = Volley.newRequestQueue(getActivity());
		itemArray = new ArrayList<RedditItem>();
		postAdapter = new RedditAdapter(itemArray, getActivity());
		setListAdapter(postAdapter);
		downloadJson(getArguments().getString(ARG_SUBREDDIT_URL) + ".json" + "?limit=10", true);
		
		super.onActivityCreated(savedInstanceState);
	}
	
	private void initializeListView() {
		list.setDivider(null);
		list.setDividerHeight(20);
		list.setPadding(20, 20, 20, 20);
		list.setClipToPadding(false);				// !important
		list.setVerticalScrollBarEnabled(true);
		list.addFooterView(progressBarHolder);
		list.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		list.setOnScrollListener(this);
	}
	
	private void initializeImageManager() {
		LoaderSettings settings = new SettingsBuilder()
	    	.withDisconnectOnEveryCall(true)
	    	// 50 is the percentage of the available cache to be used
	    	.withCacheManager(new LruBitmapCache(getActivity(), 50))
	    	.withConnectionTimeout(20000)
	    	.withReadTimeout(30000)
	    	.build(getActivity());
		imageManager = new ImageManager(getActivity().getApplicationContext(), settings);
	}
	
	private void initializeProgressBar() {
		ProgressDialog loadingProgress = new ProgressDialog(getActivity());
		loadingProgress.setCancelable(false);
		loadingProgress.setMessage("Downloading...");
		loadingProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		loadingProgress.setMax(100);
	}
	
	public void reloadPosts() {
		setListShown(false);
		postAdapter.clearAllItems();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 downloadJson(getArguments().getString(ARG_SUBREDDIT_URL) + ".json" + "?limit=10", false); 
	         } 
	    }, 1000); 
	}

	private void downloadJson(String urlToParse, boolean shouldAnimate) {
		if(shouldAnimate)
			setListShown(false);
		JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET, urlToParse, null, new Response.Listener<JSONObject>() {
			
			@Override
			public void onResponse(JSONObject arg0) { 
				try {
					setListShown(true);
					parseJson(arg0);
					postAdapter.notifyDataSetChanged();
					currentlyLoading = false;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}, new Response.ErrorListener() {
	
			@Override
			public void onErrorResponse(final VolleyError arg0) {
				try {
					errorNetwork(arg0);
//					setListShown(true);
					currentlyLoading = false;
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		reqQueue.add(jsonReq);
	}
	
	/*
	 * Parsing and displaying
	 */
	
	private void parseJson(JSONObject jsonObject) {
		try {
			JSONObject value = jsonObject.getJSONObject("data");
			JSONArray children = value.getJSONArray("children");
			if(children.length() == 0 || children == null || value.isNull("data")) {
				enoughLoadingForToday = true;
				System.out.println("izempty");
			}
			else {
				enoughLoadingForToday = false;
			}
			for(int i = 0; i< children.length(); i++) {
				JSONObject child = children.getJSONObject(i).getJSONObject("data");
				RedditItem item = new RedditItem();
				item.title = (String)child.opt("title");
				item.url = (String)child.opt("url");
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
			}
			// Check if no items are displayed
			if(children.length() == 0 && itemArray.isEmpty()) {
//				showEmptyLayout();
			}
		}
		catch (Exception e) {
			errorParsing(e);
		}
	}
	
	/*
	 * Local listeners
	 */
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Save the current GridView state (to save the position)
//		savedPosition = firstVisibleItem;
//		savedPositionOffset = (posts.getChildAt(0) == null) ? 0 : posts.getChildAt(0).getTop();
		
		if(firstVisibleItem == 1) {
		}
		
		// Check if currently at the bottom of the GridView and whether currenlty loading something
	    if(firstVisibleItem + visibleItemCount == totalItemCount && !currentlyLoading && lastPostId != null && !enoughLoadingForToday) {
	    	// It is important to check if something is currently loading, otherwise this method is going to be called
	    	// multiple times in a row which results in a continuous loading of posts...which is just not what I want
	    	// and not what the user wants. Pretty cool trick.
	    	downloadJson(getArguments().getString(ARG_SUBREDDIT_URL) + ".json" + "?limit=10" + "&after=" + lastPostId, false);
	    	currentlyLoading = true;
	    }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}

	/*
	 * UI Utils
	 */
	
	public void showInfoCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.INFO).show();
	}
	
	public void showConfirmCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.CONFIRM).show();
	}
	
	public void showErrorCrouton(int stringId) {
		Crouton.makeText(getActivity(), getResources().getString(stringId), Style.ALERT).show();
	}
	
	private void errorParsing(Exception e) {
		error(e, R.string.error_parse_title, R.string.error_parse_message);
	}
	
	private void errorNetwork(VolleyError e) {
		error(e, R.string.error_connection_title, R.string.error_connection_message);
	}
	
	private void errorImage(Exception e) {
		error(e, R.string.error_unexpected_image_title, R.string.error_unexpected_image_message);
	}
	
	private void errorWallpaper(Exception e) {
		error(e, R.string.error_unexpected_wallpaper_title, R.string.error_unexpected_wallpaper_message);
	}
	
	private void errorUnknown(Exception e) {
		error(e, R.string.error_unknown_title, R.string.error_unknown_message);
	}
	
	private void error(final Exception e, int errorTitleId, int errorMessageId) {
		e.printStackTrace();
		final Activity lastActivity = getActivity();
		final Resources resources = lastActivity.getResources();
		try {
			Crouton.clearCroutonsForActivity(getActivity());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		final Crouton crouton = Crouton.makeText(getActivity(),
				resources.getString(errorTitleId)
				+ " - "
				+ resources.getString(errorMessageId)
				+ " "
				+ resources.getString(R.string.tap_to_dismiss),
				Style.ALERT);
		crouton.setConfiguration(INFINITE);
	    crouton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				StringWriter errors = new StringWriter();
			    e.printStackTrace(new PrintWriter(errors));
			    ClipData errorData = ClipData.newPlainText(resources.getString(R.string.error_stacktrace), errors.toString());
			    clipboardManager.setPrimaryClip(errorData);
			    Toast.makeText(lastActivity.getApplicationContext(),
			    		resources.getString(R.string.toast_error_copied_to_clipboard),
			    		Toast.LENGTH_LONG).show();
			    crouton.hide();
			}
		});
	    crouton.setConfiguration(INFINITE);
	    crouton.show();
	}
	
	/*
	 * Methods called by other classes
	 */
	
	public static RedditFragment getThis() {
		return mThis;
	}
	
	public boolean isCurrentlyLoading() {
		return currentlyLoading;
	}
	
	public static final ImageManager getImageManager() {
	    return imageManager;
	}
	
	public void showImagePreview(String title, String imageUrl, int points) {
		Intent previewIntent = new Intent(getActivity().getApplicationContext(), ImagePreview.class);
		Bundle args = new Bundle();
		args.putString(ImagePreview.EXTRA_POST_TITLE, title);
		args.putString(ImagePreview.EXTRA_IMAGE_URL, imageUrl);
		args.putInt(ImagePreview.EXTRA_POST_POINTS, points);
		previewIntent.putExtras(args);
		startActivity(previewIntent);
		
	}
	
	public void startDownload(String imageName, String imageUrl) {
		try {
			showInfoCrouton(R.string.crouton_download);
			final DownloadManager mgr = (DownloadManager)getActivity().getSystemService(HomeActivity.DOWNLOAD_SERVICE);
	        String filenameArray[] = imageUrl.split("\\.");
	        String ext = filenameArray[filenameArray.length-1];
	        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(imageUrl));
			req.setTitle(getResources().getString(R.string.downloading_in_progress)).setDescription(imageName);
			req.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/Pony Wallpaper", imageName + "." + ext);
			final long downloadId = mgr.enqueue(req);
			loadingProgress.setProgress(0);
			loadingProgress.show();
			Timer myTimer = new Timer();
		    myTimer.schedule(new TimerTask() {          
		        @Override
		            public void run() {
		            DownloadManager.Query q = new DownloadManager.Query();
		                q.setFilterById(downloadId);
		                Cursor cursor = mgr.query(q);
		                cursor.moveToFirst();
		                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
		                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
		                cursor.close();
		                final int dl_progress = (bytes_downloaded * 100 / bytes_total);
		                getActivity().runOnUiThread(new Runnable(){
		                    @Override
		                    public void run(){
	                    		loadingProgress.setProgress(dl_progress);
		                    	if(dl_progress == 100)
	                    			loadingProgress.dismiss();
		                    }
		                });

		        }

		    }, 0, 10);
		}
		catch(Exception e) {
			errorImage(e);
		}
	}
	
	public void setWallpaper(final String imageUrl) {
		showInfoCrouton(R.string.crouton_set_wallpaper);
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
		        	errorWallpaper(e);
		        }
		    }
		});
		thread.start();
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
	
	public int getPosForView(View view) {
		return list.getPositionForView(view);
	}
	
}