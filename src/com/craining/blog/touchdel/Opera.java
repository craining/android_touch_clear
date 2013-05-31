package com.craining.blog.touchdel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.craining.blog.touchdel.widget.DelWidgetProvider;

public class Opera {
	
	public static final String[] AUTHOR_EMAIL_ADDRESS = {"craining@163.com"};
	public static final String AUTHOR_EMAIL_SUB = "TouchClear-1.0";

	public static final File BUFILE = new File("/sdcard/.touchDelTag/delpaths.db");
	public static final File TEMPDBFILE = new File("/sdcard/.touchDelTag/temp_delpaths.db");

	public static final File BUPATH = new File("/sdcard/.touchDelTag/");
	public static final File DBFILE = new File("data/data/com.craining.blog.touchdel/databases/delpaths.db");
	public static final File FILE_REMEMBER = new File("data/data/com.craining.blog.touchdel/remember");

	public static final File WIDGET_EXIST = new File("data/data/com.craining.blog.touchdel/widgetTag");

	public static final String UPDATESERVICE = "com.craining.blog.touchdel.widget.UpdateWidgetService";

	private static long size = 0;

	/**
	 * 显示toast
	 * 
	 * @param con
	 *            上下文
	 * @param str
	 *            显示的字符串
	 * @param shortOrLong
	 *            显示时间：0-短， 1-长
	 */
	public static void DisplayToast(Context con, String str, int shortOrLong) {
		Toast toast = null;
		if (toast == null) {
			toast = Toast.makeText(con, "", shortOrLong);
		}
		toast.setText(str);
		toast.show();
	}

	/**
	 * 递归获得文件或目录的大小
	 * 
	 * @param dir
	 * @return
	 */
	public static long getFileSize(File dir) {

		try {
			if (!dir.isDirectory()) {
				setSize(getSize() + dir.length());
				// Log.e("", dir.toString());
			} else {
				for (File file : dir.listFiles()) {
					if (!file.isDirectory()) {
						// Log.e("", file.toString());
						setSize(getSize() + file.length());
					} else {
						getFileSize(file);// 递归
					}
				}
			}
		} catch (Exception e) {
			setSize(-1);
		}

		return getSize();
	}

	/**
	 * 删除一个文件或目录（可以是非空目录）
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean delDir(File dir) {
		if (dir == null || !dir.exists()) {
			return false;
		}
		if (dir.isFile() || dir.listFiles() == null) {
			dir.delete();
			return true;
		} else {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					file.delete();
				} else if (file.isDirectory()) {
					delDir(file);// 递归
				}
			}
			dir.delete();
		}

		return true;
	}

	/**
	 * 删除一个文件
	 * 
	 * @param con
	 * @param id
	 * @param a
	 */
	public static void delOneFile(Context con, int id, ArrayList<String> a) {

		File todelFile = new File(a.get(id - 2));
		Opera.delDir(todelFile);
		if (!todelFile.exists()) {
			Opera.DisplayToast(con, con.getString(R.string.delfilesuccess), 0);
		} else {
			Opera.DisplayToast(con, con.getString(R.string.delfilefail), 0);
		}
	}

	/**
	 * 删除所有文件
	 * 
	 * @param con
	 * @param a
	 */
	public static void delAllFiles(Context con, ArrayList<String> a) {
		boolean delSuccess = true;
		for (int i = 0; i < a.size(); i++) {
			File todelFile = new File(a.get(i));
			try {
				Opera.delDir(todelFile);
			} catch (Exception e) {
				delSuccess = false;
				break;
			}
			if (todelFile.exists()) {
				delSuccess = false;
			}
		}
		if (delSuccess) {
			Opera.DisplayToast(con, con.getString(R.string.delallfilessuccess), 0);
		} else {
			Opera.DisplayToast(con, con.getString(R.string.delallfilesfail), 0);
		}
	}

	/**
	 * 拷贝一个文件,srcFile源文件，destFile目标文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFileTo(File srcFile, File destFile) throws IOException {
		if (srcFile.isDirectory() || destFile.isDirectory()) {
			return false;// 判断是否是文件
		}
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile);
		int readLen = 0;
		byte[] buf = new byte[1024];
		while ((readLen = fis.read(buf)) != -1) {
			fos.write(buf, 0, readLen);
		}
		fos.flush();
		fos.close();
		fis.close();

		return true;
	}

	/**
	 * 判断存储卡是否可用
	 * 
	 * @return
	 */
	public static boolean isSdPresent() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 恢复数据
	 * 
	 * @param con
	 * @return
	 */
	public static boolean goBackData(Context con) {
		if (isSdPresent()) {
			if (BUFILE.exists()) {
				try {
					copyFileTo(BUFILE, DBFILE);
					Opera.DisplayToast(con, con.getString(R.string.datagoback_success), 0);
				} catch (IOException e) {
					Opera.DisplayToast(con, con.getString(R.string.datagoback_fail), 0);
					return false;
				}
			} else {
				Opera.DisplayToast(con, con.getString(R.string.backupfilemissing), 0);
				return false;
			}
		} else {
			Opera.DisplayToast(con, con.getString(R.string.sdcardunpresent), 0);
			return false;
		}

		return true;
	}

	/**
	 * 导入备份
	 * 
	 * @param con
	 * @return
	 */
	public static boolean importData(Context con, ArrayList<String> path, ArrayList<String> pathorfile) {
		if (isSdPresent()) {
			if (BUFILE.exists()) {
				try {
					/* 获取已经备份的数据 */
					copyFileTo(DBFILE, TEMPDBFILE);// 将数据库文件移到临时目录中
					copyFileTo(BUFILE, DBFILE);// 将备份的数据读取出来
					DataBaseAdapter db_adapter = new DataBaseAdapter(con);
					db_adapter.open();
					ArrayList<String> newArray_paths = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_PATH);
					ArrayList<String> newArray_fileorpath = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_FILE_PATH);
					db_adapter.close();
					copyFileTo(TEMPDBFILE, DBFILE);// 将临时目录里的数据库文件放回
					TEMPDBFILE.delete();
					/* 将备份数据写入数据库 */
					db_adapter.open();
					for (int i = 0; i < newArray_paths.size(); i++) {
						boolean same = false;
						for (int j = 0; j < path.size(); j++) {
							if (path.get(j).equals(newArray_paths.get(i))) {
								same = true;
							}
						}
						if (!same) {
							db_adapter.insertData(newArray_paths.get(i), newArray_fileorpath.get(i));
						}
					}
					db_adapter.close();
					Opera.DisplayToast(con, con.getString(R.string.dataimport_success), 0);
					return true;

				} catch (IOException e) {
					Opera.DisplayToast(con, con.getString(R.string.dataimport_fail), 0);
					e.printStackTrace();
				}

			} else {
				Opera.DisplayToast(con, con.getString(R.string.backupfilemissing), 0);
			}
		} else {
			Opera.DisplayToast(con, con.getString(R.string.sdcardunpresent), 0);
		}

		return false;
	}

	/**
	 * 导出数据
	 * 
	 * @param con
	 * @return
	 */
	public static boolean exportData(Context con) {
		if (isSdPresent()) {
			if (!BUPATH.exists()) {
				BUPATH.mkdir();
			}
			try {
				// Log.e(databaseFile.toString(), backupFilePath.toString());
				if (copyFileTo(DBFILE, BUFILE)) {
					Opera.DisplayToast(con, con.getString(R.string.backupsuccess), 0);
				}

			} catch (IOException e) {
				Opera.DisplayToast(con, con.getString(R.string.backuperror), 0);
				e.printStackTrace();
			}
		} else {
			Opera.DisplayToast(con, con.getString(R.string.sdcardunpresent), 0);
		}

		return false;
	}

	/**
	 * 判断文件是否已经删尽
	 * 
	 * @param con
	 * @return
	 */
	public static long filesAllSize(Context con) {
		/* 读取数据库，获得所有文件绝对路径，依次判断是否存在，若存在则累计文件大小 */
		Log.e("Opera", "Get files size!");
		setSize(0);
		long allsize = 0;
		DataBaseAdapter db_adapter = new DataBaseAdapter(con);
		db_adapter.open();
		ArrayList<String> paths = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_PATH);
		db_adapter.close();
		if (paths.size() != 0 || paths != null) {
			for (int k = 0; k < paths.size(); k++) {
				File a = new File(paths.get(k));
				if (a.exists()) {
					allsize = allsize + getFileSize(a);
				}
			}
		}
		setSize(0);

		return allsize;
	}

	/**
	 * 将文件的字节数转换为kb、mb、或gb
	 * 
	 * @param size
	 * @return
	 */
	public static String sizeLongToString(long size) {
		if (size == 0) {
			return "0";
		} else {
			String a = "";
			if (size / 1024 < 1024.0) {
				a = String.format("%.2f", size / 1024.0) + "KB";
			} else if (size / 1048576 < 1024) {
				a = String.format("%.2f", size / 1048576.0) + "MB";
			} else {
				a = String.format("%.2f", size / 1073740824.0) + "GB";
			}
			return a;
		}
	}

	/**
	 * 更新所有AppWidget
	 * 
	 * @param context
	 */
	public static void updateAllWidgets(Context context) {
		AppWidgetManager gm = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = gm.getAppWidgetIds(new ComponentName(context, DelWidgetProvider.class));
		if (appWidgetIds != null) {
			final int N = appWidgetIds.length;
			Log.e("Opera", "+++++UpdateAll======> widgetNum" + N);
			for (int i = 0; i < N; i++) {
				DelWidgetProvider.updateAppWidget(context, gm, appWidgetIds[i]);

			}

		}
	}

	/**
	 * 通过Service的类名来判断是否启动某个服务
	 * 
	 * @param mServiceList
	 * @param className
	 * @return
	 */
	public static boolean serviceIsRunning(Context context, String serviceName) {

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> mServiceList = activityManager.getRunningServices(100);

		for (int i = 0; i < mServiceList.size(); i++) {
			if (serviceName.equals(mServiceList.get(i).service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static void tryStartService(Context context) {
		/* 存在Widget时， 判断更新服务是否开启，若未开启，则开启.... */
		if (WIDGET_EXIST.exists()) {
			Log.e("WIDGET", "file existed!");
			if (!Opera.serviceIsRunning(context, Opera.UPDATESERVICE)) {
				context.startService(new Intent(Opera.UPDATESERVICE));
				Log.e("WIDGET", "service is started!");
			}
		} else {
			Log.e("WIDGET", "file not existed!");
			if (Opera.serviceIsRunning(context, Opera.UPDATESERVICE)) {
				context.stopService(new Intent(Opera.UPDATESERVICE));
				Log.e("WIDGET", "service is stoped!");
			}
		}
	}

	private static void setSize(long size) {
		Opera.size = size;
	}

	private static long getSize() {
		return size;
	}
}
