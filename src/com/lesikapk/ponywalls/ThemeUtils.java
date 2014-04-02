package com.lesikapk.ponywalls;

import com.lesikapk.ponywalls.library.SystemBarTintManager;

import android.R.bool;
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
		loadTheme(activity, isDark(activity));
	}
	
	public void loadTheme(Activity activity, boolean isDark) {
		System.out.println(isDark);
		if(isDark)
			activity.getApplication().setTheme(R.style.Theme_PonyWalls_Holo);
		else
			activity.getApplication().setTheme(R.style.Theme_PonyWalls_Holo_Light);
	}
	
	public boolean isDark(Activity activity) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		return prefs.getBoolean("dark_mode_enabled", false);
	}
	
//	public void loadTheme(Activity activity) {
//		loadTheme(activity, getThemeSetting(activity));
//	}
	
	public void loadTheme(Activity activity, String theme) {
//		int colorResource = getThemeColorResource(activity, theme);
//		loadTheme(activity, theme, colorResource, null, false);
	}
	
	public void loadTheme(Activity activity, View view, boolean shouldApplyColor) {
//		String theme = getThemeSetting(activity);
//		loadTheme(activity, view, shouldApplyColor, theme);
	}
	
	public void loadTheme(Activity activity, View view, boolean shouldApplyColor, String theme) {
//		int colorResource = getThemeColorResource(activity, theme);
//		loadTheme(activity, theme, colorResource, view, shouldApplyColor);
			
	}

//	private void loadTheme(Activity activity, String theme, int colorResource, View view, boolean shouldApplyColor) {
//		// This is the actual method that does stuff. The other methods are just for using outside, for example providing a view
//		// and telling the method that the view needs to be colored are variables from "outside"...I'm not a native English speaker so it's hard to explain
//		String colorString = "#"+Integer.toHexString(activity.getResources().getColor(colorResource));
//		int color = Color.parseColor(colorString);
//		if(isThemeDark(activity)) {
//			activity.setTheme(android.R.style.Theme_Holo);
//		}
//		else {
//			activity.setTheme(android.R.style.Theme_Holo_Light);
//		}
//		activity.getActionBar().setBackgroundDrawable(new ColorDrawable(color));		
//		if(kitkatOrHigher()) {
//			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
//			tintManager.setStatusBarTintEnabled(true);
//			tintManager.setStatusBarTintColor(color);
//			if(view != null) {
//				SystemBarTintManager.SystemBarConfig tintManagerConfig = tintManager.getConfig();
//				view.setPadding(0, tintManagerConfig.getPixelInsetTop(true), tintManagerConfig.getPixelInsetRight(), 0);
//				if(shouldApplyColor) {
//					view.setBackgroundColor(color);
//				}
//			}
//		}
//	}
	
	private boolean kitkatOrHigher() {
		//return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		return false;
	}
	
}
