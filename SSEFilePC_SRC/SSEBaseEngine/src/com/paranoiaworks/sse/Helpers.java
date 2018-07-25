package com.paranoiaworks.sse;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

public class Helpers {
	
	public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
	public static final String DATE_FORMAT_DATEONLY = "dd/MM/yyyy";
	public static final String REGEX_REPLACEALL_LASTDOT = "\\.(?!.*\\.)";
	
	public static final String UNIX_FILE_SEPARATOR = "/";
	public static final String WINDOWS_FILE_SEPARATOR = "\\";
	
	
	public static byte[] xorit(byte[] text, byte[] passPhrase)
	{		
		if (passPhrase.length == 0) passPhrase = "x".getBytes();
		byte[] outputBuffer = new byte[text.length];
		int counter = 0;
		for (int i = 0; i < text.length; ++i)
		{
			byte a = text[i];
			byte b = passPhrase[counter];
			outputBuffer[i] = (byte)(a ^ b);	
			++counter;
			if (counter == passPhrase.length) counter = 0;
		}		
		return outputBuffer;
	}
	
	public static byte[] concat(byte[]... args) 
	{
		int fulllength = 0;
		for (byte[] arrItem : args) 
		{
			fulllength += arrItem.length;
        }
		byte[] retArray = new byte[fulllength];
		int start = 0;
		for (byte[] arrItem : args) 
		{
			System.arraycopy(arrItem, 0, retArray, start, arrItem.length);
			start += arrItem.length;
		}
		return retArray;
	}
	
	public static byte[] getSubarray(byte[] array, int offset, int length) 
	{
		byte[] result = new byte[length];
		System.arraycopy(array, offset, result, 0, length);
		return result;
	}

	public static String removeExt (String fileName, String extension)
    {
    	String name = fileName;
    	if (fileName.endsWith("." + extension))
    		name = name.substring(0, name.lastIndexOf('.')); 		
    	return name;
    }
	
	public static String getFirstDirFromFilepath(String filepath)
    {
		filepath = convertToCurrentFileSeparator(filepath);
		String[] temp = filepath.split(Pattern.quote(File.separator));
    	if(temp[0].equals("") && temp.length > 1) return temp[1];
    	return temp[0];
    }
	
	public static String getFirstDirFromFilepathWithLFS(String filepath) //leading file separator (/...)
    {
		filepath = convertToCurrentFileSeparator(filepath);
		if(regexGetCountOf(filepath, File.separator) == 1) return filepath;
		String[] temp = filepath.split(Pattern.quote(File.separator));
    	if(temp[0].equals("") && temp.length > 1) return File.separator + temp[1];
    	return File.separator + temp[0];
    }
	
	public static String[] listToStringArray (List<String> strings)
    {
		String[] sList = new String[strings.size()];
		for(int i = 0; i < strings.size(); ++i)
		sList[i] = strings.get(i);
		return sList;
    }
	
	public static String[] fileListToNameStringArray (List<File> files)
    {
		String[] sList = new String[files.size()];
		for(int i = 0; i < files.size(); ++i)
		sList[i] = files.get(i).getName();
		return sList;
    }
	
	public static long getDirectorySize(File directory) 
	{
		int totalFolder = 0, totalFile = 0;
		long foldersize = 0;

		totalFolder++; 
		File[] filelist = directory.listFiles();
		if(filelist == null) return -1;
		for (int i = 0; i < filelist.length; i++) 
		{
			if (filelist[i].isDirectory()) 
			{
				foldersize += getDirectorySize(filelist[i]);
			} else {
				totalFile++;
				foldersize += filelist[i].length();
			}
		}
		return foldersize;
	}
	
	public static long getDirectorySizeWithInterruptionCheck(File directory) throws InterruptedException 
	{
		int totalFolder = 0, totalFile = 0;
		long foldersize = 0;

		totalFolder++; 
		File[] filelist = directory.listFiles();
		if(filelist == null) throw new InterruptedException("DirectorySize: FileList is NULL");
		for (int i = 0; i < filelist.length; i++) 
		{
			if (filelist[i].isDirectory()) 
			{
				long tempSize = getDirectorySizeWithInterruptionCheck(filelist[i]);
				if(tempSize == -1) return -1;
				foldersize += tempSize;
			} else {
				totalFile++;
				foldersize += filelist[i].length();
			}
			if (Thread.interrupted())
			{
				throw new InterruptedException("DirectorySize: Thread Interrupted");
			}
		}
		return foldersize;
	}
	
	public static boolean deleteDirectory(File directory) 
	{
		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory())
			return false;

		String[] list = directory.list();

		if (list != null) 
		{
			for (int i = 0; i < list.length; i++) 
			{
				File entry = new File(directory, list[i]);

				if (entry.isDirectory())
				{
					if (!deleteDirectory(entry))
						return false;
				}
				else
				{
					if (!entry.delete())
						return false;
				}
			}
		}
		return directory.delete();
	}
	
	public static String getFormatedFileSize(long fileSize) 
	{
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(2);
		formatter.setMinimumFractionDigits(2);
		double fileSizeD = fileSize;
		if(fileSizeD < 1024) return ((long)fileSizeD + " B");
		fileSizeD = fileSizeD / 1024;
		if(fileSizeD < 1024) return (formatter.format(fileSizeD) + " kB");
		fileSizeD = fileSizeD / 1024;
		if(fileSizeD < 1024) return (formatter.format(fileSizeD) + " MB");
		fileSizeD = fileSizeD / 1024;
		if(fileSizeD < 1024) return (formatter.format(fileSizeD) + " GB");
		fileSizeD = fileSizeD / 1024;
		if(fileSizeD < 1024) return (formatter.format(fileSizeD) + " TB");
		fileSizeD = fileSizeD / 1024;
		if(fileSizeD < 1024) return (formatter.format(fileSizeD) + " PB");
		return (formatter.format(fileSizeD / 1024) + " EB");	
	}
	
	public static String getFormatedDate(long time) 
	{
		return getFormatedDate(time, null);
	}
	
	public static String getFormatedDate(long time, String pattern) 
	{
		if(pattern == null) pattern = DATE_FORMAT;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(time);
	}
	
	public static String replaceLastDot(String text, String replacement) 
	{
		return text.replaceAll(REGEX_REPLACEALL_LASTDOT, replacement);
	}
	
	public static int regexGetCountOf(byte[] input, String regex) 
	{   
		return regexGetCountOf(new String(input), regex);
	}
	
	public static int regexGetCountOf(String input, String regex) 
	{            	
		int count = 0;
		Pattern p = Pattern.compile(regex);   
		Matcher m = p.matcher(input);
		while (m.find()) ++count;
		return count;
	}
	
    public static String byteArrayToHexString(byte[] bytes) {
    	char[] hexArray = "0123456789ABCDEF".toCharArray();
    	char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
	public static void saveStringToFile(File file, String text) throws IOException
	{
		try {
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
			out.write(text);
			out.flush();
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
	}
	
	public static String loadStringFromFile(File file) throws IOException
	{
		StringBuilder text = new StringBuilder();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

			String line = bufferedReader.readLine();
			while(line != null){
				text.append(line.trim());
				text.append("\n");
				line = bufferedReader.readLine();
			}      
			bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } 
        return text.toString();
	}
	
	public static byte[] loadBytesFromFile(File file) throws Exception 
	{
		byte[] b = new byte[(int) file.length()];
		FileInputStream fileInputStream = null;
		
        try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(b);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fileInputStream != null) fileInputStream.close();
		}
        
        return b;
	}
	
	public static void saveBytesToFile(File file, byte[] bytes) throws Exception 
	{
        FileOutputStream fos = null;
        try {
			fos = new FileOutputStream(file);
			fos.write(bytes);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fos != null) {
				fos.flush();
				fos.close();
			}
		}
	}
	
	public static String loadStringFromInputStream(InputStream is) throws IOException
	{
		StringBuilder text = new StringBuilder();
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF8"));

			String line = bufferedReader.readLine();
			while(line != null){
				text.append(line.trim());
				text.append("\n");
				line = bufferedReader.readLine();
			}      
			bufferedReader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } 
        return text.toString();
	}
    
    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                 + replacement
                 + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }
    
	public static char getCharFromChosenCharset(String charSet, byte deriveFromValue)
	{
		int byteValue = deriveFromValue + 128;
		int charIndex = (int)Math.round(((double)byteValue / 255) * (charSet.length() - 1));
		return charSet.charAt(charIndex);
	}
	
	public static String getFormatedTime(long time, Locale locale) 
	{
		if(locale == null) locale = Locale.getDefault();
		DateFormat formatter = DateFormat.getTimeInstance(
	            DateFormat.MEDIUM, 
	            locale);
		return formatter.format(new Date(time));
	}
    
    public static <T> T[] concatAll(T[] first, T[]... rest) {
    	  int totalLength = first.length;
    	  for (T[] array : rest) {
    	    totalLength += array.length;
    	  }
    	  T[] result = Arrays.copyOf(first, totalLength);
    	  int offset = first.length;
    	  for (T[] array : rest) {
    	    System.arraycopy(array, 0, result, offset, array.length);
    	    offset += array.length;
    	  }
    	  return result;
    	}
	
	public static String convertToUnixFileSeparator(String path)
	{
		path = path.replaceAll(Pattern.quote(File.separator), UNIX_FILE_SEPARATOR);
		return path;
	}
	
	public static String convertToCurrentFileSeparator(String path)
	{
		path = path.replaceAll(Pattern.quote(UNIX_FILE_SEPARATOR), Matcher.quoteReplacement(File.separator));
		path = path.replaceAll(Pattern.quote(WINDOWS_FILE_SEPARATOR), Matcher.quoteReplacement(File.separator));
		return path;
	}
		
	public static FilenameFilter getOnlyExtFilenameFilter(String extension)
	{
		Helpers h = new Helpers();
		return h.getOnlyExtFF(extension);
	}
	
	private FilenameFilter getOnlyExtFF(String extension)
	{
		OnlyExt oe = new OnlyExt(extension);
		return oe;
	}
	
	private class OnlyExt implements FilenameFilter 
	{ 
		String ext;	
		public OnlyExt(String ext) 
		{ 
			this.ext = "." + ext; 
		}
		
		public boolean accept(File dir, String name) 
		{ 
			return name.endsWith(ext); 
		} 
	}
		
	public static DirectoryStats getDirectoryStats(File directory)
	{
		Helpers h = new Helpers();
		return h.getDirectoryStatsInner(directory);
	}
	
	private DirectoryStats getDirectoryStatsInner(File directory)
	{
		DirectoryStats ds =  new DirectoryStats();
		return ds;
	}
	
	public class DirectoryStats 
	{ 
		public int allFolders = 0, allFiles = 0;
		public int okFolders = 0, okFiles = 0;
	}
	
	public static String createStringWithLength(int length, char charToFill) 
	{
		if (length > 0) {
			char[] array = new char[length];
			Arrays.fill(array, charToFill);
			return new String(array);
		}
		return "";
	}
	
	public static String getShortenedStackTrace(Throwable e, int maxLines) 
	{
	    StringWriter writer = new StringWriter();
	    e.printStackTrace(new PrintWriter(writer));
	    String[] lines = writer.toString().split("\n");
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
	        sb.append(lines[i]).append("\n");
	    }
	    return sb.toString();
	}
	
	public static Dimension getImageDimension(File imgFile) throws IOException 
	{
		int pos = imgFile.getName().lastIndexOf(".");
		if (pos == -1)
			throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
		String suffix = imgFile.getName().substring(pos + 1);
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new FileImageInputStream(imgFile);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				return new Dimension(width, height);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				reader.dispose();
			}
		}
		throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
	}
	
	public static boolean writeTestFile(File dir)
	{
		try {
			String name = null;
			File testFile = null;
			while(true) {
				name = (System.currentTimeMillis() + ".testfile");
				testFile = new File(dir.getAbsolutePath() + File.separator + name);
				if(!testFile.exists()) break;
			}
			OutputStream os = new FileOutputStream(testFile);
			os.write(0);
			os.flush();
			os.close();
			return testFile.delete();	
		} catch (Exception e) {
			return false;
		}		
	}
	
	public static boolean isMac()
	{
	    boolean mac = false;		
		try {
			String OS = System.getProperty("os.name").toLowerCase();
			if(OS != null && OS.indexOf("mac") > -1) mac = true;
		} catch (Exception e) {
			// swallow
		}
	    return mac;
	}
	
	public static String insertTextPeriodically(String text, String insert, int period)
	{
		if(period < 1) return text;
		StringBuilder builder = new StringBuilder(text.length() + insert.length() * (text.length()/period)+1);

		int index = 0;
		String prefix = "";
		while (index < text.length())
		{
			builder.append(prefix);
			prefix = insert;
			builder.append(text.substring(index, 
					Math.min(index + period, text.length())));
			index += period;
		}
		return builder.toString();
	}
	
    /** Load Image from App's Resource */
    public static Image loadImageFromResource(Class usedClass, String name, String extension) 
    {	
    	return loadImageFromResource(usedClass, name, extension, "res/");
    }
    	
    /** Load Image from App's Resource */
    public static Image loadImageFromResource(Class usedClass, String name, String extension, String resPath) 
    {	
    	Image image = null;  
    	
    	try {
    		BufferedImage normalImage = (ImageIO.read(usedClass.getResource("/" + resPath + name + "." + extension)));
    		BufferedImage hdpiImage = (ImageIO.read(usedClass.getResource("/" + resPath + name + ".hdpi." + extension)));
    		
    		int originalWidth = normalImage.getWidth();
    		int originalHeight = normalImage.getHeight();
    		
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
    		
    		double scale = defaultScreen.getDefaultConfiguration().getDefaultTransform().getScaleX();
    		
    		if(scale > 1.0)
    		{
	    		int newWidth = (int)Math.round(originalWidth * scale);
	    		int newHeight = (int)Math.round(originalHeight * scale);	    		
	    		
	    		ResampleOp resizeOp = new ResampleOp(newWidth, newHeight);
	    		resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
	    		Image resapledImage = resizeOp.filter(hdpiImage, null);		
	    		
	    		List<Image> images = new ArrayList<Image>();
				images.add(normalImage);
				images.add(resapledImage);
				
				//image = new BaseMultiResolutionImage(images.toArray(new Image[0]));
				
				Class<?> multiImageClass = Class.forName("java.awt.image.BaseMultiResolutionImage");
				Object multiImageObject = multiImageClass.getConstructor(Image[].class).newInstance((Object)images.toArray(new Image[0]));
				image = (Image)multiImageObject;
    		}
		} catch (Exception e) {}
    	
    	if(image != null) return image;
    	
    	try {
    		image = Toolkit.getDefaultToolkit().getImage(usedClass.getClassLoader().getResource(resPath + name + "." + extension));
    	} catch (Exception e){}
    	
    	try {
    		if(image == null) image = ImageIO.read(usedClass.getResource("/" + resPath + name + "." + extension));
    	} catch (Exception e){}    	
    	
    	return image;   	
    }
}
