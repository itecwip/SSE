package com.paranoiaworks.sse.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

/**
 * JPasswordField with Placeholder
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */ 

@SuppressWarnings("serial")
public class PlaceholderPasswordField extends JPasswordField {

    private String placeholder;

    public PlaceholderPasswordField() {
    }

    public PlaceholderPasswordField(final Document pDoc, final String pText, final int pColumns)
    {
        super(pDoc, pText, pColumns);
    }

    public PlaceholderPasswordField(final int pColumns) {
        super(pColumns);
    }

    public PlaceholderPasswordField(final String pText) {
        super(pText);
    }

    public PlaceholderPasswordField(final String pText, final int pColumns) {
        super(pText, pColumns);
    }

    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(final String s) {
        placeholder = s;
    }	

    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g2d = (Graphics2D) pG;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getDisabledTextColor());
        g2d.drawString(this.isEnabled() ? placeholder : " ", getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}
