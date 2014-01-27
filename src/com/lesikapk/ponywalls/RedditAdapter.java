package com.lesikapk.ponywalls;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class RedditAdapter extends BaseAdapter {
	
	private ArrayList<RedditItem> list;
	private Context mContext;
	private int lastPosition;
	public static int i = 0;
	
	public static class ViewHolder {
		TextView title;
		TextView details;
		TextView points;
		ImageView thumb;
		ImageView image;
	}

	public RedditAdapter(ArrayList<RedditItem> itemArray, Context context) {
		list = itemArray;
		mContext = context;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
        	convertView = LayoutInflater.from(mContext).inflate(R.layout.card_post, null);
        	TextView votesPrefix = (TextView)convertView.findViewById(R.id.votes_prefix);
        	votesPrefix.setText(mContext.getString(R.string.post_votes_prefix) + " ");
        	holder = new ViewHolder();
            holder.title = (TextView)convertView.findViewById(R.id.mycard_title);
            holder.details = (TextView)convertView.findViewById(R.id.mycard_time);
            holder.points = (TextView)convertView.findViewById(R.id.votes);
            holder.image = (ImageView)convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.image.setImageResource(R.drawable.spinner_holo);
        holder.title.setText(list.get(index).title);
        holder.details.setText(list.get(index).subreddit);
        int points = list.get(index).points;
        String text = Integer.toString(points) + " " + mContext.getString(R.string.post_votes_suffix);
        holder.points.setText(text);
        if(!list.get(index).thumbnail.isEmpty()) {
        	UrlImageViewHelper.setUrlDrawable(holder.image, list.get(index).url);
		}
        else {
        	holder.thumb.setImageResource(R.drawable.emo_im_cool);
        }
//        if (i < 1) {
//        	// To make sure the first post is loaded with the right animation.
//        	// For whatever reason, the first list item uses a different animation than the others.
//        	// This if is run only once, when the first list item is loaded.
//        	Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scroll_up_animation);
//        	convertView.startAnimation(animation);
//	        lastPosition = index;
//        	i++;
//        }
//        else {
//	        Animation animation = AnimationUtils.loadAnimation(mContext, (index > lastPosition) ? R.anim.scroll_up_animation : R.anim.no_animation);
//	        convertView.startAnimation(animation);
//	        lastPosition = index;
//        }
        return convertView;
	}
}
