package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Toast with image on the left 
 * (ok, cancel, info implemented)
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 */
public class ImageToast extends Toast {
	
	public final static int TOAST_IMAGE_OK = 1;
	public final static int TOAST_IMAGE_CANCEL = 2;
	public final static int TOAST_IMAGE_INFO = 3;
	public final static int TOAST_IMAGE_INFO_RED = 4;
	
	private TextView textView;
	private ImageView image;
	
	public ImageToast(String text, int imageCode, Activity context) 
	{
		super(context);
		
    	LayoutInflater inflater = context.getLayoutInflater();
    	View layout = inflater.inflate(R.layout.lc_toast_layout, (ViewGroup) context.findViewById(R.id.toast_layout_a));

    	image = (ImageView) layout.findViewById(R.id.image);
    	image.setImageResource(getDrawableID(imageCode));
    	  	
    	textView = (TextView) layout.findViewById(R.id.text);
    	textView.setText(Html.fromHtml(text));
    	
    	if(imageCode != TOAST_IMAGE_CANCEL) super.setDuration(Toast.LENGTH_SHORT);
    	else super.setDuration(Toast.LENGTH_LONG);
    		
    	super.setView(layout);
	}
	
	@Override
	public void setText(CharSequence text)
	{
		textView.setText(Html.fromHtml(text.toString()));
	}
	
	public void setImage(int imageCode)
	{
		image.setImageResource(getDrawableID(imageCode));
	}
	
	private int getDrawableID(int imageCode)
	{
        int drawableID = 0;
		switch (imageCode) 
        {
	        case 1: drawableID =(R.drawable.ok_icon); break;
	        case 2: drawableID =(R.drawable.cancel_icon); break;
	        case 3: drawableID = (R.drawable.info_icon); break;
	        case 4: drawableID = (R.drawable.info_icon_red); break;
	        default: drawableID = (R.drawable.cancel_icon); break;
        }
		
		return drawableID;
	}
}
