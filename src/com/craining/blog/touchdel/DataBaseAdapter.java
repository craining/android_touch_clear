package com.craining.blog.touchdel;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseAdapter {

	public static final String KEY_ID = "_id";
	public static final String KEY_FILE_PATH = "fileorpath";
	public static final String KEY_PATH = "paths";
	private static final String DB_NAME = "delpaths.db";
	private static final String DB_TABLE = "table_path";
	private static final int DB_VERSION = 1;
	private Context mContext = null;

	private static final String DB_CREATE = "CREATE TABLE " + DB_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_PATH + " TEXT," + KEY_FILE_PATH + " TEXT )";
	private static final String DB_DELETE = "delete from " + DB_TABLE + ";";

	private static SQLiteDatabase mSQLiteDatabase = null;
	private DatabaseHelper mDatabaseHelper = null;

	public DataBaseAdapter(Context context) {
		mContext = context;
	}

	public void open() throws SQLException {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mSQLiteDatabase = mDatabaseHelper.getWritableDatabase();
	}

	public void close() {
		mDatabaseHelper.close();
	}

	/**
	 * ����һ������
	 * 
	 * @param path
	 * @param fileorpath
	 * @return
	 */
	public long insertData(String path, String fileorpath) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PATH, path);
		initialValues.put(KEY_FILE_PATH, fileorpath);

		return mSQLiteDatabase.insert(DB_TABLE, KEY_ID, initialValues);
	}

	/**
	 * ɾ��һ������
	 * 
	 * @param rowId
	 * @return
	 */
	public boolean deleteData(long rowId) {
		return mSQLiteDatabase.delete(DB_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * ��ȡ����� column �е��������ݷŵ�һ��List��
	 * 
	 * @param column
	 * @return
	 */
	public static ArrayList<String> getColumnThingsInf(String column) {
		ArrayList<String> getlist = new ArrayList<String>();
		Cursor findColumDate = mSQLiteDatabase.query(DB_TABLE, new String[] { column }, null, null, null, null, null);
		findColumDate.moveToFirst();
		final int Index = findColumDate.getColumnIndexOrThrow(column);
		for (findColumDate.moveToFirst(); !findColumDate.isAfterLast(); findColumDate.moveToNext()) {
			String getOneItem = findColumDate.getString(Index);
			getlist.add(getOneItem);
		}
		return getlist;
	}

	/**
	 * ɾ���������
	 * 
	 * @param db
	 */
	public void clearTable() {
		mSQLiteDatabase.execSQL(DB_DELETE);
	}

	/**
	 * ������Ŀ���ݣ�ɾ��һ����Ŀ
	 * 
	 * @param path
	 */
	public void deleteOnePath(String path) {
		String toDelete = "DELETE FROM " + DB_TABLE + " WHERE " + KEY_PATH + " ='" + path + "'";
		mSQLiteDatabase.execSQL(toDelete);
	}

	public boolean isEmpty() {

		return (getColumnThingsInf(DataBaseAdapter.KEY_PATH) == null || getColumnThingsInf(DataBaseAdapter.KEY_PATH).size() == 0);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			/* ������getWritableDatabase()�� getReadableDatabase()����ʱ �򴴽�һ�����ݿ� */
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/* ���ݿ�û�б�ʱ����һ�� */
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}
}
