package com.paranoiaworks.sse.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.paranoiaworks.sse.CryptFile;
import com.paranoiaworks.sse.Encryptor;
import com.paranoiaworks.sse.Helpers;
import com.paranoiaworks.sse.MessageHandler;
import com.paranoiaworks.sse.components.MessageDialog;
import com.paranoiaworks.sse.components.PlaceholderPasswordField;
import com.paranoiaworks.sse.components.SimplePasswordDialog;
import com.paranoiaworks.sse.dao.SettingsDataHolder;

/**
 * S.S.E. for PC - Drag and Drop GUI Main Class
 * 
 * @author Paranoia Works
 * @version 1.0.12
 */ 

public class SSEFileGUI extends JFrame {

    private static final long serialVersionUID = 1L;    
    private static final String appVersionCode = "12R3F";
    private static final String publishYear = "2018";
    private static final String CONFIG_FILE = "ssefe.conf";
    
    protected static final String FE_SETTINGS_ALGORITHM = "FE_SETTINGS_ALGORITHM";
    protected static final String FE_SETTINGS_OUTPUTDIR_ENC_ENABLED = "FE_SETTINGS_OUTPUTDIR_ENC_ENABLED";
    protected static final String FE_SETTINGS_OUTPUTDIR_DEC_ENABLED = "FE_SETTINGS_OUTPUTDIR_DEC_ENABLED";
    protected static final String FE_SETTINGS_OUTPUTDIR_ENC_PATH = "FE_SETTINGS_OUTPUTDIR_ENC_PATH";
    protected static final String FE_SETTINGS_OUTPUTDIR_DEC_PATH = "FE_SETTINGS_OUTPUTDIR_DEC_PATH";
    
    private static final int SQUARE_BUTTON_SIZE = Helpers.isMac() ? 40 : 34;
    
    private JComboBox algorithmCB;
    private PlaceholderPasswordField passwordTF;
    private PlaceholderPasswordField passwordConfirmTF;
    private JButton stopButton;
    private JProgressBar filesPB;
    private JProgressBar currentFilePB;
    private JDialog helpDialog;
    private final JCheckBox showHidePasswordCB;
    
    private Thread workerThread;
    private MessageHandler mh;
    
    private String[] cmdLineArgs;
    private boolean compress = true;
    private String inputFilePath = null;		
    private String password = null;
    private int algorithmCode = -1;
    private int errors = -1;
    private CryptFile inputFile = null;	
    private final SettingsDataHolder sdh;
	
    private List<File> files;
    private boolean encDecLock = false;
	
	private static ResourceBundle textBundle;
	
	static {
		//Locale.setDefault(new Locale("ja"));
		Locale locale = Locale.getDefault();
		textBundle = ResourceBundle.getBundle("res.SSEFileGUI", locale);
		
		ResourceBundle fileChooserRB = null;		
		try {
			List<String> fileChooserKeys = Collections.list(ResourceBundle.getBundle("res.JFileChooserKeys").getKeys());
			fileChooserRB = ResourceBundle.getBundle("res.JFileChooser", locale);		
			for(int i = 0; i < fileChooserKeys.size(); ++i)
			{
				try{UIManager.put(fileChooserKeys.get(i), fileChooserRB.getString(fileChooserKeys.get(i)));}catch(Exception e){}
			}
	
		} catch (Exception e) {
			// swallow exception
		}	
	}

    public static void main(String[] args) {
        new SSEFileGUI(args);
    }

    /** Prepare and Render Main Window */
    public SSEFileGUI(String[] args) {
        super(textBundle.getString("ssefilegui.label.MainLabel"));
        cmdLineArgs = args;
        int windowWidth = Integer.parseInt(textBundle.getString("ssefilegui.spec.mainWindowWidth"));
		int windowHeight = Integer.parseInt(textBundle.getString("ssefilegui.spec.mainWindowHeight"));
		int textSizeIncDec = Integer.parseInt(textBundle.getString("ssefilegui.spec.textPaneFontIncDec"));
        
        this.setMinimumSize(new Dimension(windowWidth, windowHeight));

        String[] algorithmList = {"AES (256 bit)", "RC6 (256 bit)", "Serpent (256 bit)", "Twofish (256 bit)", "GOST28147 (256 bit)", "Blowfish (448 bit)"};

        sdh = SettingsDataHolder.getSettingsDataHolder(CONFIG_FILE);       
        
        //+ Top Pane
        final JPanel topPane = new JPanel(new BorderLayout());
        JPanel topPaneLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPaneLeft.setBorder(new EmptyBorder(2, 0, 8, 0));
        JPanel bottomPane = new JPanel(new BorderLayout());
        bottomPane.setBorder(new EmptyBorder(5, 0, 5, 2));
        
        JPanel passwordPane = new JPanel(new BorderLayout());
        JPanel passwordLabelPane = new JPanel(new BorderLayout());
        JLabel passworLabel = new JLabel(textBundle.getString("ssefilegui.label.PasswordLabel"), SwingConstants.LEFT);
        showHidePasswordCB = new JCheckBox(textBundle.getString("ssefilegui.label.ShowHidePasswordCB"));
        passwordTF = new PlaceholderPasswordField();
        passwordTF.enableInputMethods(true);
        passwordTF.setPlaceholder(" " + textBundle.getString("ssefilegui.label.PasswordPH"));
        final char echoChar = passwordTF.getEchoChar();
        passwordTF.setPreferredSize(new Dimension(250, 24));
        passwordTF.getDocument().addDocumentListener(getPasswordOnChangeListener());
        passwordConfirmTF = new PlaceholderPasswordField();
        passwordConfirmTF.enableInputMethods(true);
        passwordConfirmTF.setPlaceholder(" " + textBundle.getString("ssefilegui.label.PasswordConfirmPH"));
        passwordConfirmTF.setPreferredSize(new Dimension(250, 24));
        passwordLabelPane.add(passworLabel, BorderLayout.LINE_START);
        passwordLabelPane.add(showHidePasswordCB, BorderLayout.LINE_END);
        passwordPane.add(passwordLabelPane, BorderLayout.PAGE_START);
        passwordPane.add(passwordTF, BorderLayout.CENTER);
        passwordPane.add(passwordConfirmTF, BorderLayout.PAGE_END);
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
            	try {
            		sdh.addOrReplacePersistentDataObject(FE_SETTINGS_ALGORITHM, algorithmCB.getSelectedIndex());
					sdh.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
                System.exit(0);
            }
        });
        
        showHidePasswordCB.addActionListener(new ActionListener() 
        {	
        	public void actionPerformed(ActionEvent evt)  
			{
        		if(showHidePasswordCB.isSelected()) {
        			passwordTF.setEchoChar((char)0);
        			passwordConfirmTF.setText(Helpers.createStringWithLength(passwordTF.getPassword().length, '.'));
        			passwordConfirmTF.setEnabled(false);
        			passwordConfirmTF.setBackground(topPane.getBackground());
        		}
        		else { 
        			passwordTF.setEchoChar(echoChar);
        			passwordConfirmTF.setEnabled(true);
        			passwordConfirmTF.setText(passwordTF.getText());
        			passwordConfirmTF.setBackground(passwordTF.getBackground());
        		}
			}
        });
        
        JPanel algorithmPane = new JPanel(new BorderLayout());
        algorithmPane.setBorder(new EmptyBorder(0, 10, 21, 0));
        JLabel algorithmLabel = new JLabel(textBundle.getString("ssefilegui.label.AlgorithmLabel"), SwingConstants.LEFT);
        algorithmLabel.setBorder(new EmptyBorder(3, 0, 4, 0));
        algorithmCB = new JComboBox(algorithmList);
        algorithmCB.setEditable(false);
        algorithmPane.add(algorithmLabel, BorderLayout.PAGE_START);
        algorithmPane.add(algorithmCB, BorderLayout.CENTER);    
        
        Integer algorithmCBSetting = sdh.getPersistentDataInteger(FE_SETTINGS_ALGORITHM);
        if(algorithmCBSetting != null && algorithmCBSetting + 1 <= algorithmList.length)        
        	algorithmCB.setSelectedIndex(algorithmCBSetting);
        
        JPanel topPaneRight = new JPanel(new BorderLayout());
        topPaneRight.setBorder(new EmptyBorder(5, 5, 55, 5));
        JButton helpButton = new JButton();
        helpButton.setToolTipText(textBundle.getString("ssefilegui.label.HelpDialog"));
        helpButton.setPreferredSize(new Dimension(SQUARE_BUTTON_SIZE, SQUARE_BUTTON_SIZE));
        JButton settingsButton = new JButton();
        settingsButton.setToolTipText(textBundle.getString("ssefilegui.settings.settingsTitle"));
        settingsButton.setPreferredSize(new Dimension(SQUARE_BUTTON_SIZE, SQUARE_BUTTON_SIZE));
        topPaneRight.add(settingsButton, BorderLayout.WEST);
        topPaneRight.add(new JLabel(" "), BorderLayout.CENTER);
        topPaneRight.add(helpButton, BorderLayout.EAST);
        
        try {
			Image imgHelp = Helpers.loadImageFromResource(SSEFileGUI.class, "icon_help", "png");
			Image imgSettings = Helpers.loadImageFromResource(SSEFileGUI.class, "icon_settings", "png");
			
			helpButton.setIcon(new ImageIcon(imgHelp));
			settingsButton.setIcon(new ImageIcon(imgSettings));
			
			List<Image> appIcons = new ArrayList<Image>();
			appIcons.add(ImageIO.read(getClass().getResource("/res/icon_16.png")));
			appIcons.add(ImageIO.read(getClass().getResource("/res/icon_32.png")));	
			appIcons.add(ImageIO.read(getClass().getResource("/res/icon_64.png")));	
			appIcons.add(ImageIO.read(getClass().getResource("/res/icon_128.png")));	
			this.setIconImages(appIcons);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        helpButton.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent evt) 
        	{
        		final HelpDialogFE helpDialog = new HelpDialogFE(SSEFileGUI.this, textBundle, sdh, appVersionCode, publishYear);
        		helpDialog.setShowProUpdate(false);
        		helpDialog.showDialog();
		    }
        });  
        
        settingsButton.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent evt) 
        	{
        		final SettingsDialogFE settingsDialog = new SettingsDialogFE(SSEFileGUI.this, textBundle, sdh);
        		settingsDialog.showDialog();
		    }
        }); 
        
        topPaneLeft.add(passwordPane);
        topPaneLeft.add(algorithmPane);
        topPane.add(topPaneLeft, BorderLayout.LINE_START);
        topPane.add(topPaneRight, BorderLayout.LINE_END);
        //- Top Pane
        

        //+ Bottom Pane
        JPanel progressBarPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filesPB = new JProgressBar();
        currentFilePB = new JProgressBar();
        filesPB.setStringPainted(true);
        currentFilePB.setStringPainted(true);
    
        progressBarPane.add(new JLabel(textBundle.getString("ssefilegui.label.ProgressBarPaneCurrent") + " ", SwingConstants.LEFT));
        progressBarPane.add(currentFilePB);
        progressBarPane.add(new JLabel("     ", SwingConstants.LEFT));
        progressBarPane.add(new JLabel(textBundle.getString("ssefilegui.label.ProgressBarPaneAll") + " ", SwingConstants.LEFT));
        progressBarPane.add(filesPB);
        
        stopButton = new JButton(textBundle.getString("ssefilegui.label.StopButton"));
        stopButton.setPreferredSize(new Dimension(80, 30));
        stopButton.setEnabled(false);
        
        stopButton.addActionListener(new ActionListener() 
        {
        	public void actionPerformed(ActionEvent evt) 
        	{
        		mh.interrupt();
		    }
        });
        
        bottomPane.add(progressBarPane, BorderLayout.LINE_START);
        bottomPane.add(stopButton, BorderLayout.LINE_END);
        //- Bottom Pane
        

        //+ Drop (center) Pane
        JPanel dropPane = new JPanel(new BorderLayout());
        
        JLabel dropLabel = new JLabel(textBundle.getString("ssefilegui.label.DropLabel"), SwingConstants.CENTER);
        int dropLabelFontSize = Integer.parseInt(textBundle.getString("ssefilegui.spec.dropLabelFontSize"));
        dropLabel.setFont(new Font(dropLabel.getFont().getFamily(), Font.BOLD, dropLabelFontSize));
        Border paddingBorder = BorderFactory.createEmptyBorder(2,0,2,0);
        dropLabel.setBorder(paddingBorder);       
        dropLabel.setOpaque(true);
        dropLabel.setForeground(Color.decode("#E0E0E0"));
        dropLabel.setBackground(Color.decode("#707070"));
        
        JPanel mainTextAreaWrapper = new JPanel(new BorderLayout());
        mainTextAreaWrapper.setBorder(BorderFactory.createLineBorder(Color.decode("#FF0000")));
        JTextPane mainTextArea = new JTextPane();  
        mainTextArea.setEditable(false);        
        mainTextArea.setMargin(new Insets(5,5,5,5));
        if(textSizeIncDec != 0) {
        	  Font f = mainTextArea.getFont();
        	  Font f2 = new Font(f.getFontName(), f.getStyle(), f.getSize() + textSizeIncDec);
        	  mainTextArea.setFont(f2);
        }  
        
        JScrollPane sbrText = new JScrollPane(mainTextArea);
        sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sbrText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        mainTextAreaWrapper.add(sbrText, BorderLayout.CENTER);
        dropPane.add(dropLabel, BorderLayout.PAGE_START);
        dropPane.add(mainTextAreaWrapper, BorderLayout.CENTER);
             
        mh = new MessageHandler(mainTextArea);
        mh.addProgressBar(currentFilePB);
        
        // Drag and Drop listener implementation 
        SimpleDragDropListener ddListener = new SimpleDragDropListener()
        {
		    @Override
		    public void drop(DropTargetDropEvent event) 
		    {
		        if(encDecLock) return;
		        lockApp();
		        
				password = passwordTF.getText().trim(); // deprecation note: (vs getPassword()) in fact, using strings doesn't change much regarding security concerns
				algorithmCode = Encryptor.positionToAlgCode(algorithmCB.getSelectedIndex());
				
				if(password.equals(""))
				{
					mh.print(textBundle.getString("ssefilegui.text.EnterPassword") + "\n\n", Color.RED, true);
					unlockApp();
					return;
				}
		        
		        event.acceptDrop(DnDConstants.ACTION_COPY);
		        Transferable transferable = event.getTransferable();
		        DataFlavor[] flavors = transferable.getTransferDataFlavors();

		        files = null;
		        for (DataFlavor flavor : flavors) 
		        {
		            try {
		                if (flavor.isFlavorJavaFileListType()) files = (List)transferable.getTransferData(flavor);
		                
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }

		        event.dropComplete(true);    
		        if (files == null || files.size() == 0) {
		        	mh.print(textBundle.getString("ssefilegui.text.IncorrectFile") + "\n\n", Color.RED, true);
		        	unlockApp();
		        	return;
		        }
		        
		        boolean checkPasswordConfirmation = false;
		        
		        for(int i = 0; i < files.size(); ++i) 
		        {
		        	CryptFile tempFile = new CryptFile(files.get(i));
		        	if(tempFile.isFile() && tempFile.isEncrypted()) continue;
		        	checkPasswordConfirmation = true;
		        	break;
		        }
		        
				if(!password.equals(passwordConfirmTF.getText().trim()) && !showHidePasswordCB.isSelected() && checkPasswordConfirmation)
				{
					mh.print(textBundle.getString("ssefilegui.text.PasswordsDontMatch") + "\n\n", Color.RED, true);
					unlockApp();
					return;
				}
		        
		        encDecExecute();  
		    }
        };
        
        new DropTarget(mainTextArea, ddListener); // Connect the mainTextArea with a drag and drop listener
        //- Drop (center) Pane

        // Add components to the content
        this.getContentPane().add(BorderLayout.NORTH, topPane);
        this.getContentPane().add(BorderLayout.CENTER, dropPane);
        this.getContentPane().add(BorderLayout.PAGE_END, bottomPane);  
        
        // Center
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        
        boolean showPasswordDialog = false;
        if(cmdLineArgs != null && cmdLineArgs.length > 0)
		{
			try {
				CryptFile inputFile = new CryptFile(cmdLineArgs[0]);
				if(inputFile.exists() && inputFile.isEncrypted())
				{
					inputFile = new CryptFile(inputFile.getAbsolutePath());
					showPasswordDialog = true;
					SimplePasswordDialog spd = new SimplePasswordDialog(SSEFileGUI.this, inputFile.getName());
					password = spd.showDialog();
					
					if(password != null) {
						passwordTF.setText(password);
						passwordConfirmTF.setText(password);
						
						files = new ArrayList<File>();
						files.add(inputFile);
						encDecExecute();
					}
				}
					
			} catch (Exception e) {
				// swallow
			}
		}
        
		if(!showPasswordDialog)
		{
	        EventQueue.invokeLater(new Runnable(){
	        	public void run() 
	        	{
	        		passwordTF.grabFocus();
	        		passwordTF.requestFocus();
	        		if(sdh.getErrorMessage() != null)
	        			showErrorDialog(sdh.getErrorMessage());  
	        	}
	        });
		}
    }
    
    /** Enc/Dec method */
    public synchronized void encDecExecute()
    {
		// Start enc/dec process
        workerThread = new Thread (new Runnable() 
		{
			public void run() 
			{	
				filesPB.setMaximum(files.size());
				filesPB.setValue(0);
				filesPB.setString(0 + "/" + files.size());
				Collections.sort(files);
				
				for (int i = 0; i < files.size(); ++i) 
		        {	
					inputFilePath = files.get(i).getPath();						
					inputFile = new CryptFile(inputFilePath);
					
					if(!mh.isBlank()) {
						if(i > 0) mh.print(textBundle.getString("ssefilegui.spec.horDelimiter") + "\n\n", Color.LIGHT_GRAY, false);
						else mh.print(textBundle.getString("ssefilegui.spec.horDelimiterFirst") + "\n\n");
					}
					
					mh.print(textBundle.getString("ssefilegui.text.Processing") + " (" + (i + 1) + "/" + files.size() + "): ", null, true);
					mh.print(inputFile.getName(), Color.decode("#0000AA"), true);
					if(inputFile.isFile() && inputFile.length() < 1) mh.println(textBundle.getString("ssefilegui.warning.FileSize") + " " + inputFile.length(), Color.RED, true);
					mh.println("");
					
					errors = -1;
					try {
						Encryptor encryptor = new Encryptor(password, algorithmCode, true);
						
						if(!inputFile.isEncrypted())
							errors = (int)encryptor.zipAndEncryptFile(inputFile, compress, mh, getOutputFolder(true));
						else
							errors = (int)encryptor.unzipAndDecryptFile(new CryptFile(inputFilePath), mh, getOutputFolder(false));
						
    					filesPB.setValue(i + 1);
    					filesPB.setString((i + 1) + "/" + files.size());
						
					} catch (InvalidParameterException e) {
						mh.print(e.getMessage() + "\n\n", Color.RED, true);
						break;
					}  catch (Exception e) {
						String message = "";
						if(!inputFile.isEncrypted())
						{
							System.gc(); try{Thread.sleep(200);}catch(InterruptedException e1){}; // Because of a bug in Java						
							message = e.getMessage();
							CryptFile tf = new CryptFile(inputFilePath + "." + Encryptor.ENC_FILE_EXTENSION + "." + Encryptor.ENC_FILE_UNFINISHED_EXTENSION);
							if(tf.exists()) tf.delete(); 
						}
						else
						{
							System.gc(); try{Thread.sleep(200);}catch(InterruptedException e1){}; // Because of a bug in Java
							message = e.getMessage();
							String[] messages = message.split("\\|\\|"); // try to split "message||delete file path"
							if(!(messages == null || messages.length < 1 || messages[0] == null || messages[0].trim().equals(""))) message = messages[0];
							
							if(messages.length > 1)
							{
								CryptFile tf = new CryptFile(messages[1]);
								if(tf.exists()) tf.delete();
							}
						}
						
        	    		mh.println(message + "\n\n", Color.RED, true);
        	    		break;
					}
					
					if(errors > -1)
					{
						if(errors == 0) mh.println("\n" + textBundle.getString("ssefilegui.text.Completed") + ": OK\n\n", null, true);
						else mh.println("\n" + textBundle.getString("ssefilegui.text.Completed")+ ": " + errors + " " + textBundle.getString("ssefilegui.text.Errors").toLowerCase() + "\n\n", Color.RED, true);
					}
				}

				unlockApp();
			}
		});
		workerThread.setPriority(Thread.MIN_PRIORITY);
		workerThread.start();
    }
    
    /** Prepare layout for enc/dec process */
    private void lockApp()
    {
	    encDecLock = true;     
	    showHidePasswordCB.setEnabled(false);
	    passwordTF.setEnabled(false);
	    passwordConfirmTF.setEnabled(false);
	    algorithmCB.setEnabled(false);
	    stopButton.setEnabled(true);
    }
    
    /** Return layout back to "Enter Parameters" */
    private void unlockApp()
    {
        encDecLock = false;
        showHidePasswordCB.setEnabled(true);
        passwordTF.setEnabled(true);
        if(!showHidePasswordCB.isSelected()) passwordConfirmTF.setEnabled(true);
        algorithmCB.setEnabled(true);
        stopButton.setEnabled(false);
    }
    
    /** Show Error Dialog */
    private void showErrorDialog(String message)
    {
    	final MessageDialog messageDialog = new MessageDialog(SSEFileGUI.this, "", MessageDialog.ICON_NEGATIVE);
    	messageDialog.setText("<html>" + message + "</html>");
    	messageDialog.showDialog();
    }
    
    /** Get Enc/Dec Output Directory */
    private File getOutputFolder(boolean encrypted) 
    { 	
    	String enabledCode = encrypted ? FE_SETTINGS_OUTPUTDIR_ENC_ENABLED : FE_SETTINGS_OUTPUTDIR_DEC_ENABLED;
    	String pathCode = encrypted ? FE_SETTINGS_OUTPUTDIR_ENC_PATH : FE_SETTINGS_OUTPUTDIR_DEC_PATH;
    	
		Boolean enabled = sdh.getPersistentDataBoolean(enabledCode);		
		if(enabled == null || !enabled) return null;
		
		String path = sdh.getPersistentDataString(pathCode);
    	
		File outputDir = null;
    	
    	try {
			outputDir = new File(path);		
			if(!outputDir.exists() || !outputDir.isDirectory() || !Helpers.writeTestFile(outputDir))
				outputDir = null;				
		} catch (Exception e) {
			// swallow
		}
    	
    	if(outputDir == null) 
    	{
    		sdh.addOrReplacePersistentDataObject(enabledCode, false);
    		sdh.addOrReplacePersistentDataObject(pathCode, null);
    		
    		try {
				sdh.save();
			} catch (Exception e) {
				// swallow
			}
    	}
    	
    	return outputDir;
    }
    
    /** Password Field Change Listener */
    private DocumentListener getPasswordOnChangeListener()
    {
    	return new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
            	handleEvent();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
            	handleEvent();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
            	handleEvent();
            }
            
            private void handleEvent() {
            	if(showHidePasswordCB.isSelected()) {
        			passwordConfirmTF.setText(Helpers.createStringWithLength(passwordTF.getPassword().length, '.'));
            	}
            }
    	};
    }
}