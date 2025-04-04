package com.gncbrown.GetMeBack.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = MySQLiteHelper.class.getSimpleName();

	// Fields
	private static final String KEY_ENTRY = "entry";
	private static final String KEY_ENTRY_DATE = "entry_date";
	private static final String KEY_ENTRY_LEVEL = "entry_level";

	// Tables
	private static final String TABLE_LOG = "log";
	private static final String[] TABLES = { TABLE_LOG };
	private static final String[] LOG_COLUMNS = { KEY_ENTRY, KEY_ENTRY_DATE };
	private static final String[] NEW_LOG_COLUMNS = {
			KEY_ENTRY,
			KEY_ENTRY_DATE,
			KEY_ENTRY_LEVEL
	};


	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "GetMeBackLogDB";
	
	//private static final String DATE_QUERY_STRING = "datetime(" + KEY_ENTRY_DATE + ", 'localtime')";
	private static final String DATE_QUERY_FIELD = KEY_ENTRY_DATE;

	private Context context;
	private static MySQLiteHelper instance;
	private Preferences prefs;
	private int logFileLimit;

	private MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;

		prefs = new Preferences(context);
		logFileLimit = (int)prefs.retrieveFromPreferences("LogFileLimit");
	}

	public static synchronized MySQLiteHelper getInstance(Context context) {
		if (instance == null) {
			instance = new MySQLiteHelper(context.getApplicationContext());
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		createLogTable(db);
		
		addLogEntry(db, Logger.LogLevel.Warning, "Create new database");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade, oldVersion=" + oldVersion
				+ ", newVersion=" + newVersion);

		if (oldVersion <= 3) {
			try {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOG);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			createLogTable(db);
		}
		
		addLogEntry(db, Logger.LogLevel.Warning, "Upgrade database: oldVersion=" + oldVersion
				+ ", newVersion=" + newVersion);
	}

	public static int getDatabaseVersion() {
		return DATABASE_VERSION;
	}

	/*
	 * TABLE manipulation
	 */

	private boolean createLogTable_v1(SQLiteDatabase db) {
		String CREATE_LOG_TABLE = "CREATE TABLE " + TABLE_LOG + " ( "
				+ KEY_ENTRY + " TEXT, " 
				+ KEY_ENTRY_DATE
				+ " DATETIME DEFAULT CURRENT_TIMESTAMP PRIMARY KEY)";
		try {
			db.execSQL(CREATE_LOG_TABLE);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean createLogTable_v2(SQLiteDatabase db) {
		String CREATE_LOG_TABLE = "CREATE TABLE " + TABLE_LOG + " ( "
				+ KEY_ENTRY + " TEXT, " 
				+ KEY_ENTRY_DATE + " TEXT, " //" PRIMARY KEY)";
				+ "PRIMARY KEY (" + KEY_ENTRY_DATE + ", " + KEY_ENTRY + ") )";
		try {
			db.execSQL(CREATE_LOG_TABLE);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}


	private boolean createLogTable(SQLiteDatabase db) {
		String CREATE_LOG_TABLE = "CREATE TABLE " + TABLE_LOG + " ( "
				+ KEY_ENTRY + " TEXT, "
				+ KEY_ENTRY_DATE + " TEXT, "
				+ KEY_ENTRY_LEVEL + " TEXT, "
				+ "PRIMARY KEY (" + KEY_ENTRY_DATE + ", " + KEY_ENTRY + ") )";
		try {
			db.execSQL(CREATE_LOG_TABLE);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/*
	 * LOG manipulation
	 */
	public long addLogEntry(String entry, Logger.LogLevel level) {
		//Log.d(TAG, "addLogEntry, level=" + level + ", entry=" + entry);

		ContentValues values = new ContentValues();
		values.put(KEY_ENTRY, entry);
		values.put(KEY_ENTRY_DATE, Utils.getDateTime());
		values.put(KEY_ENTRY_LEVEL, level.name());

		long status = -1;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			status = addLogEntry(db, level, entry);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		//Log.d(TAG, "addLogEntry, status=" + status);
		return status;
	}

	public long addLogEntry(SQLiteDatabase db, Logger.LogLevel level, String entry) {
		String entryDate = Utils.getDateTime();

		ContentValues values = new ContentValues();
		values.put(KEY_ENTRY, entry);
		values.put(KEY_ENTRY_DATE, entryDate);
		values.put(KEY_ENTRY_LEVEL, level.name());

		long status = -1;
		try {
			status = db.insert(TABLE_LOG, null, values);

			pruneLogEntries(db, logFileLimit);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		Log.d(TAG, String.format("addLogEntry[status=%s], date=%s, entry=%s, level=%s",
//				status, entryDate, entry, level.name()));
		return status;
	}

	public void clearLogEntries() {
		//Log.d(TAG, "clearLogEntries");
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.delete(TABLE_LOG, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}

		Log.d(TAG, "clearLogEntries complete.");
	}

	public int pruneLogEntries(int logLimit) {
		int pruned = 0;
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			pruned = pruneLogEntries(db, logLimit);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pruned;
	}

	public int pruneLogEntries(SQLiteDatabase db, int logLimit) {
		//Log.d(TAG, "pruneLogEntries");
		String rowCountQuery = String.format("SELECT COUNT(*) AS count FROM %s", TABLE_LOG);
		int rowCount = 0;
		int pruned = 0;

		try {
			Cursor cursor = db.rawQuery(rowCountQuery, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					rowCount = cursor.getInt(0);
				} while (cursor.moveToNext());

				if (rowCount > logLimit) {
					pruned = rowCount-logLimit;
					String purgeQuery = String.format("SELECT ROWID FROM %s ORDER BY %s ASC LIMIT %d",
							TABLE_LOG, KEY_ENTRY_DATE, pruned);
					List<String> rowIds = getQueryResults(db, purgeQuery, false);
					String purgeCommand = String.format("DELETE FROM %s WHERE ROWID IN (%s)",
						TABLE_LOG, String.join(", ", rowIds));
					db.execSQL(purgeCommand);
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Log.d(TAG, "pruneLogEntries returned " + pruned);
		return pruned;
	}

	public List<String> getLogEntries(Boolean reverse) {
		//Log.d(TAG, "MySQLiteHelper.getLogEntries");
		List<String> entries = new LinkedList<String>();
		String query = "SELECT " + DATE_QUERY_FIELD + ", "
				+ KEY_ENTRY + " FROM " + TABLE_LOG + " ORDER BY "
				+ KEY_ENTRY_DATE + (reverse ? " DESC" : " ASC");

		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String entryDate = cursor.getString(0);
					String entry = cursor.getString(1);
					entries.add("[" + entryDate + "] " + entry);
				} while (cursor.moveToNext());
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		//Log.d(TAG, "getLogEntries complete.");
		return entries;
	}

	public String getLogEntriesAsString(Boolean reverse, String filter) {
		//Log.d(TAG, "getLogEntriesAsString");
		StringBuilder entries = new StringBuilder();
		String query = "SELECT " + DATE_QUERY_FIELD + ", "
				+ KEY_ENTRY + " FROM " + TABLE_LOG 
				+ ("".equals(filter) ? "" : " WHERE " + KEY_ENTRY + " LIKE \"%" + filter + "%\"")
				+ " ORDER BY "
				+ KEY_ENTRY_DATE + (reverse ? " DESC" : " ASC");

		String delim = "";
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String entryDate = cursor.getString(0);
					String entry = cursor.getString(1);
					entries.append(delim + "[" + entryDate + "] " + entry);
					delim = Utils.NL;
				} while (cursor.moveToNext());
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		//Log.d(TAG, "getLogEntriesAsString complete, entries=" + entries.toString());
		return entries.toString();
	}
	
	public List<String> getLogEntries() {
		return getLogEntries(false);
	}

	public List<LogEntry> getEntriesAsLogEntries(Boolean reverse, String filter, boolean colorize) {
		//Log.d(TAG, "MySQLiteHelper.getLogEntries");
		List<LogEntry> entries = new LinkedList<LogEntry>();
		String query = "SELECT "
				+ DATE_QUERY_FIELD + ", "
				+ KEY_ENTRY + ", "
				+ KEY_ENTRY_LEVEL
				+ " FROM " + TABLE_LOG + " ORDER BY "
				+ KEY_ENTRY_DATE + (reverse ? " DESC" : " ASC");

		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String entryDate = cursor.getString(0);
					String entry = cursor.getString(1)
							//.replaceAll("&", "&amp;")
							.replaceAll("#", "&sect;")
							.replaceAll("<", "&lt;")
							.replaceAll(">", "&gt;");
					String level = cursor.getString(2);
					String entryString = String.format("[%s]%s: %s", entryDate, level, entry);
					if (!"".equals(filter)) {
						if (entryString.toLowerCase().contains(filter.toLowerCase()))
							entries.add(new LogEntry(entry, entryDate, Logger.LogLevel.valueOf(level)));
					} else {
						entries.add(new LogEntry(entry, entryDate, Logger.LogLevel.valueOf(level)));
					}
				} while (cursor.moveToNext());
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
		//Log.d(TAG, "getLogEntries complete.");
		return entries;
	}

	public int pruneLogFile(Context context) {
		MySQLiteHelper db = new MySQLiteHelper(context);
		int pruned = db.pruneLogEntries(logFileLimit);
		db.close();

		return pruned;
	}

	public void appendLogTranscript(Context context, Logger.LogLevel logLevel, String value) {
		String x = context.getClass().getSimpleName();
		if (Logger.loggable(context, logLevel)) {
			MySQLiteHelper db = new MySQLiteHelper(context);
			db.addLogEntry(value, logLevel);
			db.close();
		}

		pruneLogFile(context);
	}

	public void appendLogTranscript(Context context, String value) {
		MySQLiteHelper db = new MySQLiteHelper(context);
		db.addLogEntry(value, Logger.LogLevel.Info);
		db.close();
	}

	/*
	 * General DB manipulation
	 */
	public List<String> execute(String sql) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			try {
				db.execSQL(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}

		List<String> results = new ArrayList<String>();
		results.add("Complete.");
		//Log.d(TAG, "insert, sql=" + sql + " execute complete.");
		return results;
	}

	public List<String> getQueryResults(SQLiteDatabase db, String query, boolean showFieldName) {
		//Log.d(TAG, "getQueryResults; query=" + query);
		List<String> results = new LinkedList<String>();

		try {
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String result = "";
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						result += (showFieldName ? cursor.getColumnName(i) + "=" : "")
								+ cursor.getString(i) + ", ";
					}
					result = result.substring(0, result.lastIndexOf(","));
					results.add(result);
				} while (cursor.moveToNext());
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Log.d(TAG, "getQueryResults; returns size=" + results.size());
		return results;
	}

	private String getColumnsAsString(String tableName) {
		String[] columnArray = null;
		if (TABLE_LOG.equalsIgnoreCase(tableName))
			columnArray = LOG_COLUMNS;

		StringBuilder sb = new StringBuilder();
		for (String c : columnArray) {
			if (sb.length() != 0)
				sb.append(", ");
			sb.append(c);
		}
		return sb.toString();
	}

	public String[] getTables() {
		return TABLES;
	}
}