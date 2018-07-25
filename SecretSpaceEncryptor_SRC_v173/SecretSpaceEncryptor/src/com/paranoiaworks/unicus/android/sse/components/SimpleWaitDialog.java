package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Simple "Rotating wheels" Wait Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */
public class SimpleWaitDialog extends SecureDialog {
	
	private AnimationDrawable waitAnimation;
	private TextView dialogTitle;

	public SimpleWaitDialog(View v) 
	{
		this((Activity)v.getContext());
	}
	
	public SimpleWaitDialog(Context context) 
	{
		super(context);
		init();
	}
	
	public SimpleWaitDialog(Context context, int theme) 
	{
		super(context, theme);
		init();
	}
	
	@Override
	public void setTitle(CharSequence title) 
	{
		dialogTitle.setText(title);
	}

	private void init()
	{		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lc_simple_wait_dialog);
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
    	ImageView waitImage = (ImageView) findViewById(R.id.WD_image);
    	waitAnimation = (AnimationDrawable)getContext().getResources().getDrawable(R.anim.anim_wait);
        waitImage.setImageDrawable(waitAnimation);
        dialogTitle = (TextView) findViewById(R.id.WD_title);
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) 
    {     
    	waitAnimation.start();     
        super.onWindowFocusChanged(hasFocus);
    }
}
