package com.lesikapk.ponywalls;

import com.lesikapk.ponywalls.library.SystemBarTintManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

public class ThemeUtils {
	
	public void loadTheme(Activity activity) {
		loadTheme(activity, getThemeSetting(activity.getApplicationContext()));
	}
	
	public void loadTheme(Activity activity, String theme) {
		int colorResource = getThemeColorResource(activity, theme);
		loadTheme(activity, theme, colorResource, null, false);
	}
	
	public void loadTheme(Activity activity, View view, boolean shouldApplyColor) {
		String theme = getThemeSetting(activity.getApplicationContext());
		loadTheme(activity, view, shouldApplyColor, theme);
	}
	
	public void loadTheme(Activity activity, View view, boolean shouldApplyColor, String theme) {
		int colorResource = getThemeColorResource(activity, theme);
		loadTheme(activity, theme, colorResource, view, shouldApplyColor);
			
	}

	private void loadTheme(Activity activity, String theme, int colorResource, View view, boolean shouldApplyColor) {
		// This is the actual method that does stuff. The other methods are just for using outside, for example providing a view
		// and telling the method that the view needs to be colored are variables from "outside"...I'm not a native English speaker so it's hard to explain
		String colorString = "#"+Integer.toHexString(activity.getResources().getColor(colorResource));
		int color = Color.parseColor(colorString);
		if(isThemeDark(activity, theme)) {
			activity.setTheme(android.R.style.Theme_Holo);
			activity.getActionBar().setBackgroundDrawable(new ColorDrawable(color));
		}
//		else if(theme.equalsIgnoreCase(activity.getResources().getString(R.string.setting_theme_holo_light_value))) {
//			activity.setTheme(android.R.style.Theme_Holo_Light);
//			activity.getActionBar().setBackgroundDrawable()
//		}
		else {
			activity.setTheme(android.R.style.Theme_Holo_Light);
			activity.getActionBar().setBackgroundDrawable(new ColorDrawable(color));
		}
		
		if(kitkatOrHigher()) {
			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintColor(color);
			if(view != null) {
				SystemBarTintManager.SystemBarConfig tintManagerConfig = tintManager.getConfig();
				view.setPadding(0, tintManagerConfig.getPixelInsetTop(true), tintManagerConfig.getPixelInsetRight(), 0);
				if(shouldApplyColor) {
					view.setBackgroundColor(color);
				}
			}
		}
	}
	
	private boolean kitkatOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}
	
	public String getThemeSetting(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString("theme", "R.string.setting_theme_holo_light_value");
	}
	
	public int getThemeColorResource(Activity activity, String theme) {
		Resources resources = activity.getResources();
		if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_red_value))) {
			return R.color.theme_holo_red;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_green_value))) {
			return R.color.theme_holo_green;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_blue_value))) {
			return R.color.theme_holo_blue;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_lightblue_value))) {
			return R.color.theme_holo_lightblue;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_orange_value))) {
			return R.color.theme_holo_orange;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_light_value))) {
			return R.color.theme_holo_light;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_dark_value))) {
			return R.color.theme_holo_dark;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_black_value))) {
			return R.color.theme_holo_black;
		}
		else {
			return -1;
		}
	}
	
	public boolean isThemeDark(Activity activity, String theme) {
		Resources resources = activity.getResources();
		if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_dark_value))) {
			return true;
		}
		else if(theme.equalsIgnoreCase(resources.getString(R.string.setting_theme_holo_black_value))) {
			return true;
		}
		else {
			return false;
		}
	}
}
