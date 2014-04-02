package com.lesikapk.ponywalls;

import java.util.ArrayList;

import android.R.anim;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.imageloader.core.model.ImageTag;
import com.novoda.imageloader.core.model.ImageTagFactory;
import com.squareup.picasso.Picasso;

public class RedditAdapter extends BaseAdapter {
	
	private ArrayList<RedditItem> list;
	private Context mContext;
	private TextView votesPrefix;
	private Button downloadButton;
	private ImageButton setWallpaperButton;
	private ImageButton shareButton;
	private ImageButton threeDotButton;
	public int lastPosition = -1;
	
	private boolean userLikesItDark;
	
	public static class ViewHolder {
		TextView title;
		TextView author;
		TextView points;
		ImageView image;
	}

	public RedditAdapter(ArrayList<RedditItem> itemArray, Context context) {
		list = itemArray;
		mContext = context;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		userLikesItDark = prefs.getBoolean("dark_mode_enabled", false);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public void clearAllItems() {
		list.removeAll(list);
		lastPosition = -1;			// Revert the position to -1 so the animation is good again.
	}
	
	@Override
	public View getView(int index, View convertView, ViewGroup arg2) {
		// Initialize some stuff
		ViewHolder holder = null;
		ImageTagFactory imageTagFactory = ImageTagFactory.newInstance(mContext, R.drawable.spinner);
		imageTagFactory.setDefaultImageResId(R.drawable.spinner);
		imageTagFactory.setErrorImageId(R.drawable.icn_error_404_cloud);
        if(convertView == null) {
        	
//        	if(!userLikesItDark) {
//        		convertView = LayoutInflater.from(mContext).inflate(R.layout.card_post_holo_light, null);
//        	}
//        	else {
    		convertView = LayoutInflater.from(mContext).inflate(R.layout.card_post, null);
//        	}
        	
        	holder = new ViewHolder();
            holder.title 			= (TextView)	convertView.findViewById(R.id.post_title);
            holder.author 			= (TextView)	convertView.findViewById(R.id.post_author);
            holder.points 			= (TextView)	convertView.findViewById(R.id.post_votes);
        	holder.image			= (ImageView)	convertView.findViewById(R.id.post_image);
        	
            votesPrefix 			= (TextView)	convertView.findViewById(R.id.post_votes_prefix);
        	downloadButton 			= (Button)		convertView.findViewById(R.id.download_button);
            setWallpaperButton 		= (ImageButton)	convertView.findViewById(R.id.set_wallpaper_button);
            shareButton				= (ImageButton)	convertView.findViewById(R.id.share_button);
            threeDotButton			= (ImageButton) convertView.findViewById(R.id.threedot_button);
            
            convertView.setTag(holder);
            setListeners(holder, index);
        }
        else {
        	holder = (ViewHolder)convertView.getTag();
        }
		// Title
        holder.title.setText(list.get(index).title);
        
        // Author
        holder.author.setText(mContext.getString(R.string.post_author_prefix)+ " " + list.get(index).author);
        
        // Points
        int points = list.get(index).points;
        String text = Integer.toString(points) + " " + mContext.getString(R.string.post_votes_suffix);
        votesPrefix.setText(mContext.getString(R.string.post_votes_prefix) + " ");
        holder.points.setText(text);
        
        // Image
        if(list.get(index).url != null) {
//        	ImageTag tag = imageTagFactory.build(list.get(index).url, mContext);
//        	((ImageView) holder.image).setTag(tag);
//        	holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        	RedditFragment.getImageManager().getLoader().load(holder.image);
        	Picasso.with(mContext).setDebugging(true);
        	Picasso.with(mContext).load(list.get(index).url).placeholder(android.R.drawable.spinner_background).into(holder.image);
        }
        
        // Animation
        Animation animation = AnimationUtils.loadAnimation(mContext, (index > lastPosition) ? R.anim.scroll_up_animation : R.anim.no_animation);
        convertView.startAnimation(animation);
        lastPosition = index;
        
        return convertView;
	}
	
	private void setListeners(ViewHolder holder, int position) {
		final RedditFragment thisFragment = RedditFragment.getThis();
		
		holder.image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RedditItem item = list.get(thisFragment.getPosForView(v));
				RedditFragment.getThis().showImagePreview(item.title, item.url, item.points);
			}
		});
		
		downloadButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RedditItem item = list.get(thisFragment.getPosForView(v));
				RedditFragment.getThis().startDownload(item.title, item.url);
			}
		});
		
        setWallpaperButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RedditFragment.getThis().setWallpaper(list.get(thisFragment.getPosForView(v)).url);
			}
		});
        
        shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RedditFragment.getThis().shareWallpaper(list.get(thisFragment.getPosForView(v)).url);
				
			}
		});
        
        threeDotButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RedditItem item = list.get(thisFragment.getPosForView(v));
				RedditFragment.getThis().onThreeDotMenuClicked(item.commentsUrl, item.url, item.author);
			}
		});
	}
}
