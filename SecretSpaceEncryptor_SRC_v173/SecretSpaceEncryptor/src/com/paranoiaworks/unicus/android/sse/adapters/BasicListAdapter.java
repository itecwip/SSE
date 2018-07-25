package com.paranoiaworks.unicus.android.sse.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Basic List Adapter
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class BasicListAdapter extends BaseAdapter {
	private Context context;
	private final List<String> rowText;
	
    public BasicListAdapter(Context c, List<String> rowText) {
    	context = c;
        this.rowText = rowText;
    }
	
	static class ViewHolder {
		public TextView text;
	}
	
    public int getCount() {
        return rowText.size();
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
			rowView = inflater.inflate(R.layout.lc_basic_listrow, null, true);
			holder = new ViewHolder();
			holder.text = (TextView)rowView.findViewById(R.id.text);			
			
			rowView.setTag(holder);
			
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
			
		holder.text.setText(rowText.get(position));
		
		return rowView;
	}
}