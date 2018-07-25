package com.paranoiaworks.sse.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

/**
 * Steganogram Parameters Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */ 


@SuppressWarnings("serial")
public class SteganogramSettingsDialog extends JDialog {
    
	private Frame parentFrame = null;
	private JButton cancelButton = null;
	private JButton continueButton = null;
	private JCheckBox rezizeCB;
	private FilteredTextField imageWidthTF;
	private FilteredTextField imageHeightTF;
	private FilteredTextField imagePercentageTF;
	private JLabel qualityLabel;
	private JSlider qualitySlider; 
	
	private Object[] outputValues = new Object[2];
	private ResourceBundle textBundle;
	private File carrierFile;
	private int originalWidth = -1;
	private int originalHeight = -1;
	private double ratio = -1;
	private boolean textFieldsChangeLock = false;
	
	private static final int MIN_WIDTH = 400;
	private static final int MIN_HEIGHT = 100;
	private static final int THUMBNAIL_LONGER_SIDE_MAX = 400;
	private static final int LONGER_SIDE_DEFAULT = 1000;
	private static final String ALLOWED_CHARS = "0123456789";
	
	public SteganogramSettingsDialog(Frame frame, File carrierFile) throws Exception
    {
        super(frame, "Title", true);
        this.parentFrame = frame;
        this.textBundle = ResourceBundle.getBundle("rescore.SSEBaseEngineText", Locale.getDefault());
        this.carrierFile = carrierFile;
        this.setTitle(textBundle.getString("ssecore.SteganogramDialog.Parameters"));
        init();
    }
	
	public Object[] showDialog() 
    {
    	this.pack();
    	this.setLocationRelativeTo(parentFrame);
    	this.setVisible(true);	
    	
    	return outputValues;
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
        
        continueButton = new JButton(textBundle.getString("ssecore.text.Continue"));
        continueButton.setPreferredSize(new Dimension(100, 30));
        continueButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
        		String widthS = imageWidthTF.getText().trim();
        		String heightS = imageHeightTF.getText().trim();
        		
        		Double scale = null;
        		
        		if(rezizeCB.isSelected())
        	    {
	        		if(widthS.length() == 0 || heightS.length() == 0) {
	        	    	setDefaultValues();
	        	    	return;
	        	    }
	        	    
	        	    int finalWidth = Integer.parseInt(widthS);
	        	    int finalHeight = Integer.parseInt(heightS);
	        	    
	         	    if(finalWidth < 3 || finalHeight < 3) {
	        	    	setDefaultValues();
	        	    	return;
	        	    }       	    
	         	    
	         	    
	         	    if(finalWidth > finalHeight) {
	         	    	scale = finalWidth / (double)originalWidth;
	         	    }
	         	    else {
	         	    	scale = finalHeight / (double)originalHeight;
	         	    }	
        	    }
        		else scale = 1.0;
         	    
         	    outputValues[0] = scale;
         	    outputValues[1] = qualitySlider.getValue();
				
				SteganogramSettingsDialog.this.setVisible(false);
				SteganogramSettingsDialog.this.dispose();
			}
		});
        
        cancelButton = new JButton(textBundle.getString("ssecore.text.Cancel"));
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SteganogramSettingsDialog.this.setVisible(false);
				SteganogramSettingsDialog.this.dispose();
			}
		});
        
        buttonPane.add(cancelButton, gbc);
        buttonPane.add(new JLabel("      "), gbc);   
        buttonPane.add(continueButton, gbc);   
        
        // Thumbnail
        ImageIcon thumbnail = null;
        
        try {
			
        	BufferedImage hdpiThumbnail = getThumbnail(carrierFile);
        	BufferedImage normalThumbnail = getThumbnail(hdpiThumbnail, false);
        	
    		try {
				List<Image> images = new ArrayList<Image>();
				images.add(normalThumbnail);
				images.add(hdpiThumbnail);

				Class<?> multiImageClass = Class.forName("java.awt.image.BaseMultiResolutionImage");
				Object multiImageObject = multiImageClass.getConstructor(Image[].class).newInstance((Object)images.toArray(new Image[0]));
				thumbnail = new ImageIcon((Image)multiImageObject);
			} catch (Exception e2) {}
        	
        	if(thumbnail == null)
        		thumbnail = new ImageIcon(normalThumbnail);
			
		} catch (Exception e1) {
			throw new IllegalStateException(textBundle.getString("ssecore.SteganogramDialog.InvalidImage") + ": " + carrierFile.getName());
		}
        
        JPanel parametersPane = new JPanel();
        parametersPane.setLayout(new BoxLayout(parametersPane, BoxLayout.Y_AXIS));
        parametersPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        // Set Size
        JPanel setSizeTop = new JPanel(new BorderLayout()); 
        setSizeTop.setBorder(new EmptyBorder(8, 8, 2, 8));
        rezizeCB = new JCheckBox(textBundle.getString("ssecore.SteganogramDialog.ResizeImage"));      
        setSizeTop.add(new JLabel(textBundle.getString("ssecore.SteganogramDialog.OutputImageSize")), BorderLayout.WEST); 
        setSizeTop.add(new JLabel("    "), BorderLayout.CENTER); 
        setSizeTop.add(rezizeCB, BorderLayout.EAST); 
        
        JPanel setSizeBottom = new JPanel(new BorderLayout());
        setSizeBottom.setBorder(new EmptyBorder(2, 8, 8, 8));
        JPanel setSizeBottomLeft = new JPanel();
        setSizeBottomLeft.setLayout(new BoxLayout(setSizeBottomLeft, BoxLayout.X_AXIS));
        imageWidthTF = new FilteredTextField();
        imageWidthTF.setAllowedChars(ALLOWED_CHARS, 5);
        imageWidthTF.setPreferredSize(new Dimension(80, 24));
        imageWidthTF.setHorizontalAlignment(JTextField.CENTER);
        imageHeightTF = new FilteredTextField();
        imageHeightTF.setAllowedChars(ALLOWED_CHARS, 5);
        imageHeightTF.setPreferredSize(new Dimension(80, 24));
        imageHeightTF.setHorizontalAlignment(JTextField.CENTER);
        imagePercentageTF = new FilteredTextField();
        imagePercentageTF.setAllowedChars(ALLOWED_CHARS, 3);
        imagePercentageTF.setPreferredSize(new Dimension(40, 24));
        imagePercentageTF.setHorizontalAlignment(JTextField.CENTER);
        setSizeBottomLeft.add(imageWidthTF);
        setSizeBottomLeft.add(new JLabel(" " + textBundle.getString("ssecore.SteganogramDialog.ImageSizeUnitPX") + "   "));
        setSizeBottomLeft.add(imageHeightTF);
        setSizeBottomLeft.add(new JLabel(" " + textBundle.getString("ssecore.SteganogramDialog.ImageSizeUnitPX") + "      "));
        setSizeBottomLeft.add(imagePercentageTF);
        setSizeBottomLeft.add(new JLabel(" %"));       
        setSizeBottom.add(setSizeBottomLeft, BorderLayout.WEST);
        
        parametersPane.add(setSizeTop);
        parametersPane.add(setSizeBottom);
        parametersPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        rezizeCB.setSelected(true);
        rezizeCB.addActionListener(new ActionListener() 
        {	
        	public void actionPerformed(ActionEvent evt)  
			{
        		if(rezizeCB.isSelected()) {
        			imageWidthTF.setEnabled(true);
        			imageHeightTF.setEnabled(true);
        			imagePercentageTF.setEnabled(true);
        			setDefaultValues();
        		}
        		else { 
        			imageWidthTF.setEnabled(false);
        			imageHeightTF.setEnabled(false);
        			imagePercentageTF.setEnabled(false);
        			setMaxValues();
        		}
			}
        });
        
        imageWidthTF.getDocument().addDocumentListener(new DocumentListener() 
        {
        	public void changedUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void removeUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void insertUpdate(DocumentEvent e) {
        		onChange();
        	}

        	public void onChange() 
        	{
        	    if(textFieldsChangeLock) return;
        		String text = imageWidthTF.getText().trim();
        		
        	    if(text.length() == 0) return;
        	    int value = Integer.parseInt(text);
        	    if(value < 1) return;
        	    
        	    int newWidth = value;
        	    int newHeight = -1;
        	    int newPercentage = -1;
        	    
        	    if (value >= originalWidth)
        	    {
        	    	newWidth = originalWidth;
        	    	newHeight = originalHeight;
        	    	newPercentage = 100;
        	    }
        	    else 
        	    {
        	    	newHeight = (int)Math.round(newWidth / ratio);
        	    	newPercentage = (int)Math.round(newWidth / (double)originalWidth * 100);
        	    }
        	    
        	    setValues(newWidth, newHeight, newPercentage);       	    
        	}
        });
        
        imageHeightTF.getDocument().addDocumentListener(new DocumentListener() 
        {
        	public void changedUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void removeUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void insertUpdate(DocumentEvent e) {
        		onChange();
        	}

        	public void onChange() 
        	{
        	    if(textFieldsChangeLock) return;
        		String text = imageHeightTF.getText().trim();
        		
        	    if(text.length() == 0) return;
        	    int value = Integer.parseInt(text);
        	    if(value < 1) return;
        	    
        	    int newWidth = -1;
        	    int newHeight = value;
        	    int newPercentage = -1;
        	    
        	    if (value >= originalHeight)
        	    {
        	    	newWidth = originalWidth;
        	    	newHeight = originalHeight;
        	    	newPercentage = 100;
        	    }
        	    else 
        	    {
        			newWidth = (int)Math.round(newHeight * ratio);
        			newPercentage = (int)Math.round(newHeight / (double)originalHeight * 100);
        	    }
        	    
        	    setValues(newWidth, newHeight, newPercentage); 	    
        	}
        });
        
        imagePercentageTF.getDocument().addDocumentListener(new DocumentListener() 
        {
        	public void changedUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void removeUpdate(DocumentEvent e) {
        		onChange();
        	}
        	public void insertUpdate(DocumentEvent e) {
        		onChange();
        	}

        	public void onChange() 
        	{
        	    if(textFieldsChangeLock) return;
        		String text = imagePercentageTF.getText().trim();
        		
        	    if(text.length() == 0) return;
        	    int value = Integer.parseInt(text);
        	    if(value < 1) return;
        	    
        	    int newWidth = -1;
        	    int newHeight = -1;
        	    int newPercentage = value;
        	    
        	    if (value >= 100)
        	    {
        	    	newWidth = originalWidth;
        	    	newHeight = originalHeight;
        	    	newPercentage = 100;
        	    }
        	    else 
        	    {
        	    	newWidth = (int)Math.round(originalWidth * ((double)newPercentage / 100));
        	    	newHeight = (int)Math.round(originalHeight * ((double)newPercentage / 100));
        	    }
        	    
        	    setValues(newWidth, newHeight, newPercentage);    	    
        	}
        });
   
        
        // Set JPEG Quality
        JPanel setQualityTop = new JPanel(new BorderLayout());
        setQualityTop.setBorder(new EmptyBorder(8, 8, 2, 8));
        qualityLabel = new JLabel();
        setQualityTop.add(qualityLabel, BorderLayout.WEST); 
        
        JPanel setQualityBottom = new JPanel(new BorderLayout());
        setQualityBottom.setBorder(new EmptyBorder(2, 1, 10, 1));  
        qualitySlider = new JSlider(JSlider.HORIZONTAL, 50, 100, 90);
        setQualityBottom.add(qualitySlider);
        
        parametersPane.add(setQualityTop);
        parametersPane.add(setQualityBottom);
        parametersPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        this.add(new JLabel(thumbnail), BorderLayout.NORTH);
        this.add(parametersPane, BorderLayout.CENTER);
        this.add(buttonPane, BorderLayout.SOUTH);
        
        qualitySlider.addChangeListener(new ChangeListener() 
        {
        	public void stateChanged(ChangeEvent event) 
        	{
        		qualityLabel.setText(textBundle.getString("ssecore.SteganogramDialog.JpegQuality") + " (" + qualitySlider.getValue() + ")");
            }
          });
        
        qualityLabel.setText(textBundle.getString("ssecore.SteganogramDialog.JpegQuality") + " (" + qualitySlider.getValue() + ")");
        
        setDefaultValues();
    }
    
    private BufferedImage getThumbnail(File imageFile) throws Exception
    {
    	BufferedImage image = ImageIO.read(imageFile);
    	originalWidth = image.getWidth();
		originalHeight = image.getHeight();
    	return getThumbnail(image, true);
    }
    private BufferedImage getThumbnail(BufferedImage image, boolean hdpi) throws Exception
    {		
        double scale = 1.0;
    	
    	if(hdpi) 
    	{
	        try {
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
				scale = defaultScreen.getDefaultConfiguration().getDefaultTransform().getScaleX();
			} catch (Exception e) {}
	    	
	    	if(scale < 1.0) scale = 1.0; 
    	}
    	
    	int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();
		ratio = originalWidth / (double)originalHeight;
		int newWidth = 1;
		int newHeight= 1;
		
		int longerSide = originalWidth > originalHeight ? originalWidth : originalHeight;
		if(longerSide > Math.round(THUMBNAIL_LONGER_SIDE_MAX * (hdpi ? scale : 1))) 
			longerSide = (int)Math.round(THUMBNAIL_LONGER_SIDE_MAX * (hdpi ? scale : 1));
		
		if(originalWidth > originalHeight) {
			newWidth = longerSide;
			newHeight = (int)Math.round(newWidth / ratio);
		}
		else {
			newHeight = longerSide;
			newWidth= (int)Math.round(ratio * newHeight);
		}
		
		ResampleOp resizeOp = new ResampleOp(newWidth, newHeight);
		resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
		image = resizeOp.filter(image, null);
		
		return image;
    }
    
    private void setDefaultValues()
    {
    	textFieldsChangeLock = true;
    	int newWidth = originalWidth;
    	int newHeight = originalHeight;
    	int percentage = 100;
    	
    	if(ratio > 1.0) 
    	{
    		if(newWidth > LONGER_SIDE_DEFAULT) 
    		{ 
    			newWidth = LONGER_SIDE_DEFAULT;
    			newHeight = (int)Math.round(newWidth / ratio);
    			percentage = (int)Math.round(newWidth / (double)originalWidth * 100);
    		}  		
    	}
    	else
    	{
    		if(newHeight > LONGER_SIDE_DEFAULT)
    		{
    			newHeight = LONGER_SIDE_DEFAULT;
    			newWidth = (int)Math.round(newHeight * ratio);
    			percentage = (int)Math.round(newHeight / (double)originalHeight * 100);
    		}
    	}
    	
    	imageWidthTF.setText(Integer.toString(newWidth));
    	imageHeightTF.setText(Integer.toString(newHeight));
    	imagePercentageTF.setText(Integer.toString(percentage));  
    	textFieldsChangeLock = false;
    }
    
    private void setMaxValues()
    {
    	textFieldsChangeLock = true;
    	imageWidthTF.setText(Integer.toString(originalWidth));
    	imageHeightTF.setText(Integer.toString(originalHeight));
    	imagePercentageTF.setText(Integer.toString(100)); 
    	textFieldsChangeLock = false;
    }
    
    private void setValues(final int finalWidth, final int finalHeight, final int finalPercentage)
    {
	    Runnable renderChanges = new Runnable() {
	        @Override
	        public void run() {
	        	textFieldsChangeLock = true;
	        	imageWidthTF.setText(Integer.toString(finalWidth));
	        	imageHeightTF.setText(Integer.toString(finalHeight));
	        	imagePercentageTF.setText(Integer.toString(finalPercentage)); 
	        	textFieldsChangeLock = false;
	        }
	    };       
	    SwingUtilities.invokeLater(renderChanges); 
    }
    
}
