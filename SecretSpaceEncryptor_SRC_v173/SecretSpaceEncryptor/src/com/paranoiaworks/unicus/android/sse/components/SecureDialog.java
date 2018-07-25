package com.paranoiaworks.unicus.android.sse.components;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;

/**
 * Dialog with screen-shot taking protection
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */

public class SecureDialog extends Dialog {
	
	static private boolean protectionActive = true;

	public SecureDialog(Context context) {
		super(context);
		if(protectionActive)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
	}

	protected SecureDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		if(protectionActive)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
	}
	
	public SecureDialog(Context context, int themeResId) {
		super(context, themeResId);
		if(protectionActive)
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
	}

	public static void setProtectionActive(boolean active) {
		protectionActive = active;
	}
}
