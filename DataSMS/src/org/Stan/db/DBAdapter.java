package org.Stan.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Database Adapter class
 * 
 * DBAdapter class is used to wrap database adapter which is to engage with the database.
 */

public class DBAdapter {
	// Database version is used to version control
	private static final int DATABASE_VERSION = 1;

	// Database information
	private static final String DATABASE_NAME = "org.Stan.DataSMS.db";

	// Database tables
	private static final String ACCOUNTS_TABLE_NAME = "accounts";
	private static final String MESSAGES_TABLE_NAME = "messages";

	// SQL command used for table creation
	private final static String TABLE_ACCOUNTS_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ ACCOUNTS_TABLE_NAME
			+ " ("
			+ Account.ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Account.NAME
			+ " TEXT,"
			+ Account.NUMBER + " TEXT," + Account.MASTERKEY + " TEXT" + ");";

	private final static String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ MESSAGES_TABLE_NAME
			+ " ("
			+ Message.ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ Message.FROMORTO
			+ " TEXT,"
			+ Message.NUMBER
			+ " TEXT,"
			+ Message.TIME
			+ " IEXT,"
			+ Message.MILLIONTIME
			+ " TEXT,"
			+ Message.READABLE
			+ " BOOLEAN,"
			+ Message.MSG + " TEXT" + ");";

	// Other reference links
	private Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;

	/**
	 * Reference to this class E.g. DBAdapter newDBAdapter = new
	 * DBAdapter(this);
	 * 
	 */

	public DBAdapter(Context context) {
		this.context = context;
	}

	class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create new tables
			db.execSQL(TABLE_ACCOUNTS_CREATE);
			db.execSQL(TABLE_MESSAGES_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Update to higher version database
			db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
			onCreate(db);
		}

	}

	private boolean opened = false;

	/**
	 * Open database
	 * 
	 * @return database adapter
	 * @throws SQLException
	 */
	public DBAdapter open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		db = databaseHelper.getWritableDatabase();
		opened = true;
		return this;
	}

	// Close database
	public void close() {
		databaseHelper.close();
		opened = false;
	}

	public boolean isOpen() {
		return opened;
	}

	// Account Table
	public long insertAccount(Account acc) {
		return db.insert(ACCOUNTS_TABLE_NAME, null, acc.getContentValues());
	}

	public ArrayList<Account> getAllAccount() {
		ArrayList<Account> accountList = new ArrayList<Account>();
		Cursor cs = db.query(ACCOUNTS_TABLE_NAME, new String[] { Account.NAME,
				Account.NUMBER, Account.MASTERKEY }, null, null, null, null,
				Account.NAME + " DESC");
		if (cs.moveToFirst()) {
			do {
				Account acc = new Account();
				acc.setName(cs.getString(cs.getColumnIndex(Account.NAME)));
				acc.setNumber(cs.getString(cs.getColumnIndex(Account.NUMBER)));
				acc.setMasterKey(cs.getString(cs
						.getColumnIndex(Account.MASTERKEY)));
				accountList.add(acc);
			} while (cs.moveToNext());
		}
		if (cs != null && !cs.isClosed()) {
			cs.close();
		}
		return accountList;
	}

	public Account getAccount(String accNumber) {
		Account account = new Account();
		Cursor cs = db.query(ACCOUNTS_TABLE_NAME, new String[] { Account.NAME,
				Account.NUMBER, Account.MASTERKEY }, Account.NUMBER + "=?",
				new String[] { accNumber }, null, null, Account.NAME + " DESC");
		if (cs.moveToFirst()) {
			account.setName(cs.getString(cs.getColumnIndex(Account.NAME)));
			account.setNumber(cs.getString(cs.getColumnIndex(Account.NUMBER)));
			account.setMasterKey(cs.getString(cs
					.getColumnIndex(Account.MASTERKEY)));
		}
		return account;
	}

	public boolean deletAllAccount() {
		return db.delete(ACCOUNTS_TABLE_NAME, null, null) > 0;
	}

	public boolean deletOneAccount(Account acc) {
		return db.delete(ACCOUNTS_TABLE_NAME, Account.NAME + "=?",
				new String[] { acc.getName() }) > 0;
	}

	// Message table

	private final static String[] msg_projection = { Message.FROMORTO,
			Message.NUMBER, Message.TIME, Message.MILLIONTIME,
			Message.READABLE, Message.MSG };

	public long insertMessage(Message msg) {
		return db.insert(MESSAGES_TABLE_NAME, null, msg.getContentValues());
	}

	public ArrayList<Message> getAllMsgs() {
		ArrayList<Message> allMsgs = new ArrayList<Message>();
		Cursor cs = db.query(MESSAGES_TABLE_NAME, msg_projection, null, null,
				null, null, Message.MILLIONTIME + " DESC");
		if (cs.moveToFirst()) {
			do {
				Message msg = new Message();
				msg.setFromOrTo(cs.getString(cs
						.getColumnIndex(Message.FROMORTO)));
				msg.setNumber(cs.getString(cs.getColumnIndex(Message.NUMBER)));
				msg.setTime(cs.getString(cs.getColumnIndex(Message.TIME)));
				msg.setMillionTime(cs.getString(cs
						.getColumnIndex(Message.MILLIONTIME)));
				msg.setMsg(cs.getString(cs.getColumnIndex(Message.MSG)));
				msg.setReadable(cs.getString(
						cs.getColumnIndex(Message.READABLE)).equals("TRUE") ? true
						: false);
				allMsgs.add(msg);
			} while (cs.moveToNext());
		}
		if (cs != null && !cs.isClosed()) {
			cs.close();
		}
		return allMsgs;
	}

	public ArrayList<Message> getOneContactMsgs(Account acc) {
		ArrayList<Message> allMsgs = new ArrayList<Message>();
		Cursor cs = db.query(MESSAGES_TABLE_NAME, msg_projection,
				Message.NUMBER + "=?", new String[] { acc.getNumber() }, null,
				null, Message.MILLIONTIME + " DESC");
		if (cs.moveToLast()) {
			do {
				Message msg = new Message();
				msg.setFromOrTo(cs.getString(cs
						.getColumnIndex(Message.FROMORTO)));
				msg.setNumber(cs.getString(cs.getColumnIndex(Message.NUMBER)));
				msg.setTime(cs.getString(cs.getColumnIndex(Message.TIME)));
				msg.setMillionTime(cs.getString(cs
						.getColumnIndex(Message.MILLIONTIME)));
				msg.setMsg(cs.getString(cs.getColumnIndex(Message.MSG)));
				msg.setReadable(cs.getString(
						cs.getColumnIndex(Message.READABLE)).equals("true") ? true
						: false);
				allMsgs.add(msg);
			} while (cs.moveToPrevious());
		}
		if (cs != null && !cs.isClosed()) {
			cs.close();
		}
		return allMsgs;
	}

	public boolean deletAllMsg() {
		return db.delete(MESSAGES_TABLE_NAME, null, null) > 0;
	}

	public boolean deletOneContactMsg(Account acc) {
		return db.delete(MESSAGES_TABLE_NAME, Message.NUMBER + "=?",
				new String[] { acc.getName() }) > 0;
	}

	public boolean deletOneMsg(Message msg) {
		return db.delete(MESSAGES_TABLE_NAME, Message.ID + "=?",
				new String[] { String.valueOf(msg.getId()) }) > 0;
	}

	public boolean markMsgAsRead(Account acc) {
		ContentValues args = new ContentValues();
		args.put(Message.READABLE, true);
		return db.update(MESSAGES_TABLE_NAME, args, Message.NUMBER + "=?",
				new String[] { acc.getName() }) > 0;
	}

}
