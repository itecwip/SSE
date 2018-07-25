package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Verbose Wait Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */
public class VerboseWaitDialog extends SecureDialog {

	TextView mainText;
	Button endButton;
	LinearLayout buttonWrapper;
	
	public VerboseWaitDialog(View v) 
	{
		this((Activity)v.getContext());
	}
	
	public VerboseWaitDialog(Context context) 
	{
		super(context);
		init();
	}
	
	public VerboseWaitDialog(Context context, int theme) 
	{
		super(context, theme);
		init();
	}
	
	public void appendText(String text)
	{
		mainText.append(Html.fromHtml(text));
	}

	public void showButton()
	{
		buttonWrapper.setVisibility(Button.VISIBLE);
	}
	
	public void hideButton()
	{
		buttonWrapper.setVisibility(Button.GONE);
	}
	
	private void init()
	{		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lc_verbose_wait_dialog);
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
		
		mainText = (TextView)findViewById(R.id.VWD_text);
		buttonWrapper = (LinearLayout)findViewById(R.id.VWD_buttonWrapper);
		endButton = (Button)findViewById(R.id.VWD_button);
		endButton.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	cancel();
		    }
	    });
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else return super.onKeyDown(keyCode, event);
    }	
}
