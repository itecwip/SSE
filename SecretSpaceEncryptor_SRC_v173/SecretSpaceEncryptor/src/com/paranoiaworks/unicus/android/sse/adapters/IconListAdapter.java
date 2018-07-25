package com.paranoiaworks.unicus.android.sse.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.components.SelectionDialog;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

import ext.os.misc.AutoResizeTextView;

/**
 * Icon List Adapter
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */
public class IconListAdapter extends BaseAdapter {
	private Context context;
	private final List<String> title;
	private final List<String> comment;
	private final List<Integer> icon;
	private List<Integer> itemTypes;
	private TruncateAt commentTruncateAt = null;
	
    public IconListAdapter(Context c, List<String> title,  List<String> comment, List<Integer> iconResource, List<Integer> itemTypes) {
    	this(c, title, comment, iconResource, itemTypes, null);
    }
    
    public IconListAdapter(Context c, List<String> title,  List<String> comment, List<Integer> iconResource, List<Integer> itemTypes, TruncateAt commentTruncateAt) {
    	context = c;
        this.title = title;
        this.comment = comment;
        this.icon = iconResource;
        this.commentTruncateAt = commentTruncateAt;
        this.itemTypes = itemTypes;
    }
	
	public static class ViewHolder {
		public AutoResizeTextView titleTV;
		public TextView commentTV;
		public ImageView iconIV;
		public FrameLayout delimiter;
		public FrameLayout devider;
	}
	
    public int getCount() {
        return title.size();
    }
    
    public Object getItem(int position) {
        return null;
    }
    
    public long getItemId(int position) {
        return 0;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			rowView = inflater.inflate(R.layout.lc_icon_listrow, null, true);
			holder = new ViewHolder();
			holder.titleTV = (AutoResizeTextView) rowView.findViewById(R.id.ILA_title);
			holder.titleTV.setMinTextSize(12.0F);
			holder.commentTV = (TextView) rowView.findViewById(R.id.ILA_comment);
			if(commentTruncateAt != null) holder.commentTV.setEllipsize(commentTruncateAt);
			holder.iconIV = (ImageView) rowView.findViewById(R.id.ILA_icon);
			holder.delimiter = (FrameLayout) rowView.findViewById(R.id.ILA_delimiter);
			holder.devider = (FrameLayout) rowView.findViewById(R.id.ILA_devider);
			
			rowView.setTag(holder);
			
		} else {
			holder = (ViewHolder)rowView.getTag();
		}
		
		if(itemTypes != null) 
		{
			rowView.setBackgroundResource(R.drawable.border_b1px_black);
			//holder.devider.setVisibility(View.VISIBLE);
			
			if(itemTypes.get(position) == SelectionDialog.ITEMTYPE_INACTIVE) {
				holder.titleTV.setTextColor(Color.rgb(180, 180, 180));
				holder.commentTV.setTextColor(Color.rgb(180, 180, 180));
			} 
			else if(itemTypes.get(position) == SelectionDialog.ITEMTYPE_HIGHLIGHTED) {
				holder.titleTV.setTextColor(Color.rgb(0, 0, 0));
				holder.commentTV.setTextColor(Color.rgb(0, 0, 0));
				rowView.setBackgroundResource(R.drawable.border_b1px_black_b);
			}
			else {
				holder.titleTV.setTextColor(Color.rgb(0, 0, 0));
				holder.commentTV.setTextColor(Color.rgb(0, 0, 0));
			}
		}
			
		holder.titleTV.setText(title.get(position));
		if(position >= comment.size() || comment.get(position) == null) {
			holder.commentTV.setVisibility(View.GONE);
			holder.delimiter.setVisibility(View.GONE);
		}
		else {
			holder.commentTV.setVisibility(View.VISIBLE);
			holder.delimiter.setVisibility(View.VISIBLE);
			holder.commentTV.setText(comment.get(position));
		}
		
		if(position >= icon.size() || icon.get(position) == null) 
			holder.iconIV.setVisibility(View.GONE);
		else 
		{
			if(itemTypes != null && itemTypes.get(position) == SelectionDialog.ITEMTYPE_INACTIVE)
			{
				Bitmap bitmap = BitmapFactory.decodeResource(rowView.getResources(), icon.get(position));
				bitmap = Helpers.toGrayscale(bitmap);
				holder.iconIV.setImageBitmap(bitmap);
				
			}
			else holder.iconIV.setImageResource(icon.get(position));
			
			holder.iconIV.setVisibility(View.VISIBLE);
		}
		
		return rowView;
	}
	
    @Override
    public boolean isEnabled(int position) {
        if(itemTypes == null) return true;
        else return itemTypes.get(position) != SelectionDialog.ITEMTYPE_INACTIVE;
    }
	
    private int dpToPx(float dp)
    {
    	float scale = context.getResources().getDisplayMetrics().density;
    	return (int)(dp * scale + 0.5f);
    }
}