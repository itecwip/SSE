package com.paranoiaworks.unicus.android.sse.adapters;

import java.util.List;

import android.app.Activity;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.VaultItem;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.ColorHelper;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

import ext.com.andraskindler.quickscroll.Scrollable;

/**
 * Provides "Password Item Row" appearance (for the Items section of Password Vault)
 * 
 * @author Paranoia Works
 * @version 1.0.4
 */
public class PWVItemArrayAdapter extends BaseAdapter implements Scrollable {
	private final Activity context;
	private final List<VaultItem> items;
	private float customFontSizeMultiplier = 1.0F;
	private boolean dataHasChanged = false;
	
	public PWVItemArrayAdapter(Activity context, List<VaultItem> items) 
	{
		this.context = context;
		this.items = items;	
	}
	
    public int getCount() {
        return items.size();
    }
    
    public Object getItem(int position) {
    	return items.get(position);
    }
    
    public long getItemId(int position) {
        return position;
    }
	
	static class ViewHolder {
		public ImageView itemIcon;
		public TextView itemName;
		public TextView itemPassword;
		public TextView itemDate;
		public RelativeLayout passDateLine;
		public RelativeLayout mainLayout;
		public double originalTextSize = -1;
		public int originalColor;
		public int originalPasswordColor;
		public RelativeLayout.LayoutParams originalPassDateLineParams;
		public Drawable originalIconBg;
		
		public boolean lastRenderSpecial = false;
	}
	
	public void setFontSizeMultiplier(float multiplier)
	{
		customFontSizeMultiplier = multiplier;
	}

	@Override
	public synchronized View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.lc_passwordvault_item_listrow, null, true);
			holder = new ViewHolder();
			holder.itemName = (TextView) rowView.findViewById(R.id.PWVI_itemName);
			holder.itemPassword = (TextView) rowView.findViewById(R.id.PWVI_itemPassword);
			holder.itemDate = (TextView) rowView.findViewById(R.id.PWVI_itemDate);
			holder.itemIcon = (ImageView) rowView.findViewById(R.id.PWVI_itemIcon);
			holder.passDateLine = (RelativeLayout) rowView.findViewById(R.id.PWVI_passDateLine);
			holder.mainLayout = (RelativeLayout) rowView.findViewById(R.id.PWVI_RowLayout);
			
			if (holder.originalTextSize < 0)
			{
				//float den = rowView.getResources().getDisplayMetrics().density;
				holder.originalTextSize = holder.itemName.getTextSize();
				holder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(holder.originalTextSize * 1.32 * customFontSizeMultiplier));
				holder.itemPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(holder.originalTextSize * 1.28 * customFontSizeMultiplier));
				holder.originalColor = rowView.getResources().getColor(R.color.white_file);
				holder.originalPasswordColor = rowView.getResources().getColor(R.color.lightblue_file);
				holder.originalPassDateLineParams = (LayoutParams) holder.passDateLine.getLayoutParams();
				holder.originalIconBg = holder.itemIcon.getBackground();
				resetView(holder, rowView);
			}
			
			rowView.setTag(holder);
			
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
			
		VaultItem tempItem = items.get(position);
		holder.itemName.setTag(tempItem.getItemSecurityHash());
		if (holder.lastRenderSpecial) resetView(holder, rowView);
		
		if (!tempItem.isSpecial())
		{
			holder.itemName.setText(tempItem.getItemName());
			
			String tempPassword = tempItem.getItemPassword().trim();
			if(tempItem.isSelected())
			{				
				if(tempPassword.equals("")) 
					tempPassword = tempItem.getItemComment();
				holder.mainLayout.setBackgroundResource(R.drawable.d_itemicon_bg);
				holder.itemPassword.setText(tempPassword);
				holder.lastRenderSpecial = true;
				holder.itemDate.setText("");
			}		
			else
			{
				if(tempPassword.equals(""))
					holder.itemPassword.setText(tempItem.getItemComment());
				else
					holder.itemPassword.setText("********");
				holder.itemDate.setText(Helpers.getFormatedDate(tempItem.getDateModified(), rowView.getResources().getConfiguration().locale));
			}
			holder.itemIcon.setImageResource(ColorHelper.getColorBean(tempItem.getColorCode()).itemIconRId);

		} else {
			int code = tempItem.getSpecialCode();
			switch (code) 
	        {
		        case VaultItem.SPEC_GOBACKITEM:
		        	
		        	Shader textShader = new LinearGradient(0, 0, 0, holder.itemName.getTextSize(), 
		        			new int[]{tempItem.getColorCode(), holder.originalColor},
		        			new float[]{0, 1}, TileMode.CLAMP);
		        	
		        	holder.itemName.setText(rowView.getResources().getString(R.string.pwv_folder_text) + ": " + tempItem.getItemName());
		        	holder.itemName.getPaint().setShader(textShader);
		        	holder.itemIcon.setImageResource(R.drawable.item_back);
		        	holder.itemIcon.setBackgroundResource(R.drawable.null_image);
		        	holder.itemPassword.setText("");
		        	holder.itemDate.setText("");
		        	holder.passDateLine.setLayoutParams(new LayoutParams(0, 0));
		        	rowView.setBackgroundResource(R.drawable.d_filerow_a);
		        	break;
		        case VaultItem.SPEC_NEWITEM:  
		        	holder.itemName.setText(rowView.getResources().getString(R.string.pwv_newItem_text));
		        	holder.itemIcon.setImageResource(R.drawable.add_new);
		        	holder.itemIcon.setBackgroundResource(R.drawable.null_image);
		        	holder.itemPassword.setText("");
		        	holder.passDateLine.setLayoutParams(new LayoutParams(0, 0));
		        	rowView.setBackgroundResource(R.drawable.d_filerow_a);
		        	break;
	        }
			
			holder.lastRenderSpecial = true;
		}

		rowView.getBackground().setDither(true);
		return rowView;
	}
	
	private void resetView(ViewHolder holder, View rowView)
	{
		rowView.setBackgroundResource(R.drawable.d_filerow);
		holder.itemName.setTextColor(holder.originalColor);
		holder.itemName.getPaint().setShader(null);
		holder.itemPassword.setTextColor(holder.originalPasswordColor);
		holder.itemDate.setTextColor(holder.originalColor);
		holder.passDateLine.setLayoutParams(holder.originalPassDateLineParams);
		holder.itemIcon.setBackgroundDrawable(holder.originalIconBg);
		holder.mainLayout.setBackgroundResource(R.drawable.d_null_semitrans);
		holder.lastRenderSpecial = false;
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
		dataHasChanged = true;
	}
	
	public String getIndicatorForPosition(int childposition, int groupposition) {
		VaultItem tempItem = items.get(childposition);
		if(tempItem.isSpecial()) {
			if(tempItem.getSpecialCode() == VaultItem.SPEC_NEWITEM) return "+:Y";
			else return "..:Y";
		}
		return tempItem.getItemName().toUpperCase().charAt(0) + ":W";
	}

	public int getScrollPosition(int childposition, int groupposition) {
		return childposition;
	}
	
	public synchronized boolean hasDataChanged()
	{
		boolean temp = dataHasChanged;
		dataHasChanged = false;
		return temp;
	}
}