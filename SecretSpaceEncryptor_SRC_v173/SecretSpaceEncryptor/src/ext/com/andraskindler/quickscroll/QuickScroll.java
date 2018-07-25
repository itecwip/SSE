package ext.com.andraskindler.quickscroll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ExpandableListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Interface required for FastTrack.
 * 
 * @author andraskindler
 *
 * @modified by Paranoia Works
 *
 */
public class QuickScroll extends View {

    // IDs
    protected static final int ID_PIN = 512;
    protected static final int ID_PIN_TEXT = 513;
    // type statics
    public static final int TYPE_POPUP = 0;
    public static final int TYPE_INDICATOR = 1;
    public static final int TYPE_POPUP_WITH_HANDLE = 2;
    public static final int TYPE_INDICATOR_WITH_HANDLE = 3;
    // style statics
    public static final int STYLE_NONE = 0;
    public static final int STYLE_HOLO = 1;
    // base colors
    public static final int GREY_DARK = Color.parseColor("#e0585858");
    public static final int GREY_LIGHT = Color.parseColor("#f0888888");
    public static final int GREY_SCROLLBAR = Color.parseColor("#64404040");
    public static final int BLUE_LIGHT = Color.parseColor("#FF33B5E5");
    public static final int BLUE_LIGHT_SEMITRANSPARENT = Color.parseColor("#8033B5E5");
    protected static final int SCROLLBAR_MARGIN = 10;
    private static final int HANDLERBAR_HEIGHT = 55;
    private static final int HANDLERBAR_WIDTH = 22;
    // base variables
    protected boolean isScrolling;
    protected AlphaAnimation fadeInAnimation, fadeOutAnimation;
    protected TextView scrollIndicatorTextView;
    protected Scrollable scrollable;
    protected ListView listView;
    protected int groupPosition;
    protected int itemCount;
    protected int type;
    protected boolean isInitialized = false;
    protected static final int TEXT_PADDING = 4;
    private double minAllVsVisibleRatio = 1.1;
    private int itemsOnPage = -1;
    private float lastHeight = -1;
    private int lastPosition = -1;
    private int itemsOnPageB = 0;
    // handlebar variables
    protected View handleBar;
    // indicator variables
    protected RelativeLayout scrollIndicator;

    // default constructors
    public QuickScroll(Context context) {
        super(context);
    }

    public QuickScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QuickScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initializing the QuickScroll, this function must be called.
     * <p/>
     *
     * @param type       the QuickScroll type. Available inputs: <b>QuickScroll.TYPE_POPUP</b> or <b>QuickScroll.TYPE_INDICATOR</b>
     * @param list       the ListView
     * @param scrollable the adapter, must implement Scrollable interface
     */
    public void init(final int type, final ListView list, final Scrollable scrollable, final int style) {
        if (isInitialized) return;

        this.type = type;
        listView = list;
        this.scrollable = scrollable;
        groupPosition = -1;
        fadeInAnimation = new AlphaAnimation(.0f, 1.0f);
        fadeInAnimation.setFillAfter(true);
        fadeOutAnimation = new AlphaAnimation(1.0f, .0f);
        fadeOutAnimation.setFillAfter(true);
        fadeOutAnimation.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                isScrolling = false;
            }
        });
        isScrolling = false;

        listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (isScrolling && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN)) {
                    return true;
                }
                return false;
            }
        });

        final RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        final RelativeLayout container = new RelativeLayout(getContext());
        container.setBackgroundColor(Color.TRANSPARENT);
        containerParams.addRule(RelativeLayout.ALIGN_TOP, getId());
        containerParams.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
        container.setLayoutParams(containerParams);

        if (this.type == TYPE_POPUP || this.type == TYPE_POPUP_WITH_HANDLE) {
            scrollIndicatorTextView = new TextView(getContext());
            scrollIndicatorTextView.setTextColor(Color.WHITE);
            scrollIndicatorTextView.setVisibility(View.INVISIBLE);
            scrollIndicatorTextView.setGravity(Gravity.CENTER);
            final RelativeLayout.LayoutParams popupParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            popupParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            scrollIndicatorTextView.setLayoutParams(popupParams);
            setPopupColor(GREY_LIGHT, GREY_DARK, 1, Color.WHITE, 1);
            setTextPadding(TEXT_PADDING, TEXT_PADDING, TEXT_PADDING, TEXT_PADDING);
            container.addView(scrollIndicatorTextView);
        } else {
            scrollIndicator = createPin();
            scrollIndicatorTextView = (TextView) scrollIndicator.findViewById(ID_PIN_TEXT);
            (scrollIndicator.findViewById(ID_PIN)).getLayoutParams().width = 25;
            container.addView(scrollIndicator);
        }

        // setting scrollbar width
        getLayoutParams().width = dpToPx(37);
        scrollIndicatorTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);

        // scrollbar setup
        if (style != STYLE_NONE) {
            final RelativeLayout layout = new RelativeLayout(getContext());
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_LEFT, getId());
            params.addRule(RelativeLayout.ALIGN_TOP, getId());
            params.addRule(RelativeLayout.ALIGN_RIGHT, getId());
            params.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
            layout.setLayoutParams(params);
            
            final View scrollbar = new View(getContext());
            scrollbar.setBackgroundColor(Color.TRANSPARENT); // Scroll Bar
            final RelativeLayout.LayoutParams scrollBarParams = new RelativeLayout.LayoutParams(dpToPx(2), LayoutParams.MATCH_PARENT);
            scrollBarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            scrollBarParams.rightMargin = dpToPx(5);
            scrollBarParams.topMargin = SCROLLBAR_MARGIN;
            scrollBarParams.bottomMargin = SCROLLBAR_MARGIN;
            scrollbar.setLayoutParams(scrollBarParams);
            layout.addView(scrollbar);
            ViewGroup.class.cast(listView.getParent()).addView(layout);
            // creating the handlebar
            if (this.type == TYPE_INDICATOR_WITH_HANDLE || this.type == TYPE_POPUP_WITH_HANDLE) {
                //handleBar = new View(getContext());
                handleBar = new ImageView(getContext());
                setHandlebarColor(BLUE_LIGHT, BLUE_LIGHT, BLUE_LIGHT_SEMITRANSPARENT);
                final RelativeLayout.LayoutParams handleParams = new RelativeLayout.LayoutParams(dpToPx(HANDLERBAR_WIDTH), dpToPx(HANDLERBAR_HEIGHT));
                handleBar.setLayoutParams(handleParams);
                ((RelativeLayout.LayoutParams) handleBar.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                          
                ((ImageView)handleBar).setImageResource(R.drawable.fastscroll_slider);              
                layout.addView(handleBar);
                listView.setOnScrollListener(new OnScrollListener() {

                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    	
                    }
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    	chooseScrollType(visibleItemCount, totalItemCount);
                    	if (totalItemCount - visibleItemCount > 0) {
                    		moveHandlebar(getHeight() * firstVisibleItem / (totalItemCount - visibleItemCount), true);
                    		if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) moveScrollIndicator(getHeight() * firstVisibleItem / (totalItemCount - visibleItemCount));
                        }
                    }
                });
            }
        }
        scrollIndicatorTextView.setText("..");
        isInitialized = true;

        ViewGroup.class.cast(listView.getParent()).addView(container);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(!isInitialized) return false;
    	Adapter adapter = listView.getAdapter();

        if (adapter == null)
            return false;

        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }

        itemCount = adapter.getCount();
        if (itemCount == 0)
            return false;
        if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            if (type == TYPE_POPUP || type == TYPE_INDICATOR) {
                scrollIndicatorTextView.startAnimation(fadeOutAnimation);
            } else {
                if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE)
                    handleBar.setSelected(false);
                scrollIndicator.startAnimation(fadeOutAnimation);
            }
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) {
                    scrollIndicator.startAnimation(fadeInAnimation);
                    scrollIndicator.setPadding(0, 0, getWidth(), 0);
                } else
                    scrollIndicatorTextView.startAnimation(fadeInAnimation); scroll(event.getY());
                isScrolling = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                scroll(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
                if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE)
                    handleBar.setSelected(false);
                if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE)
                    scrollIndicator.startAnimation(fadeOutAnimation);
                else
                    scrollIndicatorTextView.startAnimation(fadeOutAnimation);
                return true;
            default:
                return false;
        }
    }

    @SuppressLint("NewApi")
    protected void scroll(final float height) {
    	
    	if(scrollable.hasDataChanged()) resetLastValues();
    	
    	if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE) {
            handleBar.setSelected(true);
        }
    	
        int position = (int) ((height / getHeight()) * itemCount);
        if (listView instanceof ExpandableListView) {
            final int groupPosition = ExpandableListView.getPackedPositionGroup(((ExpandableListView) listView).getExpandableListPosition(position));
            if (groupPosition != -1)
                this.groupPosition = groupPosition;
        }

        if (position < 0)
            position = 0;
        else if (position >= itemCount)
            position = itemCount - 1;
        
        if(itemCount <= 0) return;
        
        String text = scrollable.getIndicatorForPosition(position, groupPosition);
        String itemType = Character.toString(text.charAt(text.length() - 1));
        text = text.substring(0, text.indexOf(":"));       
             
        int tempItemsOnPage = (listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
        if(Math.abs(itemsOnPage - tempItemsOnPage) > 1) itemsOnPage = tempItemsOnPage;
        
        double heightPosition = height / getHeight();
        int positionCorrection = (int)Math.round((heightPosition * itemsOnPage));
        if(heightPosition >= 1 || heightPosition <= 0) positionCorrection = 0;
        
        double incrementFactor = (((double)Math.abs(lastHeight - height)) / getHeight()) / (0.9/itemCount);  
        
        
        if((lastHeight > height && lastPosition < position - positionCorrection) 
        		|| lastHeight < height && lastPosition > position - positionCorrection 
        			|| incrementFactor < 1) return;
        
        lastHeight = height;
        lastPosition = position - positionCorrection;
                   
        if(itemType.equalsIgnoreCase("Y"))
        	scrollIndicatorTextView.setTextColor(Color.parseColor("#ecc650"));
        else if(itemType.equalsIgnoreCase("W"))
        	scrollIndicatorTextView.setTextColor(Color.parseColor("#dddddd"));
        scrollIndicatorTextView.setText(text);
        
        listView.setSelection(scrollable.getScrollPosition(lastPosition, groupPosition));  
    }
    
    @SuppressLint("NewApi")
    protected void moveHandlebar(final float where, boolean corection) {
        float move = where;
        if(corection) move -= (dpToPx(HANDLERBAR_HEIGHT) * (where / getHeight()));
        if (move < SCROLLBAR_MARGIN)
            move = SCROLLBAR_MARGIN;
        else if (move > getHeight() - handleBar.getHeight() - SCROLLBAR_MARGIN)
            move = getHeight() - handleBar.getHeight() - SCROLLBAR_MARGIN;
        
        ViewHelper.setTranslationY(handleBar, move);
    }
    
    @SuppressLint("NewApi")
    protected void moveScrollIndicator(final float where) {    	
    	float move = where - (scrollIndicator.getHeight() * (where / getHeight()));

	    if (move < 0)
	        move = 0;
	    else if (move > getHeight() - scrollIndicator.getHeight())
	        move = getHeight() - scrollIndicator.getHeight();
	
	    ViewHelper.setTranslationY(scrollIndicator, move);
    }

    /**
     * Sets the fade in and fade out duration of the indicator; default is 150 ms.
     * <p/>
     *
     * @param millis the fade duration in milliseconds
     */
    public void setFadeDuration(long millis) {
        fadeInAnimation.setDuration(millis);
        fadeOutAnimation.setDuration(millis);
    }

    /**
     * Sets the indicator colors, when QuickScroll.TYPE_INDICATOR is selected as type.
     * <p/>
     *
     * @param background the background color of the square
     * @param tip        the background color of the tip triangle
     * @param text       the color of the text
     */
    public void setIndicatorColor(final int background, final int tip, final int text) {
        if (type == TYPE_INDICATOR || type == TYPE_INDICATOR_WITH_HANDLE) {
            ((Pin) scrollIndicator.findViewById(ID_PIN)).setColor(tip);
            scrollIndicatorTextView.setTextColor(text);
            scrollIndicatorTextView.setBackgroundColor(background);
        }
    }

    /**
     * Sets the popup colors, when QuickScroll.TYPE_POPUP is selected as type.
     * <p/>
     *
     * @param backgroundcolor the background color of the TextView
     * @param bordercolor     the background color of the border surrounding the TextView
     * @param textcolor       the color of the text
     */
    public void setPopupColor(final int backgroundcolor, final int bordercolor, final int borderwidthDPI, final int textcolor, float cornerradiusDPI) {

        final GradientDrawable popupbackground = new GradientDrawable();
        popupbackground.setCornerRadius(dpToPx(cornerradiusDPI));
        popupbackground.setStroke(dpToPx(borderwidthDPI), bordercolor);
        popupbackground.setColor(backgroundcolor);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            scrollIndicatorTextView.setBackgroundDrawable(popupbackground);
        else
            scrollIndicatorTextView.setBackground(popupbackground);

        scrollIndicatorTextView.setTextColor(textcolor);
    }

    /**
     * Sets the width and height of the TextView containing the indicatortext. Default is WRAP_CONTENT, WRAP_CONTENT.
     * <p/>
     *
     * @param widthDP  width in DP
     * @param heightDP height in DP
     */
    public void setSize(final int widthDP, final int heightDP) {
        scrollIndicatorTextView.getLayoutParams().width = dpToPx(widthDP);
        scrollIndicatorTextView.getLayoutParams().height = dpToPx(heightDP);
    }

    /**
     * Sets the padding of the TextView containing the indicatortext. Default is 4 dp.
     * <p/>
     *
     * @param paddingLeftDP   left padding in DP
     * @param paddingTopDP    top param in DP
     * @param paddingBottomDP bottom param in DP
     * @param paddingRightDP  right param in DP
     */
    public void setTextPadding(final int paddingLeftDP, final int paddingTopDP, final int paddingBottomDP, final int paddingRightDP) {
        scrollIndicatorTextView.setPadding(dpToPx(paddingLeftDP), dpToPx(paddingTopDP), dpToPx(paddingRightDP), dpToPx(paddingBottomDP));

    }

    /**
     * Turns on fixed size for the TextView containing the indicatortext. Do not use with setSize()! This mode looks good if the indicatortext length is fixed, e.g. it's always two characters long.
     * <p/>
     *
     * @param sizeEMS number of characters in the indicatortext
     */
    public void setFixedSize(final int sizeEMS) {
        scrollIndicatorTextView.setEms(sizeEMS);
    }

    /**
     * Set the textsize of the TextView containing the indicatortext.
     *
     * @param unit - use TypedValue statics
     * @param size - the size according to the selected unit
     */
    public void setTextSize(final int unit, final float size) {
        scrollIndicatorTextView.setTextSize(unit, size);
    }

    /**
     * Set the colors of the handlebar.
     *
     * @param inactive     - color of the inactive handlebar
     * @param activebase   - base color of the active handlebar
     * @param activestroke - stroke of the active handlebar
     */
    public void setHandlebarColor(final int inactive, final int activebase, final int activestroke) {
        if (type == TYPE_INDICATOR_WITH_HANDLE || type == TYPE_POPUP_WITH_HANDLE) {
            final float density = getResources().getDisplayMetrics().density;
            final GradientDrawable bg_inactive = 
            		new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[] {0xcd859fa9, 0xcdbdd7e1});
            bg_inactive.setCornerRadius(density * 2);
            //bg_inactive.setColor(inactive);
            bg_inactive.setStroke((int) (2 * density), activestroke);
            final GradientDrawable bg_active = new GradientDrawable();
            bg_active.setCornerRadius(density * 2);
            bg_active.setColor(activebase);
            bg_active.setStroke((int) (5 * density), activestroke);
            final StateListDrawable states = new StateListDrawable();
            states.addState(new int[]{android.R.attr.state_selected}, bg_active);
            states.addState(new int[]{android.R.attr.state_enabled}, bg_inactive);

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                handleBar.setBackgroundDrawable(states);
            else
                handleBar.setBackground(states);
        }
    }
    
    /** Minimum Ration of All/Visible items when quick scroll bar is activated */ 
    public void setMinAllVsVisibleRatio(double ratio)
    {
    	if(ratio < 1.1) ratio = 1.1;
    	minAllVsVisibleRatio = ratio;
    }

    protected RelativeLayout createPin() {
        final RelativeLayout pinLayout = new RelativeLayout(getContext());
        pinLayout.setVisibility(View.INVISIBLE);

        final Pin pin = new Pin(getContext());
        pin.setId(ID_PIN);
        final RelativeLayout.LayoutParams pinParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pinParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        pinParams.addRule(RelativeLayout.ALIGN_BOTTOM, ID_PIN_TEXT);
        pinParams.addRule(RelativeLayout.ALIGN_TOP, ID_PIN_TEXT);
        pin.setLayoutParams(pinParams);
        pinLayout.addView(pin);

        final TextView indicatorTextView = new TextView(getContext());
        indicatorTextView.setId(ID_PIN_TEXT);
        final RelativeLayout.LayoutParams indicatorParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        indicatorParams.addRule(RelativeLayout.LEFT_OF, ID_PIN);
        indicatorTextView.setLayoutParams(indicatorParams);
        indicatorTextView.setTextColor(Color.WHITE);
        indicatorTextView.setGravity(Gravity.CENTER);
        indicatorTextView.setBackgroundColor(GREY_LIGHT);
        pinLayout.addView(indicatorTextView);

        return pinLayout;
    }
    
    private void resetLastValues()
    {
        itemsOnPage = -1;
        lastHeight = -1;
        lastPosition = -1;
    }
    
	/** Scroll / Quick Scroll **/
	private void chooseScrollType(int visibleItemCount, int totalItemCount)
	{
		if(Math.abs(itemsOnPageB - visibleItemCount) > 1) itemsOnPageB = visibleItemCount;	
		
		if(itemsOnPageB <= 1 || totalItemCount <= 1) {
			if(getVisibility() == View.VISIBLE) activateQuickScroll(false);
			return;
		}		    		
		
		boolean activate = false;
		if(totalItemCount/(double)itemsOnPageB > minAllVsVisibleRatio) activate = true;
						
		if((activate && getVisibility() == View.VISIBLE) || (!activate && getVisibility() == View.INVISIBLE)) return;		
			
		activateQuickScroll(activate);			
	}
	
	private void activateQuickScroll(boolean activate)
	{
		if(activate) {
			setVisibility(View.VISIBLE);
			handleBar.setVisibility(View.VISIBLE);
			listView.setVerticalScrollBarEnabled(false); 
		}
		else {
			setVisibility(View.INVISIBLE);
			handleBar.setVisibility(View.INVISIBLE);
			listView.setVerticalScrollBarEnabled(true); 
		}
	}
    
    private int dpToPx(float dp)
    {
    	float scale = getResources().getDisplayMetrics().density;
    	return (int)(dp * scale + 0.5f);
    }
}
