package com.paranoiaworks.unicus.android.sse.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Provides icons and other resources for chosen color (android.graphics.Color)
 * 
 * @author Paranoia Works
 * @version 1.0.3
 */
public class ColorHelper {

	private static Map<Integer, ColorBean> mainColorMap;
	private static List<ColorBean> colorList;
	
	static
	{
		int color;
		mainColorMap = new HashMap<Integer, ColorBean>();
		color = Color.rgb(255, 255, 0);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_yellow, R.drawable.folder_yellow, R.drawable.password_item_yellow));
		color = Color.rgb(255, 0, 0);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_red, R.drawable.folder_red, R.drawable.password_item_red));
		color = Color.rgb(170, 0, 255);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_purple, R.drawable.folder_purple, R.drawable.password_item_purple));
		color = Color.rgb(255, 150, 0);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_orange, R.drawable.folder_orange, R.drawable.password_item_orange));
		color = Color.rgb(0, 220, 220);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_lightblue, R.drawable.folder_lightblue, R.drawable.password_item_lightblue));
		color = Color.rgb(0, 255, 0);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_green, R.drawable.folder_green, R.drawable.password_item_green));
		color = Color.rgb(0, 0, 255);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.color_blue, R.drawable.folder_blue, R.drawable.password_item_blue));
		
		//+ Special Icons
		color = Color.rgb(0, 120, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_internet, R.drawable.folder_spec_internet, null));
		color = Color.rgb(0, 121, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_personal, R.drawable.folder_spec_personal, null));
		color = Color.rgb(0, 122, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_mail, R.drawable.folder_spec_mail, null));
		color = Color.rgb(0, 123, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_wifi, R.drawable.folder_spec_wifi, null));
		color = Color.rgb(0, 124, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_social, R.drawable.folder_spec_social, null));
		color = Color.rgb(0, 125, 240);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color,
				R.string.icon_spec_cloud, R.drawable.folder_spec_cloud, null));
		color = Color.rgb(100, 100, 100);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_data, R.drawable.folder_spec_data, null));
		color = Color.rgb(100, 100, 101);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_database, R.drawable.folder_spec_database, null));
		color = Color.rgb(100, 100, 102);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_hardware, R.drawable.folder_spec_hardware, null));
		color = Color.rgb(100, 100, 103);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_notes, R.drawable.folder_spec_notes, null));
		color = Color.rgb(100, 100, 104);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_garbage, R.drawable.folder_spec_garbage, null));
		color = Color.rgb(100, 100, 105);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_texts, R.drawable.folder_spec_texts, null));
		color = Color.rgb(100, 100, 106);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_files, R.drawable.folder_spec_files, null));
		color = Color.rgb(100, 100, 107);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_computer, R.drawable.folder_spec_computer, null));
		color = Color.rgb(100, 100, 108);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_phone, R.drawable.folder_spec_phone, null));
		color = Color.rgb(200, 160, 20);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_finance, R.drawable.folder_spec_finance, null));
		color = Color.rgb(200, 160, 21);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_cards, R.drawable.folder_spec_cards, null));
		color = Color.rgb(10, 255, 135);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_freetime, R.drawable.folder_spec_freetime, null));
		color = Color.rgb(10, 255, 136);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_games, R.drawable.folder_spec_games, null));
		color = Color.rgb(240, 100, 0);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_job, R.drawable.folder_spec_job, null));
		color = Color.rgb(240, 100, 1);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_education, R.drawable.folder_spec_education, null));
		color = Color.rgb(255, 255, 1);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_ideas, R.drawable.forder_spec_ideas, null));
		color = Color.rgb(0, 110, 80);
		mainColorMap.put(color, (new ColorHelper()).new ColorBean(color, 
				R.string.icon_spec_memories, R.drawable.folder_spec_elephant, null));
		
		//- Special Icons
		
		colorList = new ArrayList<ColorHelper.ColorBean>(mainColorMap.values());
		Collections.sort(colorList);
	}
	
	private ColorHelper(){}
	
	/** Get ColorBean for provided ColorCode */
	public static ColorBean getColorBean(int colorCode)
	{
		ColorBean cb = mainColorMap.get(colorCode);
		if(cb == null) cb = mainColorMap.get(Color.rgb(255, 255, 0));
		return cb;
	}
	
	/** Get supported Colors */
	public static List<ColorHelper.ColorBean> getColorList()
	{	
		return colorList;
	}
	
	/** Position in the colorList */
	public static int getColorPosition(int colorCode)
	{
		 return colorList.indexOf(getColorBean(colorCode));
	}
	
    /** Keeps color attributes */
	public class ColorBean implements Comparable<ColorBean>
    {    
    	public int colorCode;
        public int colorNameRId;
        public Integer folderIconRId;
        public Integer itemIconRId;
        
    	public ColorBean(int colorCode, int colorNameRId, Integer folderIconRId, Integer itemIconRId)
        {
        	this.colorCode = colorCode;
        	this.colorNameRId = colorNameRId;
        	this.folderIconRId = folderIconRId;
        	this.itemIconRId = itemIconRId;
        }
    	
    	public int compareTo(ColorBean cb)
    	{
    		if(this.itemIconRId == null && cb.itemIconRId != null) return 1;  //Special Icons to End (folderIconRId not used in this version - need to be defined if needed)
    		if(this.itemIconRId != null && cb.itemIconRId == null) return -1; //Special Icons to End...
    		if(this.colorCode < cb.colorCode) return 1;
    		if(this.colorCode > cb.colorCode) return -1;
    		return 0;
    	}
    }
}
