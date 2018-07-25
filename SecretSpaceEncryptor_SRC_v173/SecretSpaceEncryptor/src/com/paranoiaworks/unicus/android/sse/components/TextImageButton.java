package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;


// TODO - not used
public class TextImageButton extends RelativeLayout {

	private TextView textView;
	private ImageView image;
	
	public TextImageButton(Context context) {
		super(context);
		init(context);
	}


	public TextImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	
	public TextImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setText(CharSequence text) 
	{
		textView.setText(text);
	}
	
	public void setImage(Drawable drawable) 
	{
		image.setImageDrawable(drawable);
	}
	
	public ImageView getImageViewObject()
	{
		return image;
	}

	private void init(Context context)
	{		
		Activity c = (Activity)context;
		LayoutInflater inflater = c.getLayoutInflater();
		//View layout = inflater.inflate(R.layout.lc_toast_layout, (ViewGroup) c.findViewById(R.id.toast_layout_a));
		RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.lc_button_image_text, null);
		
    	image = (ImageView) layout.findViewById(R.id.COMP_ITButton_image);	
    	textView = (TextView) layout.findViewById(R.id.COMP_ITButton_text);
    	
    	RelativeLayout subRoot = (RelativeLayout) layout.findViewById(R.id.COMP_ITButton_SubROOT);	
  	
		//this.addView(layout);
		
    	layout.removeAllViews();
    	this.addView(subRoot);
	}

}
