package com.lesikapk.ponywalls;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

public class HomeActivity extends FragmentActivity implements ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private String subreddit = "http://www.reddit.com/r/ponywalls/";
	private String url;
	private Utils utils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up utils
		utils = new Utils();
		utils.loadTheme(getApplicationContext(), getResources(), this, getActionBar());
		
		// Inflate layout
		setContentView(R.layout.activity_home);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, getResources().getStringArray(R.array.tab_names)), this);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		utils.loadTheme(getApplicationContext(), getResources(), this, getActionBar());
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	protected void onResume() {
		utils.loadTheme(getApplicationContext(), getResources(), this, getActionBar());
		super.onResume();
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		switch (position) {
		case 0:
			url = subreddit;
			break;
		case 1:
			url = subreddit + "new/";
			break;
		case 2:
			url = subreddit + "rising/";
			break;
		case 3:
			url = subreddit + "controversial/";
			break;
		case 4:
			url = subreddit + "top/";
			break;
		case 5:
			url = subreddit + "gilded/";
			break;
		default:
			url = null;
			break;
		}
		Fragment fragment = new RedditFragment();
		Bundle args = new Bundle();
		args.putString(RedditFragment.ARG_SUBREDDIT_URL, url);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
//		Some stuff for the search thingy I might implement some day (but this day is not today)
//	    MenuItem searchItem = menu.findItem(R.id.action_search);
//	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(getBaseContext(),SettingsActivity.class));
		case R.id.action_reload:
			RedditFragment.getThis().reloadPosts();
		default:
			// Nothing happens :)
		}
		return super.onOptionsItemSelected(item);
	}
}
