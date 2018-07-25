package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.app.Dialog;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Dual Progress Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.2
 */
public class DualProgressDialog extends SecureDialog {

	private Activity context;
	
	private TextView titleTextView;
	private ProgressBar progressBarA;
	private ProgressBar progressBarB;
	private TextView progressBarATextView;
	private TextView progressBarBTextView;
	private TextView verboseTextView;
	private Button okButton;
	private LinearLayout progressBarBContainer;
	private LinearLayout verboseContainer;
	private LinearLayout buttonWrapper;
	private ScrollView verboseSV;
	
	private boolean fullScreen = false;
	private int progressA;
	private int progressB;
	private int secondaryProgressA;
	private int maxA;
	private int maxB;
	private boolean finish = false;


	public DualProgressDialog(View v) 
	{
		this((Activity)v.getContext());
	}	
	
	public DualProgressDialog(Activity context) 
	{
		super(context);
		this.context = context;
		this.init();
	}
	
    @Override
    public void show() 
    {
    	super.show();
        if(fullScreen) 
        {	
    		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    		Window window = this.getWindow();
    		lp.copyFrom(window.getAttributes());
    		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
    		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
    		window.setAttributes(lp);
    	}
    }
    
	private void init()
	{		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lc_dualprogress_dialog);
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
		titleTextView = (TextView)findViewById(R.id.DPBD_message);
		progressBarA = (ProgressBar)findViewById(R.id.DPBD_progressBarA);
		progressBarATextView = (TextView)findViewById(R.id.DPBD_progressBarAText);
		progressBarB = (ProgressBar)findViewById(R.id.DPBD_progressBarB);
		progressBarBTextView = (TextView)findViewById(R.id.DPBD_progressBarBText);
		verboseTextView = (TextView)findViewById(R.id.DPBD_verbose);
		okButton = (Button)findViewById(R.id.DPBD_buttonOK);
		progressBarBContainer = (LinearLayout)findViewById(R.id.DPBD_progressBarBContainer);
		verboseContainer = (LinearLayout)findViewById(R.id.DPBD_verboseContainer);
		buttonWrapper = (LinearLayout)findViewById(R.id.DPBD_buttonWrapper);
		verboseSV = (ScrollView)findViewById(R.id.DPBD_verboseScroll);
		
		
		okButton.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	cancel();
		    }
	    });
	}
	
	public boolean isFinished()
	{
		return finish;
	}
    
	public void hideProgressBarB(boolean hide)
	{
		if(hide) progressBarBContainer.setVisibility(View.GONE);
		else progressBarBContainer.setVisibility(View.VISIBLE);
	}
    
	public void hideVerboseView(boolean hide)
	{
		if(hide){
			verboseContainer.setVisibility(View.GONE);
			buttonWrapper.setVisibility(View.GONE);
		}
		else {
			verboseContainer.setVisibility(View.VISIBLE);
			buttonWrapper.setVisibility(View.VISIBLE);
		}
	}
	
	public void setFullScreen(boolean fullScreen)
	{
		this.fullScreen = fullScreen;
	}
	
	public void setText(String text)
	{
		verboseTextView.setText(Html.fromHtml(text));
		scrollDown();
	}
	
	public void appendText(String text)
	{
		verboseTextView.append(Html.fromHtml(text));
		scrollDown();
	}
	
	public void appendTextRed(String text)
	{
		verboseTextView.append(Html.fromHtml("<font color='#FF0000'>" + text + "</font>"));
		scrollDown();
	}
	
	public void appendTextBlue(String text)
	{
		verboseTextView.append(Html.fromHtml("<font color='#0000AA'>" + text + "</font>"));
		scrollDown();
	}
	
	public void appendText(String text, int start, int end)
	{
		verboseTextView.append(text, start, end);
		scrollDown();
	}
	
	public void setEnabledButton(boolean enabled)
	{
		okButton.setEnabled(enabled);
		finish = enabled;
	}
	
	public void setMessage(Spanned text)
	{
		titleTextView.setText(text);
	}
	
	public void setMessage(String text)
	{
		titleTextView.setText(text);
	}
	
	public void setProgress(int progress)
	{
		progressA = progress;
		if(progress <= 0) this.setSecondaryProgress(0); //reset
		progressBarA.setSecondaryProgress(progress); //swap progress and secondary progress
		double progressRelative = 0;
		if(maxA > 0) progressRelative = ((double)progressA / maxA) * 100;
		progressBarATextView.setText((int)Math.round(progressRelative) + "%");
	}
	
	public void setSecondaryProgress(int secondaryProgress)
	{
		secondaryProgressA = secondaryProgress;
		progressBarA.setProgress(secondaryProgress); //swap progress and secondary progress
	}
	
	public void setProgressB(int progress)
	{
		progressB = progress;
		if(progress <= 0) progressBarB.setSecondaryProgress(0); //reset
		progressBarB.setSecondaryProgress(progress);
		progressBarBTextView.setText(progress + "/" + maxB);
	}
	
	public void setMax(int max)
	{
		maxA = max;
		progressBarA.setMax(max);
	}
	
	public void setMaxB(int max)
	{
		maxB = max;
		progressBarB.setMax(maxB);
		progressBarBTextView.setText(0 + "/" + maxB);
	}
	
	private void scrollDown()
	{
		verboseSV.post(new Runnable() {            
		    @Override
		    public void run() {
		    	verboseSV.fullScroll(View.FOCUS_DOWN);              
		    }
		});
	}
}
