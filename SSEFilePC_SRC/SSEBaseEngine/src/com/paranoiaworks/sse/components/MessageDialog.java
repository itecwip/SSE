package com.paranoiaworks.sse.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.paranoiaworks.sse.Helpers;

/**
 * Simple Alert Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.3
 */ 

@SuppressWarnings("serial")
public class MessageDialog extends JDialog {
    
	private Frame parentFrame = null;
	private JLabel iconL = null;
	private JLabel textL = null;
	JButton okButton = null;
	
	public static final int ICON_OK = 1;
	public static final int ICON_NEGATIVE = 2;
	public static final int ICON_INFO_RED = 3;
	public static final int ICON_INFO_BLUE = 4;
	
	private static final int MINIMAL_HEIGHT = 100;
	
	
	public MessageDialog(Frame frame, String title, int iconCode)
    {
        super(frame, title, true);
        this.parentFrame = frame;
        
        Image image = null;       
        switch (iconCode) 
        {        
        	case ICON_OK:
        		image = Helpers.loadImageFromResource(MessageDialog.class, "ok_icon", "png", "rescore/");
            	break;
        	case ICON_NEGATIVE:
        		image = Helpers.loadImageFromResource(MessageDialog.class, "cancel_icon", "png", "rescore/");
            	break;
        	case ICON_INFO_RED:
        		image = Helpers.loadImageFromResource(MessageDialog.class, "info_icon_red", "png", "rescore/");
            	break;
        	default: 
        		image = Helpers.loadImageFromResource(MessageDialog.class, "info_icon_blue", "png", "rescore/");
            	break;
        }
        
        if(image != null) {
	        Icon icon = new ImageIcon(image);
	        iconL = new JLabel(icon);
        }
        else {
        	iconL = new JLabel();
        }
        
        iconL.setBorder(new EmptyBorder(14, 15, 10, 15));
        
        textL = new JLabel();
        textL.setBorder(new EmptyBorder(5, 5, 5, 20));
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        buttonPane.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 30));
        okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				MessageDialog.this.dispose();
			}
		});
        buttonPane.add(okButton, gbc);
               
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(400, MINIMAL_HEIGHT));

        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setLocationRelativeTo(frame);     
        
        this.add(iconL, BorderLayout.LINE_START);
        this.add(textL, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }
	
    public synchronized void setText(String text) 
    {
    	textL.setText(text);
    }
    
    public synchronized void setMinimumWidth(int width) 
    {
    	this.setMinimumSize(new Dimension(width, MINIMAL_HEIGHT));
    }
	
    public synchronized void showDialog() 
    {
    	this.pack();
    	this.setLocationRelativeTo(parentFrame);
    	this.setVisible(true);
    }
}
