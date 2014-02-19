package com.lesikapk.ponywalls;

import com.lesikapk.ponywalls.library.SystemBarTintManager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;

public class ThemeUtils {

	public void createTransparency(Context context, Resources resources, Activity activity, int color) {
		// Set up the status- and navbar to be transparent/tinted
	    SystemBarTintManager tintManager = new SystemBarTintManager(activity);
	    tintManager.setTintColor(color);
//	    tintManager.setStatusBarTintDrawable(MyDrawable);
//	    tintManager.setStatusBarTintEnabled(true);
//	    tintManager.setNavigationBarTintEnabled(true);
	}
	
	public int loadTheme(Context context, Resources resources, Activity activity, ActionBar actionBar) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String theme = prefs.getString("theme", resources.getString(R.string.setting_theme_holo_light_darkactionbar_value));
		return loadThemeWithId(context, resources, activity, actionBar, theme);
	}
	public int loadThemeWithId(Context context, Resources resources, Activity activity, ActionBar actionBar, String theme) {
		int color;
		boolean isDark = false;
		if(theme == resources.getString(R.string.setting_theme_holo_red_value)) {
			color = R.color.theme_holo_red;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_dark_value)) {
			isDark = true;
			color = R.color.theme_holo_dark;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_green_value)) {
			color = R.color.theme_holo_green;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_blue_value)) {
			color = R.color.theme_holo_blue;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_lightblue_value)) {
			color = R.color.theme_holo_lightblue;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_orange_value)) {
			color = R.color.theme_holo_orange;
		}
		else if(theme == resources.getString(R.string.setting_theme_holo_light_darkactionbar_value)) {
			isDark = true;
			activity.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
			color = R.color.theme_holo_light_darkactionbar;
		}
		else {
			isDark = true;
			activity.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
			color = R.color.theme_holo_light_darkactionbar;
		}
		if(!isDark) {
			activity.setTheme(android.R.style.Theme_Holo_Light);
		}
		
		actionBar.setBackgroundDrawable(new ColorDrawable(resources.getColor(color)));
		
		return resources.getColor(color);
	}
	
}
