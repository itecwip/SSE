package com.paranoiaworks.sse.components;

import java.awt.Color;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import com.paranoiaworks.sse.Helpers;

import ext.AnimatedIcon;

/**
 * Wait Dialog (spinner)
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */ 

@SuppressWarnings("serial")
public class WaitDialog extends JDialog {
	
	private JLabel label = null;
	AnimatedIcon icon = null;
	
    public WaitDialog(Frame frame, String title)
    {
        super(frame, title, true);
        this.setUndecorated(true);
        
        label = new JLabel();
        icon = new AnimatedIcon(label, 70);

        icon.addIcon(new ImageIcon(Helpers.loadImageFromResource(WaitDialog.class, "wait_01", "png", "rescore/wait_spinner/")));
        icon.addIcon(new ImageIcon(Helpers.loadImageFromResource(WaitDialog.class, "wait_02", "png", "rescore/wait_spinner/")));
        icon.addIcon(new ImageIcon(Helpers.loadImageFromResource(WaitDialog.class, "wait_03", "png", "rescore/wait_spinner/")));
        icon.addIcon(new ImageIcon(Helpers.loadImageFromResource(WaitDialog.class, "wait_04", "png", "rescore/wait_spinner/")));
        icon.addIcon(new ImageIcon(Helpers.loadImageFromResource(WaitDialog.class, "wait_05", "png", "rescore/wait_spinner/")));
        
        label.setIcon(icon);
        label.setBorder(new EmptyBorder(5, 5, 5, 8));
        
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, 
        		new Color (220, 220, 220), new Color (190, 190, 190), new Color (107, 107, 107), new Color (154, 154, 154)));
        this.setContentPane(panel);
        
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.getContentPane().setBackground(new Color (220, 220, 220));
        this.getContentPane().add(label);
        label.setText("<html><font size='4'>&nbsp;" + title + "</font></html>");
        this.pack();
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setLocationRelativeTo(frame);
    }
    
    public synchronized void showDialog() 
    {
    	icon.start();
    	this.setVisible(true);
    }
    
    @Override
    public void dispose() 
    {
        icon.stop();
        super.dispose();
    } 
}
