package com.lesikapk.ponywalls;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImagePreview extends Activity {
    
	public static final String EXTRA_POST_TITLE = "extra_title";
	public static final String EXTRA_IMAGE_URL = "extra_url";
	public static final String EXTRA_POST_POINTS = "extra_points";
	
	private PhotoViewAttacher attacher;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.MyTheme_Holo_Transparent);
        
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater)getActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_imagepreview_layout, null);
        customActionBarView
        	.findViewById(R.id.actionbar_done)
        	.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Done"
                        finish();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)

        setContentView(R.layout.activity_image);
        Bundle args = getIntent().getExtras();
        String title = args.getString(EXTRA_POST_TITLE);
        String imageUrl = args.getString(EXTRA_IMAGE_URL);
        int points = args.getInt(EXTRA_POST_POINTS);
        ImageView imageView = (ImageView)findViewById(R.id.wallpaper_in_photoview);
        Picasso.with(getApplicationContext())
        .load(imageUrl)
//        .placeholder(R.drawable.spinner)
//        .resize(50, 50)
//        .centerCrop()
        .into(imageView);
        attacher = new PhotoViewAttacher(imageView);
    }
}