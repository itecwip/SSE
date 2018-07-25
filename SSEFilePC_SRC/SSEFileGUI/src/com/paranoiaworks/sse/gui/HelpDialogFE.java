package com.paranoiaworks.sse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.paranoiaworks.sse.Helpers;
import com.paranoiaworks.sse.dao.SettingsDataHolder;

import ext.BareBonesBrowserLaunch;

/**
 * Help Dialog (single purposed - File Encryptor)
 * 
 * @author Paranoia Works
 * @version 1.0.2
 */ 


@SuppressWarnings("serial")
public class HelpDialogFE extends JDialog implements HyperlinkListener {
    
	private Frame parentFrame = null;
	private JButton proUpdateButton; 
	private final ResourceBundle textBundle;
	private final SettingsDataHolder sdh;
	private final String publishYear;
	private final String appVersionCode;
	private boolean showProUpdate = false;
	
	public HelpDialogFE(Frame frame, ResourceBundle textBundle, SettingsDataHolder sdh, String appVersionCode, String publishYear)
    {
        super(frame, textBundle.getString("ssefilegui.label.HelpDialog"), true);
        this.parentFrame = frame;
        this.textBundle = textBundle;
        this.sdh = sdh;
        this.appVersionCode = appVersionCode;
        this.publishYear = publishYear;       
        init();
    }
	
	public synchronized void showDialog() 
    {
    	this.setLocationRelativeTo(parentFrame);
    	this.setVisible(true);	
    }
    
    private void init() 
    {       
		this.setLayout(new BorderLayout());
		int helpDialogWidth = Integer.parseInt(textBundle.getString("ssefilegui.spec.helpWidth"));
		int helpDialogHeight = Integer.parseInt(textBundle.getString("ssefilegui.spec.helpHeight"));
		int helpLogoPositionY = Integer.parseInt(textBundle.getString("ssefilegui.spec.helpLogoY"));
		this.setMinimumSize(new Dimension(helpDialogWidth, helpDialogHeight));	
		this.setResizable(false);
		this.setLocationByPlatform(true);
		this.setLocationRelativeTo(this);
	
		JPanel buttonsPane = new JPanel(new BorderLayout());
		buttonsPane.setBorder(new EmptyBorder(4, 2, 4, 2));
		final JTextPane textArea = new JTextPane();
		textArea.setLayout(null);
		textArea.setEditable(false); 
		textArea.setContentType("text/html");
		textArea.addHyperlinkListener(this); 
		textArea.setMargin(new Insets(0,0,0,0));
        JScrollPane scrollText = new JScrollPane(textArea);
        scrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
        try {
        	InputStream is = getClass().getResourceAsStream("/res/" + textBundle.getString("ssefilegui.spec.helpFile"));            
        	String htmlString = Helpers.loadStringFromInputStream(is);
            htmlString = htmlString.replaceAll("icon.png", this.getClass().getResource("/res/1x1_blank.png").toString());
            //htmlString = htmlString.replaceAll("line.png", this.getClass().getResource("/res/line.png").toString());
            htmlString = htmlString.replaceAll("!version!", appVersionCode);
            htmlString = htmlString.replaceAll("!year!", publishYear);
            
    	    HTMLEditorKit kit = new HTMLEditorKit();
    	    HTMLDocument doc = new HTMLDocument();
    	    doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
    	    textArea.setEditorKit(kit);
    	    textArea.setDocument(doc);
    	    kit.insertHTML(doc, doc.getLength(), htmlString, 0, 0, null);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
        
		Image logo = Helpers.loadImageFromResource(HelpDialogFE.class, "icon", "png");
		ImageIcon icon = new ImageIcon(logo);
		JLabel logoWrapper = new JLabel(icon);
		logoWrapper.setBounds(12, helpLogoPositionY, 95, 95);
		textArea.add(logoWrapper);
		
		proUpdateButton = new JButton (textBundle.getString("ssefilegui.label.DonateButton"));
		proUpdateButton.setPreferredSize(new Dimension(100, 30));
		proUpdateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BareBonesBrowserLaunch.openURL(textBundle.getString("ssefilegui.url.Donate").trim());
			}
		});
		
		JButton visitHomeButton = new JButton (textBundle.getString("ssefilegui.label.VisitHomePageButton"));
		visitHomeButton.setPreferredSize(new Dimension(100, 30));
		visitHomeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BareBonesBrowserLaunch.openURL(textBundle.getString("ssefilegui.url.HomePage").trim());
			}
		});
		
		JButton okButton = new JButton ("OK");
		okButton.setPreferredSize(new Dimension(100, 30));
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				HelpDialogFE.this.setVisible(false);
			}
		});
		
		buttonsPane.add(proUpdateButton, BorderLayout.LINE_START);
		buttonsPane.add(visitHomeButton, BorderLayout.CENTER);
		buttonsPane.add(okButton, BorderLayout.LINE_END);
	
		this.add(scrollText, BorderLayout.CENTER);
		this.add(buttonsPane, BorderLayout.PAGE_END);
		
        EventQueue.invokeLater(new Runnable(){
        	public void run() 
        	{
        		textArea.setCaretPosition(0);
        	}
        });
    }
	
    public void setShowProUpdate(boolean show)
    {
    	this.showProUpdate = show;
    	if(!showProUpdate) proUpdateButton.setEnabled(false);
    }	
    
	public void hyperlinkUpdate(HyperlinkEvent event) 
	{
    	if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
    		try {
    			BareBonesBrowserLaunch.openURL(event.getURL().toString());
             } catch (Exception e) {
                 e.printStackTrace();
             }
    	}
	}
}
