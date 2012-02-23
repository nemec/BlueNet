package ec.nem.bluenet;

import java.util.Observable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class MessageDatabase extends Observable {
	private static final String DATABASE_NAME = "messages.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String MESSAGE_TABLE = "Messages";
	public static final String COL_TX_NODE_NAME = "TransmitterName";
	public static final String COL_TX_NODE_ADDR = "TransmitterAddress";
	public static final String COL_RX_NODE_NAME = "ReceiverName";
	public static final String COL_RX_NODE_ADDR = "ReceiverAddress";
	public static final String COL_MESSAGE = "Message";
	public static final String COL_TIME = "Time";
	public static final String COL_COUNT = "Count";

	private static SQLiteDatabase db;
	private static SQLiteStatement insertStmt;
	private static SQLiteStatement deleteStmt;
	private static SQLiteStatement deleteByTXStmt;
	private static SQLiteStatement deleteByRXStmt;
	
	private static MessageDatabase mInstance;
	private static String mLocalNodeName;
	
	private static final String INSERT = 
		"insert into " + MESSAGE_TABLE + " (" + 
		COL_TX_NODE_NAME + ", " +
		COL_TX_NODE_ADDR + ", " +
		COL_RX_NODE_NAME + ", " +
		COL_RX_NODE_ADDR + ", " +
		COL_MESSAGE + ", " +
		COL_TIME +
		") values (?, ?, ?, ?, ?, ?);";
	
	private static final String DELETE = 
		"delete from " + MESSAGE_TABLE + " where " +
		COL_TX_NODE_NAME + " = (?) AND " +
		COL_TX_NODE_ADDR + " = (?) AND " +
		COL_RX_NODE_NAME + " = (?) AND " +
		COL_RX_NODE_ADDR + " = (?) AND " +
		COL_MESSAGE + " = (?) AND " +
		COL_TIME + " = (?);";
	
	private static final String DELETE_BY_TX_ADDR = 
		"delete from " + MESSAGE_TABLE + " where " +
		COL_TX_NODE_ADDR + " = (?);";
	
	private static final String DELETE_BY_RX_ADDR = 
		"delete from " + MESSAGE_TABLE + " where " +
		COL_RX_NODE_ADDR + " = (?);";
	
	private MessageDatabase(Context context) {
		open(context);
	}
	
	public synchronized static MessageDatabase getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new MessageDatabase(context);
		}
		if(!db.isOpen()) {
			open(context);
		}
		return mInstance;
	}
	
	public static void setLocalNode(String name) {
		mLocalNodeName = name;
	}
	
	private static void open(Context context) {
		OpenHelper openHelper = new OpenHelper(context);
		db = openHelper.getWritableDatabase();
		insertStmt = db.compileStatement(INSERT);
		deleteStmt = db.compileStatement(DELETE);
		deleteByTXStmt = db.compileStatement(DELETE_BY_TX_ADDR);
		deleteByRXStmt = db.compileStatement(DELETE_BY_RX_ADDR);
	}
	
	public synchronized void close() {
		if(db.isOpen())
			db.close();
	}

	/**
	 * Inserts a message into the messages table with the given time
	 * and node data.
	 * 
	 * @param txName Device name of the transmitting node
	 * @param txAddr MAC Address of the transmitting node
	 * @param rxName Device name of the receiving node
	 * @param rxAddr MAC Address of the receiving node
	 * @param message The message sent
	 * @param time The system time (in milliseconds) the message was received
	 * @return
	 */
	public synchronized long insert(String txName, String txAddr, String rxName, String rxAddr, 
			String message, long time) {
		insertStmt.bindString(1, txName);
		insertStmt.bindString(2, txAddr);
		insertStmt.bindString(3, rxName);
		insertStmt.bindString(4, rxAddr);
		insertStmt.bindString(5, message);
		insertStmt.bindLong(6, time);
		long result = insertStmt.executeInsert();
	
		setChanged();
		notifyObservers();
		return result;
	}
	
	/**
	 * Deletes from the messages table the exact entry equal to
	 * [txName, txAddr, rxName, rxAddr, message, time].
	 * 
	 * @param txName Device name of the transmitting node
	 * @param txAddr MAC Address of the transmitting node
	 * @param rxName Device name of the receiving node
	 * @param rxAddr MAC Address of the receiving node
	 * @param message The message sent
	 * @param time The system time (in milliseconds) the message was received
	 */
	public synchronized void delete(String txName, String txAddr, String rxName, 
			String rxAddr, String message, long time) {
		deleteStmt.bindString(1, txName);
		deleteStmt.bindString(2, txAddr);
		deleteStmt.bindString(3, rxName);
		deleteStmt.bindString(4, rxAddr);
		deleteStmt.bindString(5, message);
		deleteStmt.bindLong(6, time);
		deleteStmt.execute();
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Deletes all the entries in the table with the given transmitter
	 * MAC address
	 * 
	 * @param txAddr The transmitter MAC address
	 */
	public synchronized void deleteByTXAddress(String txAddr) {
		deleteByTXStmt.bindString(1, txAddr);
		deleteByTXStmt.execute();
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Deletes all the entries in the table with the given receiver
	 * MAC address
	 * 
	 * @param txAddr The receiver MAC address
	 */
	public synchronized void deleteByRXAddress(String rxAddr) {
		deleteByRXStmt.bindString(1, rxAddr);
		deleteByRXStmt.execute();
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Deletes all the messages in the messages table.
	 */
	public synchronized void deleteAllMessages() {
		db.delete(MESSAGE_TABLE, null, null);
		
		setChanged();
		notifyObservers();
	}
	
	/** 
	 * Gets the list of node names in the messages table and the number of messages
	 * that those nodes have sent.
	 * 
	 * @return a Cursor with [txName, count]
	 */
	public synchronized Cursor getContacts() {
		final String sql = "SELECT _id, " + COL_TX_NODE_NAME + ", count(*) AS " + COL_COUNT 
		+ " FROM " + MESSAGE_TABLE +
		" WHERE " + COL_TX_NODE_NAME + " != \"" + mLocalNodeName + "\" " +
		" GROUP BY " + COL_TX_NODE_NAME + 
		" ORDER BY " + COL_TX_NODE_NAME + " DESC";
		
		return db.rawQuery(sql, null);
	}
	
	/**
	 * Returns the list of messages between the given node and this
	 * local node, sorted in ascending order by the time sent.
	 * 
	 * @param remoteAddr The device name of the remote device
	 * @return a Cursor with [txName, message, time]
	 */
	public synchronized Cursor getConversation(String remoteName) {
		final String whereClause = "(" + COL_TX_NODE_NAME + " = '" + remoteName + "' OR " +
			COL_RX_NODE_NAME + " = '" + remoteName + "')";
		final String orderClause = COL_TIME + " ASC";
		
		return db.query(MESSAGE_TABLE, 
				new String[] { "_id", COL_TX_NODE_NAME, COL_MESSAGE, COL_TIME }, 
				whereClause, null, null, null, orderClause);
	}
	
	/**
	 * Gets the MAC address of the given device name.  Searches for rows where
	 * this device name was the transmitter.
	 * 
	 * @param deviceName Name of the device from which to get the address
	 * @return the MAC address, or null
	 */
	public synchronized String getAddressByName(String deviceName) {
		Cursor cursor;
		String address = null;
		
		final String whereClause = COL_TX_NODE_NAME + " = '" + deviceName + "'";
		cursor = db.query(true, MESSAGE_TABLE, new String[] {COL_TX_NODE_ADDR},
				whereClause, null, null, null, null, null);
		
		if(cursor.moveToFirst()) {
			address = cursor.getString(cursor.getColumnIndex(COL_TX_NODE_ADDR));
		}
		
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return address;
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String cmd =
				"create table " + MESSAGE_TABLE +
				" (_id integer primary key autoincrement , " +
				COL_TX_NODE_NAME + " text, " +
				COL_TX_NODE_ADDR + " text, " +
				COL_RX_NODE_NAME + " text, " +
				COL_RX_NODE_ADDR + " text, " +
				COL_MESSAGE + " text, " +
				COL_TIME + " integer); ";
			db.execSQL(cmd);
        }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Just drop the table and recreate it, no need for advanced migrations here.
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
			onCreate(db);
		}
	}
}
