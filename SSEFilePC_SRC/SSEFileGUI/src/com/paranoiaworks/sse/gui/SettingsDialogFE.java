package com.paranoiaworks.sse.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.paranoiaworks.sse.Helpers;
import com.paranoiaworks.sse.components.MessageDialog;
import com.paranoiaworks.sse.dao.SettingsDataHolder;

/**
 * Settings Dialog (single purposed - File Encryptor)
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */ 


@SuppressWarnings("serial")
public class SettingsDialogFE extends JDialog {
    
	private Frame parentFrame = null;
	private JButton okButton = null;
	private ResourceBundle textBundle;
	private SettingsDataHolder sdh;
	
	private static final int MIN_WIDTH = 300;
	private static final int HEIGHT = 100;
	private static final int LINE_TITLE_FONTSIZE = 14;
	private static final int LINE_CURRENT_FONTSIZE = 12;
	
	public SettingsDialogFE(Frame frame, ResourceBundle textBundle, SettingsDataHolder sdh)
    {
        super(frame, textBundle.getString("ssefilegui.settings.settingsTitle"), true);
        this.parentFrame = frame;
        this.textBundle = textBundle;
        this.sdh = sdh;
        init();
    }
	
	public synchronized void showDialog() 
    {
    	this.pack();
    	this.setLocationRelativeTo(parentFrame);
    	this.setVisible(true);	
    }
    
    private void init() 
    {       
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        buttonPane.setBorder(new EmptyBorder(16, 8, 16, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
               
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setMinimumSize(new Dimension(MIN_WIDTH, HEIGHT));

        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setLocationRelativeTo(parentFrame);   

        // Encrypted files directory
        JLabel outputEncDirTitleL = new JLabel(textBundle.getString("ssefilegui.settings.outputEncDir"));
        final JLabel outputEncDirCurrentL = new JLabel();
        fillCurrentPathLabel(outputEncDirCurrentL, null);
        final JCheckBox outputEncDirCB = new JCheckBox();        
        JPanel rowEncDir = makeRow(outputEncDirTitleL, outputEncDirCurrentL, outputEncDirCB);
        outputEncDirCB.addActionListener(new ActionListener() 
        {	
        	public void actionPerformed(ActionEvent evt)  
			{
        		if(outputEncDirCB.isSelected()) {			 
        			chooseDir(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_PATH, 
        					getPath(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_PATH),
        					outputEncDirCurrentL, outputEncDirCB);
        		}
        		else { 
        			fillCurrentPathLabel(outputEncDirCurrentL, null);
        		}
			}
        });
        Boolean encDirEnabled = sdh.getPersistentDataBoolean(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_ENABLED);
        String encDirPath = sdh.getPersistentDataString(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_PATH);
        File encDirFile = encDirPath != null ? new File(encDirPath) : null;        
        if(encDirEnabled != null && encDirEnabled && encDirFile != null && encDirFile.canWrite())
        {
        	outputEncDirCB.setSelected(true);
        	fillCurrentPathLabel(outputEncDirCurrentL, encDirFile.getAbsolutePath());
        }
        
        // Decrypted files directory
        JLabel outputDecDirTitleL = new JLabel(textBundle.getString("ssefilegui.settings.outputDecDir"));
        final JLabel outputDecDirCurrentL = new JLabel();
        fillCurrentPathLabel(outputDecDirCurrentL, null);
        final JCheckBox outputDecDirCB = new JCheckBox();
        JPanel rowDecDir = makeRow(outputDecDirTitleL, outputDecDirCurrentL, outputDecDirCB);
        outputDecDirCB.addActionListener(new ActionListener() 
        {	
        	public void actionPerformed(ActionEvent evt)  
			{
        		if(outputDecDirCB.isSelected()) {			 
        			chooseDir(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_PATH, 
        					getPath(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_PATH),
        					outputDecDirCurrentL, outputDecDirCB);
        		}
        		else { 
        			fillCurrentPathLabel(outputDecDirCurrentL, null);
        		}
			}
        });
        Boolean decDirEnabled = sdh.getPersistentDataBoolean(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_ENABLED);
        String decDirPath = sdh.getPersistentDataString(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_PATH);
        File decDirFile = decDirPath != null ? new File(decDirPath) : null;        
        if(decDirEnabled != null && decDirEnabled && decDirFile != null && decDirFile.canWrite())
        {
        	outputDecDirCB.setSelected(true);
        	fillCurrentPathLabel(outputDecDirCurrentL, decDirFile.getAbsolutePath());
        }
        
        okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 30));
        okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(outputEncDirCB.isSelected()) {
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_ENABLED, true);
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_PATH, outputEncDirCurrentL.getName());
				}
				else {
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_ENC_ENABLED, false);
				}
				
				if(outputDecDirCB.isSelected()) {
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_ENABLED, true);
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_PATH, outputDecDirCurrentL.getName());
				}
				else {
					sdh.addOrReplacePersistentDataObject(SSEFileGUI.FE_SETTINGS_OUTPUTDIR_DEC_ENABLED, false);
				}
				
				try {
					sdh.save();
				} catch (Exception e1) {
					showErrorDialog(Helpers.getShortenedStackTrace(e1, 1));
				}				 
				
				SettingsDialogFE.this.dispose();
			}
		});
        buttonPane.add(okButton, gbc);
        
         JPanel settingsPane = new JPanel();
         settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.Y_AXIS));
         settingsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
         settingsPane.add(rowEncDir);
         settingsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
         settingsPane.add(rowDecDir);
         settingsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        this.add(settingsPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
    }
	
	private JPanel makeRow(JLabel title, JLabel current, JCheckBox checkBox) 
	{
		title.setFont(new Font(title.getFont().getFamily(), Font.BOLD, LINE_TITLE_FONTSIZE));
		current.setFont(new Font(current.getFont().getFamily(), Font.PLAIN, LINE_CURRENT_FONTSIZE));
		checkBox.setBorder(new EmptyBorder(0, 20, 0, 0));
		
		JPanel rowL =  new JPanel(new BorderLayout());
        rowL.add(title, BorderLayout.NORTH);
        rowL.add(current, BorderLayout.CENTER);
        JPanel row =  new JPanel(new BorderLayout());
        row.add(rowL, BorderLayout.LINE_START);
        row.add(checkBox, BorderLayout.LINE_END);
        row.setBorder(new EmptyBorder(8, 10, 8, 15));
        
        return row;	
	}
	
	private void fillCurrentPathLabel(JLabel label, String path) 
	{
		if(path != null) {
			String ellipsized = ellipsizeMiddle(path, 45, 45);
			label.setText("<html><b>" + textBundle.getString("ssefilegui.settings.current") + ": </b>" + ellipsized + "</html>");
			label.setName(path);
			if(!ellipsized.equals(path)) label.setToolTipText(path);
	    	
			this.pack();
	    	this.setLocationRelativeTo(parentFrame);
		}
		else {
			label.setText("<html><b>" + textBundle.getString("ssefilegui.settings.current") + ": </b>(" + textBundle.getString("ssefilegui.settings.sameAsSource") + ")</html>");
			label.setName("");
			label.setToolTipText(null);
		}
	}
	    
    private void chooseDir(String mode, String startPath, JLabel currentLabel, JCheckBox checkBox)
    {   	
    	JFileChooser dirChooser = new JFileChooser(startPath);
    	dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	dirChooser.setAcceptAllFileFilterUsed(false);

    	int result = dirChooser.showOpenDialog(this);
    	if(result == JFileChooser.APPROVE_OPTION) 
		{
			File dir = dirChooser.getSelectedFile();
			
			if(Helpers.writeTestFile(dir)) {				
				sdh.addOrReplacePersistentDataObject(mode, dir.getAbsolutePath());
				fillCurrentPathLabel(currentLabel, dir.getAbsolutePath());
			}
			else {			
				checkBox.setSelected(false);
				showErrorDialog(textBundle.getString("ssefilegui.text.DirIsReadonly")); 
			}
		} 
		else if(result == JFileChooser.CANCEL_OPTION) 
		{
			checkBox.setSelected(false);
		}
    }
    
    private String getPath(String code) 
    {
    	String path = null; 
		try {
			path = sdh.getPersistentDataString(code);
			if(path == null || !(new File(path).exists())) path = System.getProperty("user.home");
			if(path == null || !(new File(path).exists())) path = null;
		} catch (Exception e) {
			// swallow
		}
		
		if(path == null) path = ".";
		
		return path;
    }
    
    private static String ellipsizeMiddle(String text, int startChars, int endChars)
    {
    	String regex = "(.{" + startChars + "}).+(.{" + endChars + "})";   	
    	return text.replaceFirst(regex, "$1...$2");
    }
    
    private void showErrorDialog(String message)
    {
    	final MessageDialog messageDialog = new MessageDialog(SettingsDialogFE.this.parentFrame, "", MessageDialog.ICON_NEGATIVE);
    	messageDialog.setText("<html>" + message + "</html>");
    	messageDialog.showDialog();
    }
}
