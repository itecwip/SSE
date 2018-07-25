package com.paranoiaworks.unicus.android.sse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.paranoiaworks.unicus.android.sse.components.SimpleWaitDialog;

/**
 * Contains Simple Logger and "SystemLog Collector"
 * Logs to application Import/Export Directory + LogCat verbose
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 */
public class SSElog {
	
	private static boolean firstLog = true;

	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	
	private Activity context;
	private Dialog waitDialog;
	
	public SSElog(View v) 
	{
		this.context = ((Activity)v.getContext());
	}
	
	public SSElog(Activity context) 
	{
		this.context = context;
	}
		
	public static void d(byte[] text)
	{
			l(null, text, "debug.log", false, firstLog && true, false);
	}
	
	public static void d(String text)
	{
		try {
			l(null, text.getBytes("UTF8"), "debug.log", true, firstLog && true, false);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void d(Exception e)
	{
 	   StringWriter sw = new StringWriter();
	   e.printStackTrace(new PrintWriter(sw));
	   String stackTrace = sw.toString();
	   try {
		   l(null, stackTrace.getBytes("UTF8"), "debug.log", false, firstLog && true, false);
	   } catch (UnsupportedEncodingException e1) {
		   e1.printStackTrace();
	   }
	}
	
	public static void d(String header, byte[] text)
	{
			l(header, text, "debug.log", false, firstLog && true, false);
	}
	
	public static void d(String header, String text)
	{
		try {
			l(header, text.getBytes("UTF8"), "debug.log", true, firstLog && true, false);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void l(String header, byte[] text, String fileName, boolean fromString, boolean clearLog, boolean toFileOnly)
	{
		try {
			String sText = null;
			if(fromString) sText = new String(text, "UTF8");
			if(header != null)
			{
				header = header.trim();
				header += "\t";
			} 
			else header = "";
			if(!toFileOnly) Log.v(header, new String(text, "UTF8"));
			
			File logDir = new File(Helpers.getImportExportDir() + File.separator + "logs");
			if(!logDir.exists()) logDir.mkdir();
			File log = new File (logDir.getAbsolutePath() + File.separator + fileName);
			if(clearLog) log.delete();
			firstLog = false;
			
			FileOutputStream fw = new FileOutputStream(log, true);
			if(sText != null)
				fw.write((getTime() + "\t" + header + sText + "\n").getBytes("UTF8"));
			else
			{
				fw.write((getTime() + "\t" + header + "Binary Data:"  + "\n").getBytes("UTF8"));
				fw.write(text);
				fw.write("\n\n".getBytes());
			}
				
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private static String getTime() 
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(System.currentTimeMillis());
	}
	
	//+ SystemLog Collector
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private static void sld(String header, String text, String fileName)
	{
		try {
			l(header, text.getBytes("UTF8"), fileName, true, true, true);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void dumpSystemErrorLog()
	{
		dumpSystemLog('E');
	}
	
	public void dumpSystemLogWhole()
	{
		dumpSystemLog('X');
	}
	
	public void dumpSystemLog(char logLevel)
	{
		waitDialog = new SimpleWaitDialog(context);
		ArrayList<String> commandLineParams = new ArrayList<String>();
        
		commandLineParams.add("-v");
		commandLineParams.add("time");
		commandLineParams.add("*:" + logLevel); //V Verbose, D Debug, I Info, W Warn, E Error, F Fatal, X - direct dump of whole log
		
		new SystemLogTask().execute(commandLineParams);
	}
	
    private class SystemLogTask extends AsyncTask<ArrayList<String>, Void, StringBuffer> 
    {            
    	private static final String SLD_FILENAME = "systemlog_dump_<NULL>.log";
    	String fileName = SLD_FILENAME;
    	
    	@Override
        protected void onPreExecute()
        {
        	if(waitDialog != null) waitDialog.show();
        }
          	
    	@Override
        protected StringBuffer doInBackground(ArrayList<String>... params)
    	{
            final StringBuffer log = new StringBuffer();
            char logLevel = '0';
            
            try {
            	ArrayList<String> arguments = ((params != null) && (params.length > 0)) ? params[0] : null;
            	
            	//+ for whole log dump (level 'X')
            	if (arguments != null) 
            	{
	            	String temp = arguments.get(arguments.size() - 1).toLowerCase();
	                logLevel = temp.charAt(temp.length() - 1);
	                if (logLevel == 'x')
	                {
	                	String command = "logcat -f " + Helpers.getImportExportDir() + File.separator 
	                		+ "logs" + File.separator + "systemlog_dump_whole.log -v time *:V";
	                	Runtime.getRuntime().exec(command);
	                	return log;
	                }
            	}
            	//- for whole log dump 
            	
            	ArrayList<String> commandLine = new ArrayList<String>();
                commandLine.add("logcat");
                commandLine.add("-d");
           
                if (arguments != null){
                    commandLine.addAll(arguments);
                    fileName = SLD_FILENAME.replaceAll("<NULL>", String.valueOf(logLevel));
                }
                
                Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                while ((line = bufferedReader.readLine()) != null){ 
                    log.append(line);
                    log.append(LINE_SEPARATOR);
                }
            } 
            catch (IOException e){
            	SSElog.sld("System Log State", "FAILED", fileName);
            } 
            return log;
        }
    	
        @Override
        protected void onPostExecute(StringBuffer log)
        {
            int MAX_LOG_MESSAGE_LENGTH = 1048576;
        	if (log != null) {
                if(log.length() > 0)
                {
	        		int keepOffset = Math.max(log.length() - MAX_LOG_MESSAGE_LENGTH, 0);
	                if (keepOffset > 0) {
	                    log.delete(0, keepOffset);
	                }
	            	SSElog.sld(null, "*** System Log Dump (" + fileName + ") ***" + LINE_SEPARATOR + log.toString(), fileName);
                }
            }
            else {
            	SSElog.sld("System Log State", "cannot retrieve system logs", fileName);
            }
        	
        	if(waitDialog != null)
        	{
        		waitDialog.cancel();
        		waitDialog = null;
        	}
        }    
    }
  //- SystemLog Collector
}
