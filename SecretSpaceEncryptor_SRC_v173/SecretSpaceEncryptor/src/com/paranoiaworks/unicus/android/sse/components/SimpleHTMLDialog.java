package com.paranoiaworks.unicus.android.sse.components;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;

/**
 * Simple HTML View Dialog
 * used for application "Quick Help"
 * 
 * @author Paranoia Works
 * @version 1.0.9
 * @related Help.js
 */
public class SimpleHTMLDialog extends SecureDialog {

	private WebView webView;
	private String scrollToToken = null;
	private boolean ignoreAnchorsInHistory = true;
	private Map<String, String> valuesMap = new HashMap<String, String>();
	
	private Context context;
	private Handler handler = new Handler();

	
	public SimpleHTMLDialog(View v) 
	{
		this((Activity)v.getContext());
	}	
	
	public SimpleHTMLDialog(Activity context) 
	{
		super(context, R.style.TransparentDialog);
		this.context = context;
		init();
	}
	
	public void loadURL(String url)
	{	
		webView.loadUrl(url);
	}
	
	public void addValue(String key, String value)
	{	
		valuesMap.put(key, value);
	}
	
	public void ignoreAnchorsInHistory(boolean ignore)
	{
		this.ignoreAnchorsInHistory = ignore;
	}
	
	private void init()
	{		
		this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lc_simple_html_dialog);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.dimAmount=0.6f;  
		this.getWindow().setAttributes(lp);  
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		View bExit = (View)findViewById(R.id.SHD_exit);
		webView = (WebView)findViewById(R.id.SHD_webView);
        webView.setWebViewClient(new AlteredWebViewClient());
        webView.setWebChromeClient(new AlteredWebChromeClient());
        webView.addJavascriptInterface(new SimpleJavaScriptInterface(), "SJSI");
			
		WebSettings webSettings = webView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
              
        bExit.bringToFront();
        bExit.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v)
		    {
		    	cancel();
		    }
	    });
    }

    final class SimpleJavaScriptInterface 
    {
    	@JavascriptInterface
    	public void gotoURL(final String url) 
        {
        	gotoURL(url, null);
        }
    	
    	@JavascriptInterface
    	public void gotoURL(final String url, final String scrollToElement) 
        {
            handler.post(new Runnable() {
                public void run() {
                	if(scrollToElement != null) scrollToToken = scrollToElement;
                	webView.loadUrl("javascript:loadURL('" + url + "')");
                }
            });
        }
        
    	@JavascriptInterface
    	public void scrollToXY(final String x, final String y, final String height)
        {
            handler.post(new Runnable() {
                public void run() {
                	double multiplier = (double)webView.getBottom() / Double.parseDouble(height);
                	webView.scrollTo(Integer.parseInt(x), (int)(Double.parseDouble(y) * multiplier));
                }
            });
        }
    	
    	@JavascriptInterface
    	public void sendMessageAndClose(final String message)
        {
    		CryptActivity ca = (CryptActivity)context;
    		ActivityMessage am = new ActivityMessage(0, message, null);
    		ca.setMessage(am);
			cancel();
        }
    	
        @JavascriptInterface
        public String getValue(String key) {
            String value = valuesMap.get(key);
        	if(value == null) value = "null";
            return value;
        }
    }

    final class AlteredWebChromeClient extends WebChromeClient
    {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            //SSElog.d("SimpleHTMLDialog: onJsAlert", message);
        	result.confirm();
            return true;
        }
        
        @Override
        public void onProgressChanged(WebView view, int newProgress)
        {
        	if(newProgress == 100)
        	{
        		webView.requestFocus();
        		
                handler.post(new Runnable() {
                    public void run() {
                    	if(scrollToToken != null) webView.loadUrl("javascript:scrollToElement('" + scrollToToken + "')");
                    	//else scrollView.scrollTo(0, 0);
                    	scrollToToken = null;
                    }
                });     		
        	}
        	super.onProgressChanged(view, newProgress);
        }
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
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else {
        	return super.onKeyDown(keyCode, event);
        }
    }
    
    @Override
    public void onBackPressed()
    {
    	if(!webView.canGoBack()) {
    		super.onBackPressed();
    	}
    	else
    	{
	    	int backIndex = 1;
	    	WebBackForwardList wbfl = webView.copyBackForwardList();
	    	
	    	if(ignoreAnchorsInHistory)
	    	{
		    	while(wbfl.getCurrentIndex() - backIndex > 0)
		    	{	
			    	String backUrl = removeAnchor(wbfl.getItemAtIndex(wbfl.getCurrentIndex() - backIndex).getUrl());
			    	if(backUrl.equals(removeAnchor(wbfl.getCurrentItem().getUrl()))) {
			    		++backIndex;
			    	}
			    	else break;
		    	}
	    	}
	    	
	    	if(backIndex > 1)
	    	{
	    		if(removeAnchor(wbfl.getItemAtIndex(wbfl.getCurrentIndex() - backIndex).getUrl()).indexOf(removeAnchor(wbfl.getCurrentItem().getUrl())) < 0)
	    			backIndex -=1;
	    	}
	    	if(wbfl.getCurrentIndex() - backIndex < 0) super.onBackPressed();
	    	else 
	    	{
	    		webView.goBackOrForward(-backIndex);
	    	}
    	}
    }
    
    private static String removeAnchor(String url)
    {
    	int index = url.lastIndexOf('#');
    	if (index > -1) {
    		url = url.substring(0, index);
    	}
    	return url;
    }
}
