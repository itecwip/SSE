package com.paranoiaworks.sse.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Simple Password Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */ 


@SuppressWarnings("serial")
public class SimplePasswordDialog extends JDialog {
    
	private Frame parentFrame = null;
	private JButton cancelButton = null;
	private JButton continueButton = null;
	
	private String p;
	private ResourceBundle textBundle;

	
	private static final int MIN_WIDTH = 100;
	private static final int MIN_HEIGHT = 100;
	
	public SimplePasswordDialog(Frame frame, String title) throws Exception
    {
        super(frame, title, true);
        this.parentFrame = frame;
        this.textBundle = ResourceBundle.getBundle("rescore.SSEBaseEngineText", Locale.getDefault());
        init();
    }
	
	public String showDialog() 
    {
    	this.pack();
    	this.setLocationRelativeTo(parentFrame);
    	this.setVisible(true);	
    	
    	return p;
    }
    
    private void init() throws Exception
    {       
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        buttonPane.setBorder(new EmptyBorder(16, 8, 16, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
               
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setLocationRelativeTo(parentFrame); 
        
        final PlaceholderPasswordField passwordTF;
        //PlaceholderPasswordField passwordConfirmTF;
        final JCheckBox showHidePasswordCB;
        JPanel passwordPane = new JPanel(new BorderLayout());
        passwordPane.setBorder(new EmptyBorder(10, 8, 2, 8));
        JPanel passwordLabelPane = new JPanel(new BorderLayout());
        JLabel passworLabel = new JLabel(textBundle.getString("ssecore.PasswodDialog.Password"), SwingConstants.LEFT);
        showHidePasswordCB = new JCheckBox(textBundle.getString("ssecore.PasswodDialog.ShowHide"));
        passwordTF = new PlaceholderPasswordField();
        passwordTF.enableInputMethods(true);
        passwordTF.setPlaceholder("");
        final char echoChar = passwordTF.getEchoChar();
        passwordTF.setPreferredSize(new Dimension(219, 24));
        //passwordTF.getDocument().addDocumentListener(getPasswordOnChangeListener());
        //passwordConfirmTF = new PlaceholderPasswordField();
        //passwordConfirmTF.enableInputMethods(true);
        //passwordConfirmTF.setPlaceholder(" " + textBundle.getString("ssefilegui.label.PasswordConfirmPH"));
        //passwordConfirmTF.setPreferredSize(new Dimension(250, 24));
        passwordLabelPane.add(passworLabel, BorderLayout.LINE_START);
        passwordLabelPane.add(showHidePasswordCB, BorderLayout.LINE_END);
        passwordPane.add(passwordLabelPane, BorderLayout.PAGE_START);
        passwordPane.add(passwordTF, BorderLayout.CENTER);
        //passwordPane.add(passwordConfirmTF, BorderLayout.PAGE_END);
        
        showHidePasswordCB.addActionListener(new ActionListener() 
        {	
        	public void actionPerformed(ActionEvent evt)  
			{
        		if(showHidePasswordCB.isSelected()) {
        			passwordTF.setEchoChar((char)0);
        		}
        		else { 
        			passwordTF.setEchoChar(echoChar);
        		}
			}
        });
        
        continueButton = new JButton(textBundle.getString("ssecore.text.Continue"));
        continueButton.setPreferredSize(new Dimension(100, 30));
        continueButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				p = passwordTF.getText().trim(); // deprecation note: (vs getPassword()) in fact, using strings doesn't change much regarding security concerns
				
				if(p.equals("")) {
					p = null;
					return;
				}
				
				SimplePasswordDialog.this.setVisible(false);
				SimplePasswordDialog.this.dispose();
			}
		});
        
        cancelButton = new JButton(textBundle.getString("ssecore.text.Cancel"));
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				p = null;
				SimplePasswordDialog.this.setVisible(false);
				SimplePasswordDialog.this.dispose();
			}
		});
        
        buttonPane.add(cancelButton, gbc);
        buttonPane.add(new JLabel("      "), gbc);   
        buttonPane.add(continueButton, gbc); 
        
        this.add(passwordPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);  
        
        EventQueue.invokeLater(new Runnable(){
        	public void run() 
        	{
	        	passwordTF.grabFocus();
	        	passwordTF.requestFocus();
        	}
        });
    }
}
