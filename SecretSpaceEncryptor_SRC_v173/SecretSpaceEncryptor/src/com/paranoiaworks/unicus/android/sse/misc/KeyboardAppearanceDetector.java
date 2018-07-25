package com.paranoiaworks.unicus.android.sse.misc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * Modified class KeyboardAppearanceDetector (original by Francesco Verheye)
 *
 * @version 1.0.0
 */
public class KeyboardAppearanceDetector implements OnGlobalLayoutListener {
	private Activity activity;
	private View activityRootView;
	private boolean wasOpened = false;
	private final Rect r = new Rect();
	
	private int lastOrientation;

	private KeyboardVisibilityChangedListener listener;

	// Constructor
	public KeyboardAppearanceDetector(Activity activity) {
		this.activity = activity;
	}
	
	public boolean isOpen()
	{
		return wasOpened;
	}

	// Set listener
	public void setKeyboardVisibilityChangedListener(
			KeyboardVisibilityChangedListener listener) {
		this.listener = listener;
	}

	// Start
	public void startDetection() {
		lastOrientation = activity.getResources().getConfiguration().orientation;
		activityRootView = ((ViewGroup) activity
				.findViewById(android.R.id.content)).getChildAt(0);

		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	// Stop
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void stopDetection() {		
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				activityRootView.getViewTreeObserver()
						.removeGlobalOnLayoutListener(this);
			} else {
				activityRootView.getViewTreeObserver()
						.removeOnGlobalLayoutListener(this);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	// OnGlobalLayoutListener
	@Override
	public void onGlobalLayout() {
		activityRootView.getWindowVisibleDisplayFrame(r);

		int height = activityRootView.getRootView().getHeight();
		int width = activityRootView.getRootView().getWidth();
		int orientation = activity.getResources().getConfiguration().orientation;
		
		if(lastOrientation != orientation)
		{
			lastOrientation = orientation;
			return;
		}
		lastOrientation = orientation;
		
		double heightDiff = height - (r.bottom - r.top);
		boolean isOpen = heightDiff/height > 0.25; // more than 1/4 of screen
		if (isOpen == wasOpened) {
			// Ignoring global layout change...
			return;
		}

		wasOpened = isOpen;

		if (listener != null) {
			listener.onKeyboardVisibilityChanged(isOpen);
		}
	}

	// Listener to notify keyboard visibility changements
	public interface KeyboardVisibilityChangedListener {
		public void onKeyboardVisibilityChanged(boolean isKeyboardVisible);
	}
	
    private float pxToDp(int px)
    {
    	float scale = activity.getResources().getDisplayMetrics().density;
    	return ((float)px - 0.5f) / scale;
    }
}
