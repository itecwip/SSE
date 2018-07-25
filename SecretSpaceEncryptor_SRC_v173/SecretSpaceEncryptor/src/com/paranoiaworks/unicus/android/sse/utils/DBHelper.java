package com.paranoiaworks.unicus.android.sse.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.paranoiaworks.unicus.android.sse.dao.ApplicationStatusBean;

/**
 * Provides all SQL Database related services
 * 
 * @author Paranoia Works
 * @version 1.2.0
 */ 
public class DBHelper {

	private static SQLiteDatabase db = null;
	private static Context ct = null;
	private static Lock lock = new ReentrantLock(); //over restricted - but some Android versions have quite odd "DB related concurrency behavior"
	
	/** Initialize DB and return SQLiteDatabase object, if exists already - return the existing one */
	public static SQLiteDatabase initDB(Context context)
	{
		lock.lock();
		try {
			if (db != null && db.isOpen()) return db;

			db = context.openOrCreateDatabase("SSE_APP.db", Context.MODE_PRIVATE, null);
			ct = context;
			createTables();
		} finally {
			lock.unlock();
		}
		return db;
	}
	
	/** Get SQLiteDatabase object */
	public static boolean isDBReady()
	{
		lock.lock();
		try {
			return db == null ? false : true;
		} finally {
			lock.unlock();
		}	
	}
	
	/** Get Context */
	public static Context getContext()
	{
		return ct;
	}
	
	
	/** Close DB and clear other related object references */
	public static void killDB()
	{
		lock.lock();
		try {
			if(db != null)
			{
				db.close();
				db = null;
			}
			ct = null;
		} finally {
			lock.unlock();
		}		
	}

	
	/** Get binary data from BLOB_REP table - used mainly for serialized objects */
	public static byte[] getBlobData(String id)
	{	
		return getBlobData(id, null, null);
	}
	
	public static byte[] getBlobData(String id, StringBuffer hashStamp, ArrayList otherOutputData)
	{		
		lock.lock();
		try {
			StringBuffer sql = new StringBuffer();
			String[] ida = {id};
			
			sql.append(" select BLOBDATA, STAMPHASH, VERSION from BLOB_REP ");
			sql.append(" where ID = ? ");
			Cursor cursor = db.rawQuery(sql.toString(), ida);
			if (cursor.getCount() < 1) 
			{
				cursor.close();
				return null;
			}
			cursor.moveToNext();
			byte[] output = cursor.getBlob(cursor.getColumnIndex("BLOBDATA"));
			if(hashStamp != null) hashStamp.append(cursor.getString(cursor.getColumnIndex("STAMPHASH")));
			if(otherOutputData != null) otherOutputData.add(cursor.getInt(cursor.getColumnIndex("VERSION")));
			cursor.close();
			if(output == null) output = new byte[1]; // in case of "DB wrong import" inconsistent
			return output;
		} finally {
			lock.unlock();
		}		
	}
	
	
	/** Delete data from BLOB_REP table */
	public static void deleteBlobData(String id)
	{		
		lock.lock();
		try {
			String sql = "delete from BLOB_REP where ID = ?";
			Object[] obj = {id};
			db.execSQL(sql.toString(), obj);
		} finally {
			lock.unlock();
		}	
	}
	
	
	/** Insert or Update(if exists) data in BLOB_REP table */
	public static void insertUpdateBlobData(String id, byte[] input)
	{
		insertUpdateBlobData(id, input, null, 0);
	}
	
	public static void insertUpdateBlobData(String id, byte[] input, int version)
	{
		insertUpdateBlobData(id, input, null, version);
	}
	
	public static void insertUpdateBlobData(String id, byte[] input, String stampHash, int version)
	{
		lock.lock();
		try {
			StringBuffer sql = new StringBuffer();
			
			Object[] obj = {input, stampHash, version, input.length, id};
			if(getBlobData(id) == null)
			{
				sql.append("insert into BLOB_REP (BLOBDATA, STAMPHASH, VERSION, SIZE, ID) values(?, ?, ?, ?, ?);");
			} 
			else 
			{
				sql.append("update BLOB_REP set BLOBDATA = ?, STAMPHASH = ?, VERSION = ?, SIZE = ?, TIMESTAMP = current_timestamp where id = ?;");
			}
			db.execSQL(sql.toString(), obj);
		} finally {
			lock.unlock();
		}	
	}
	
	
	/** Get list of message names stored in DB - for Message Encryptor */
	public static List<Object> getMessageNamesAndData()
	{	
		lock.lock();
		Cursor cursor = null;
		try {
			ArrayList<Object> data = new ArrayList<Object>();
			
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<Long> lengths = new ArrayList<Long>();
			ArrayList<Long> timestamps = new ArrayList<Long>();
			StringBuffer sql = new StringBuffer();
			
			sql.append(" select NAME, LENGTH, TIMESTAMP from MESSAGE_ARCHIVE order by lower(NAME)");
			
			cursor = db.rawQuery(sql.toString(), null);
			
			if (cursor.getCount() < 1) return null;
			
			int columnIndexName = cursor.getColumnIndex("NAME");
			int columnIndexLength = cursor.getColumnIndex("LENGTH");
			int columnIndexTimestamp = cursor.getColumnIndex("TIMESTAMP");
			
			while (cursor.moveToNext())
			{
				names.add(cursor.getString(columnIndexName));
				lengths.add(cursor.getLong(columnIndexLength));
				timestamps.add(cursor.getLong(columnIndexTimestamp));
				
			}
			
			data.add(names);
			data.add(lengths);
			data.add(timestamps);
			
			return data;		
		} finally {
			cursor.close();
			lock.unlock();
		}		
	}
	
	
	/** Get message from DB - for Message Encryptor */
	public static String getMessage(String name, List<Object> additionalData) throws UnsupportedEncodingException
	{		
		lock.lock();
		try {
			StringBuffer sql = new StringBuffer();
			String[] ida = {name};
			
			sql.append(" select MESSAGE, LENGTH, TIMESTAMP from MESSAGE_ARCHIVE ");
			sql.append(" where NAME = ? ");
			Cursor cursor = db.rawQuery(sql.toString(), ida);
			if (cursor.getCount() < 1) return null;
			cursor.moveToNext();
			byte[] output = cursor.getBlob(cursor.getColumnIndex("MESSAGE"));
			additionalData.add(cursor.getLong(cursor.getColumnIndex("LENGTH")));
			additionalData.add(cursor.getLong(cursor.getColumnIndex("TIMESTAMP")));
			cursor.close();
			return new String(output, "UTF8");
		} finally {
			lock.unlock();
		}	
	}
	
	
	/** Delete message from DB - for Message Encryptor */
	public static void deleteMessage(String name)
	{		
		lock.lock();
		try {
			StringBuffer sql = new StringBuffer();
			Object[] obj = {name};
			
			sql.append(" delete from MESSAGE_ARCHIVE ");
			sql.append(" where NAME = ? ");
			db.execSQL(sql.toString(), obj);
		} finally {
			lock.unlock();
		}
	}
	
	
	/** Insert message to DB - for Message Encryptor */
	public static void insertMessage(String name, String message)
	{	
		lock.lock();
		try {
		StringBuffer sql = new StringBuffer();
		message = message.trim();	
		byte[] messageB = message.getBytes("UTF8");
			
			Object[] obj = {name, messageB, message.length(), System.currentTimeMillis()};
			sql.append("insert into MESSAGE_ARCHIVE (NAME, MESSAGE, LENGTH, TIMESTAMP, FIELD1) values(?, ?, ?, ?, null);");
			db.execSQL(sql.toString(), obj);
		} catch (UnsupportedEncodingException e) {
			// Exception
		} finally {
			lock.unlock();
		}	
	}
	
	
	/** Get ApplicationStatusBean - for "Main Menu" report */
	public static ApplicationStatusBean getAppStatus()
	{
		lock.lock();
		Cursor cursor = null;
		ApplicationStatusBean asb = new ApplicationStatusBean();
		
		try {
			ArrayList<String> variables = new ArrayList<String>();
			StringBuffer sql = new StringBuffer();
					
			sql.append(" select * from APP_STATUS ");
		
			cursor = db.rawQuery(sql.toString(), null);
			if (cursor.getCount() < 1) // application first run
			{
				updateAppStatus();
				cursor = db.rawQuery(sql.toString(), null);
			}
			cursor.moveToNext();
			asb.setPresentRun(cursor.getLong(cursor.getColumnIndex("PRESENT_RUN")));
			asb.setLastRun(cursor.getLong(cursor.getColumnIndex("LAST_RUN")));
			asb.setNumberOfRuns(cursor.getInt(cursor.getColumnIndex("NUMBER_OF_RUNS")));
			asb.setFirstRun(cursor.getLong(cursor.getColumnIndex("FIRST_RUN")));
			asb.setField1(cursor.getString(cursor.getColumnIndex("FIELD1")));
			asb.setChecksum(cursor.getString(cursor.getColumnIndex("CHECKSUM")));
			
			variables.add(Long.toString(asb.getPresentRun()));
			variables.add(Long.toString(asb.getLastRun()));
			variables.add(Integer.toString(asb.getNumberOfRuns()));
			variables.add(Long.toString(asb.getFirstRun()));
			variables.add(asb.getField1());
			if(asb.getChecksum().equals(getVariablesChecksum(variables)))
				asb.setChecksumOk(true);
			return asb;
		} catch (Exception e) {
			// application first run
			return asb;
		} finally {
			cursor.close();
			lock.unlock();
		}	
	}
	
	
	/** Insert or Update(if exists) ApplicationStatus in DB */
	public synchronized static void updateAppStatus()
	{
		lock.lock();
		Cursor cursor = null;
		try {
			StringBuffer sql = new StringBuffer();
			
			sql.append(" select * from APP_STATUS ");
			cursor = db.rawQuery(sql.toString(), null);
			
			if (cursor.getCount() < 1)
			{ 
				ArrayList<String> variables = new ArrayList<String>();
				sql = new StringBuffer();
				sql.append("insert into APP_STATUS values(?, ?, ?, ?, ?, ?);");
				long nowTemp = Calendar.getInstance().getTimeInMillis();
				variables.add(Long.toString(nowTemp));
				variables.add(Long.toString(nowTemp));
				variables.add(Integer.toString(1));
				variables.add(Long.toString(nowTemp));
				variables.add(null);
				variables.add(getVariablesChecksum(variables));
				String[] vrs = new String[variables.size()];
				variables.toArray(vrs);
				
				db.execSQL(sql.toString(), vrs);
				
			} else {
				cursor.moveToNext();
				ArrayList<String> variables = new ArrayList<String>();
				sql = new StringBuffer();
				sql.append("update APP_STATUS set PRESENT_RUN = ?, ");
				sql.append(" LAST_RUN = ?, ");
				sql.append(" NUMBER_OF_RUNS = ?, ");
				sql.append(" FIRST_RUN = ?, ");
				sql.append(" FIELD1 = ?, ");
				sql.append(" CHECKSUM = ?; ");
				variables.add(Long.toString(Calendar.getInstance().getTimeInMillis()));
				variables.add(Long.toString(cursor.getLong(cursor.getColumnIndex("PRESENT_RUN"))));			
				variables.add(Integer.toString(cursor.getInt(cursor.getColumnIndex("NUMBER_OF_RUNS")) + 1));
				variables.add(Long.toString(cursor.getLong(cursor.getColumnIndex("FIRST_RUN"))));
				variables.add(null);
				variables.add(getVariablesChecksum(variables));
				String[] vrs = new String[variables.size()];
				variables.toArray(vrs);
				
				db.execSQL(sql.toString(), vrs);
			}
			
		} finally {
			cursor.close();
			lock.unlock();
		}	
	}
	
	
	/** ApplicationStatus Checksum */
	private static String getVariablesChecksum(List<String> variables)
	{
		lock.lock();
		try {
			String checkSum = "CheckSum";	
			for (int i = 0; i < variables.size(); ++i) checkSum += variables.get(i) + ":";
			checkSum = new String(Encryptor.getMD5Hash(checkSum));
			
			return checkSum;
		} finally {
			lock.unlock();
		}	
	}
	
	
	/** Create all application Tables - if not exist */
	private synchronized static void createTables()
	{
		StringBuffer sql = new StringBuffer();
		
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(" APP_STATUS ");
		sql.append(" (");
		sql.append(" PRESENT_RUN INTEGER, LAST_RUN INTEGER, NUMBER_OF_RUNS INTEGER, FIRST_RUN INTEGER, FIELD1 VARCHAR, CHECKSUM VARCHAR(32)");
		sql.append(" );");
		db.execSQL(sql.toString());
		
		sql = new StringBuffer();		
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(" BLOB_REP ");
		sql.append(" (");
		sql.append(" ID VARCHAR PRIMARY KEY ASC, VERSION INTEGER DEFAULT 0, ");
		sql.append(" CLOBDATA TEXT, BLOBDATA BLOB, ");
		sql.append(" SIZE INTEGER DEFAULT -1, ");
		sql.append(" STAMPHASH VARCHAR(32), ");
		sql.append(" TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP ");
		sql.append(" );");		
		db.execSQL(sql.toString());
		
		sql = new StringBuffer();		
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(" MESSAGE_ARCHIVE ");
		sql.append(" (");
		sql.append(" NAME VARCHAR(32) PRIMARY KEY ASC, MESSAGE BLOB , ");
		sql.append(" FIELD1 VARCHAR ");
		sql.append(" );");		
		db.execSQL(sql.toString());		
		
		if(!columnExist("BLOB_REP", "VERSION"))
			db.execSQL("ALTER TABLE BLOB_REP ADD COLUMN VERSION INTEGER DEFAULT 0;");
		
		if(!columnExist("BLOB_REP", "SIZE"))
			db.execSQL("ALTER TABLE BLOB_REP ADD COLUMN SIZE INTEGER DEFAULT -1;");
		
		if(!columnExist("MESSAGE_ARCHIVE", "TIMESTAMP"))
			db.execSQL("ALTER TABLE MESSAGE_ARCHIVE ADD COLUMN TIMESTAMP INTEGER DEFAULT -1;");
		
		if(!columnExist("MESSAGE_ARCHIVE", "LENGTH"))
			db.execSQL("ALTER TABLE MESSAGE_ARCHIVE ADD COLUMN LENGTH INTEGER DEFAULT -1;");
	}
	
	/** Does the column exist in the table? */
	private static boolean columnExist(String tableName, String columnName)
	{
		Cursor cursor = null;
		int index = -1;
		try {
			cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0", null);
			index = cursor.getColumnIndex(columnName);
		} 
		finally {
			cursor.close();
		}	

		return !(index == -1);
	}
}
