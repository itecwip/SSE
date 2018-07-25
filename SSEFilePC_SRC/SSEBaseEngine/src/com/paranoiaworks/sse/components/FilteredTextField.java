package com.paranoiaworks.sse.components;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Filtered Text Field 
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */ 

@SuppressWarnings("serial")
public class FilteredTextField extends JTextField {

	private String allowedChars = null;
	private Integer maxChars = null; 
	
	/** set allowed characters + max text length (both can be null = not applied) */
	public void setAllowedChars(String chars, Integer maxChars) {
		this.allowedChars = chars;
		this.maxChars = maxChars;
	}
	
	@Override
	protected Document createDefaultModel() {
	    return new FilterDocument();
	}
	
	class FilterDocument extends PlainDocument
	{
	    @Override
	    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException 
	    {
	        if (maxChars != null && getLength() + str.length() > maxChars) {
	            return;
	        }
	    	
	    	if(allowedChars == null) {
	    		 super.insertString(offs, str, a);
	    	}
	    	
	    	for(int i = 0; i < str.length(); ++i) {
	    		if(allowedChars.indexOf(str.charAt(i)) < 0) return;
	    	}
	    	
	    	super.insertString(offs, str, a);
	    }
	}
}