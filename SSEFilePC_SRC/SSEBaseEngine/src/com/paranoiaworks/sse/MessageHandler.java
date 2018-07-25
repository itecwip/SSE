package com.paranoiaworks.sse;

import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Thread Safe "GUI Manipulation MessageHandler" and formatting helper
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.2
 */ 

public class MessageHandler {

	private JTextArea textArea = null;
	private JTextPane textPane = null;
	private JProgressBar progressBar = null;
	private int lastLineSize = 0;
	private boolean interrupted = false;
	private long inpuFileSize = 1;
	
	private static Lock lock = new ReentrantLock(); 
	
	public MessageHandler()
	{
	}
	
	public MessageHandler(JTextArea textArea)
	{
		this.textArea = textArea;
	}
	
	public MessageHandler(JTextPane textPane)
	{
		this.textPane = textPane;
	}
	
	public void addProgressBar(JProgressBar progressBar)
	{
		this.progressBar = progressBar;
	}
	
	public void print(String text)
	{
		print(text, null, false);
	}
	
	public void print(String text, Color color, boolean bold)
	{
		lock.lock();
		if(textArea != null)
		{
			textArea.append(text);
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		else if(textPane != null)
		{	
			if(color == null) color = Color.BLACK;
			MutableAttributeSet att = textPane.getInputAttributes();
			StyleConstants.setForeground(att, color);
			StyleConstants.setBold(att, bold);
			
			StyledDocument doc = textPane.getStyledDocument();
			try {
				doc.insertString(doc.getLength(), text, att);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			textPane.setCaretPosition(textPane.getDocument().getLength());
		}
		else
		{
			System.out.print(text);
		}
		lastLineSize = text.length();
		lock.unlock();
	}
	
	public void println(String text)
	{
		println(text, null, false);
	}
	
	public void println(String text, Color color, boolean bold)
	{
		lock.lock();
		if(textArea != null)
		{
			textArea.append("\n" + text);
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		else if(textPane != null)
		{	
			if(color == null) color = Color.BLACK;
			MutableAttributeSet att = textPane.getInputAttributes();
			StyleConstants.setForeground(att, color);
			StyleConstants.setBold(att, bold);
			
			StyledDocument doc = textPane.getStyledDocument();
			try {
				doc.insertString(doc.getLength(), "\n" + text, att);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			textPane.setCaretPosition(textPane.getDocument().getLength());
		}
		else
		{
			System.out.println(text);
		}
		lastLineSize = text.length();
		lock.unlock();
	}
	
	public void removeLast()
	{
		lock.lock();
		if(textArea != null)
		{
			try {
				textArea.getDocument().remove(textArea.getDocument().getLength() - lastLineSize, lastLineSize);
				lastLineSize = 0;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		else if(textPane != null)
		{
			try {
				textPane.getDocument().remove(textPane.getDocument().getLength() - lastLineSize, lastLineSize);
				lastLineSize = 0;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		lock.unlock();
	}
	
	public void setProgress(int value)
	{
		lock.lock();
		if(progressBar != null)
		{
			progressBar.setValue(value);
			progressBar.setString(value + "%");
		}
		lock.unlock();
	}
	
	public boolean isBlank()
	{
		boolean blank = true;
		lock.lock();
		if(textPane.getDocument().getLength() > 0) blank = false;
		lock.unlock();
		return blank;
	}
	
	public void interrupt()
	{
		lock.lock();
		interrupted = true;
		lock.unlock();
	}
	
	public void interruptReset()
	{
		lock.lock();
		interrupted = false;
		lock.unlock();
	}
	
	public boolean interrupted()
	{
		return interrupted;
	}
	
	public long getInpuFileSize() {
		if(inpuFileSize < 1) inpuFileSize = 1;
		return inpuFileSize;
	}

	public void setInpuFileSize(long inpuFileSize) {
		this.inpuFileSize = inpuFileSize;
	}
}
