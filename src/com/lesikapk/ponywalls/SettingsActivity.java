package com.lesikapk.ponywalls;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lesikapk.ponywalls.library.SystemBarTintManager;

public class SettingsActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	PagerTitleStrip tabs; 
	
	ThemeUtils themeUtils;
	SystemBarTintManager tintManager;
	SystemBarTintManager.SystemBarConfig config;
	static SettingsActivity mThis;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setWindowAnimations(0);
		setContentView(R.layout.activity_settings);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		tabs = (PagerTitleStrip)findViewById(R.id.pager_title_strip);
		
		// Load Utils
		themeUtils = new ThemeUtils();
		themeUtils.loadTheme(this, tabs, true);
		mThis = this;
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public static SettingsActivity getThis() {
		return mThis;
	}
	
	public static ThemeUtils getThemeUtils() {
		return getThis().themeUtils;
	}
	
	public static void reloadTheme(String newSettingsValue) {
		// It is important to use loadThemeWithId here because the SharedPreferences get updated slowly. So we have to parse the theme that
		// we get directly from the onPreferenceDismissListener or whatever its name was...phew that's complicated shit!
		if(getThemeUtils() != null && getThis().tabs != null)
			getThemeUtils().loadTheme(getThis(), getThis().tabs, true, newSettingsValue);
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			int xml;
			
			if(position < 2) {
				switch (position) {
					case 0: xml = R.xml.preferences;
						break;
					case 1: xml = R.xml.author;
						break;
					default: xml = R.xml.author;
						break;
				}
				SettingsFragment fragment = new SettingsFragment();
				Bundle args = new Bundle();
				args.putInt(SettingsFragment.ARG_PREFERENCE_XML, xml);
				args.putInt(SettingsFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			else {
				Fragment fragment = new OtherFragment();
				Bundle args = new Bundle();
				args.putInt(OtherFragment.ARG_LAYOUT_XML, R.xml.help);
				args.putInt(OtherFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.tab_settings).toUpperCase(l);
			case 1:
				return getString(R.string.tab_author).toUpperCase(l);
			case 2:
				return getString(R.string.tab_help).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class SettingsFragment extends PreferenceFragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String ARG_PREFERENCE_XML = "preference_xml";
		
		private ThemeUtils themeUtils;

		public SettingsFragment() {
			
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(getArguments().getInt(ARG_PREFERENCE_XML));
			
			// Only set all this stuff, if the selected tab is the preferences tab
			// Could also be done by the tab number, which is passed to this Fragment, but meh :)
			if(getArguments().getInt(ARG_PREFERENCE_XML) == R.xml.preferences) {
				// Set onThemeChangedListener
				final ListPreference themePreference = (ListPreference)findPreference("theme");
				themePreference.setSummary(themePreference.getEntry());
				
				themePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						// Update the current theme in the summary
						themePreference.setSummary(newValue.toString());
						
						SettingsActivity.reloadTheme(newValue.toString());
						
						Context context = getActivity().getApplicationContext();
						// If the theme before the clicking 
						if(SettingsActivity.getThemeUtils().isThemeDark(getActivity(), SettingsActivity.getThemeUtils().getThemeSetting(context))
								!= SettingsActivity.getThemeUtils().isThemeDark(getActivity(), newValue.toString())) {
							getActivity().finish();
							getActivity().startActivity(getActivity().getIntent());
						}
//						getThemeUtils().createTransparency(getActivity(), getResources(), getActivity(), getThemeUtils().loadThemeWithId(
//								getActivity(), getResources(), getActivity(), getActivity().getActionBar(), newValue.toString()));
						return true;
					}
				});
			}
		}
	}
	
	public static class OtherFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String ARG_LAYOUT_XML = "layout_xml"; 

		public OtherFragment() {
			
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(getArguments().getInt(ARG_LAYOUT_XML), container, false);
			return view;
		}
	}
}
