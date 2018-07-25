package com.paranoiaworks.unicus.android.sse.components;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;

/**
 * WebView based Alert Dialog with "Don't Show Again" option
 * 
 * @author Paranoia Works
 * @version 1.0.3
 */
public class HtmlAlertDialog extends SecureDialog {

	private Context context;
	private WebView webView;
	private Button okButton;
	private CheckBox showAgainCB;
	
	private boolean willBeShown = true;
	
	private String path;
	private String title = null;
	
	private int messageCode = -1;
	
	public HtmlAlertDialog(View v, String path, String title) 
	{
		this((Activity)v.getContext(), path, title);
	}	
	
	public HtmlAlertDialog(Activity context, String path, String title) 
	{
		super(context);
		this.context = context;
		this.path = path;
		this.title = title;
		init();
	}
	
	public boolean getWillBeShown()
	{
		return willBeShown;
	}
	
	public void setMessageCode(int messageCode)
	{
		this.messageCode = messageCode;
	}
	
	public void hideDontShowAgainCheckBox()
	{
		if(showAgainCB != null) showAgainCB.setVisibility(CheckBox.GONE);
	}
	
    @Override
    public void show() {
    	if(willBeShown) 
    	{
    		super.show();
    		
    		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    		Window window = this.getWindow();
    		lp.copyFrom(window.getAttributes());
    		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
    		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
    		window.setAttributes(lp);
    	}
    }

	private void init()
	{		
    	Set<String> dontShowTest = (Set<String>)SettingDataHolder.getInstance().getPersistentDataObject("ALERT_DONTSHOW");
    	final Set<String> dontShow = (dontShowTest != null) ? dontShowTest : new HashSet<String>();
    	final String alertName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
    	
    	if(!dontShow.contains(alertName))
    		willBeShown = true;
    	else 
    	{
    		willBeShown = false;
    		return;
    	}
		
		if(title == null) this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		else {
			this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			this.setTitle(title);			
		}	
		this.setContentView(R.layout.lc_html_alert_dialog);
		if(title != null) this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.info_icon_large);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);	
		
		webView = (WebView)findViewById(R.id.HAD_webView);
        webView.setWebViewClient(new AlteredWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
		
		showAgainCB = (CheckBox)findViewById(R.id.HAD_checkBox);
		showAgainCB.setText("  " + context.getResources().getString(R.string.common_dontShowThisAgain));
		okButton = (Button)findViewById(R.id.HAD_okButton);
		okButton.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	if(showAgainCB.isChecked())
		    	{
    				dontShow.add(alertName);
    				SettingDataHolder sdh = SettingDataHolder.getInstance();
    				sdh.addOrReplacePersistentDataObject("ALERT_DONTSHOW", dontShow);
    				sdh.save();
		    	}
		    	
		    	CryptActivity ca = (CryptActivity)context;
        		ActivityMessage am = new ActivityMessage(messageCode, path, !showAgainCB.isChecked());
        		ca.setMessage(am);
				cancel();
				return;
		    }
	    });
		
		webView.loadUrl(path);
    }  
	
    final class AlteredWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && (url.startsWith("http://") || url.startsWith("https://")) ) {
                view.getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            } else {
                return false;
            }
        }     
    }
}
