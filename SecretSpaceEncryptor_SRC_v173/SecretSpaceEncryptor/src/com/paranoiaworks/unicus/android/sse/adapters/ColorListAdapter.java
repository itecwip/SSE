package com.paranoiaworks.unicus.android.sse.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.utils.ColorHelper.ColorBean;

/**
 * Provides "Color Selection Row" appearance with corresponding icons for required colors
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 * @related ColorHelper.java (ColorBean inner class)
 */
public class ColorListAdapter extends BaseAdapter {
	private Context context;
	private final List<ColorBean> colors = new ArrayList<ColorBean>();
	private int iconSet = 0;
	
	public static final int ICONSET_ITEMS = 1;
	public static final int ICONSET_FOLDERS = 2;
	
    public ColorListAdapter(Context c, List<ColorBean> colors, int iconSetCode) {
    	context = c;
        
        switch (iconSetCode) 
        {        
        	case ICONSET_ITEMS:
            	for(ColorBean color : colors)
                {     	
            		if(color.itemIconRId != null) this.colors.add(color);
                }
        		break;
        		
        	case ICONSET_FOLDERS:
            	for(ColorBean color : colors)
                {     	
            		if(color.folderIconRId != null) this.colors.add(color);
                }
        		break;
        }
        this.iconSet = iconSetCode;
    }
	
	static class ViewHolder {
		public ImageView colorIcon;
		public TextView colorName;
	}
	
    public int getCount() {
        return colors.size();
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
			rowView = inflater.inflate(R.layout.lc_color_listrow, null, true);
			holder = new ViewHolder();
			holder.colorIcon = (ImageView) rowView.findViewById(R.id.Color_icon);
			holder.colorName = (TextView) rowView.findViewById(R.id.Color_name);			
			
			rowView.setTag(holder);
			
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
			
		ColorBean tempColor = colors.get(position);
			
		holder.colorName.setText(rowView.getResources().getText(tempColor.colorNameRId));
		
		if(iconSet == ICONSET_ITEMS)
			holder.colorIcon.setImageResource(tempColor.itemIconRId);
		else if(iconSet == ICONSET_FOLDERS)
			holder.colorIcon.setImageResource(tempColor.folderIconRId);
		else holder.colorIcon.setImageResource(R.drawable.null_image);
		
		return rowView;
	}
}