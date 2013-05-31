package com.craining.blog.touchdel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.craining.blog.touchdel.addpath.Explorer;

public class DelConfig extends Activity {

	private ListView listview_main;
	public static ArrayList<String> Array_paths = new ArrayList<String>();
	public static ArrayList<String> Array_fileorpath = new ArrayList<String>();
	private Button btn_add;
	private Button btn_exit;
	private Button btn_delAll;
	private Button btn_help;
	private TextView text_hint;
	private Menu m_menu;

	private Thread thread_updateWidget = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		listview_main = (ListView) findViewById(R.id.listview_main);
		btn_add = (Button) findViewById(R.id.btn_addpath);
		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_delAll = (Button) findViewById(R.id.btn_delall);
		btn_help = (Button) findViewById(R.id.btn_help);
		text_hint = (TextView) findViewById(R.id.text_delconfig);

		showList();

		Opera.tryStartService(this);// 后台更新Widget服务

		btn_help.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				/* 帮助 */
				startActivity(new Intent(DelConfig.this, DelHelp.class));
			}
		});
		btn_exit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		btn_add.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				/* 添加目录 */
				Intent i = new Intent();
				i.setClass(DelConfig.this, Explorer.class);
				finish();
				startActivity(i);
			}
		});
		btn_delAll.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				showDelAlarmDlg(1, getString(R.string.suretodelallfiles));// 您确定要从存储卡中删除列表中的所有文件？
			}
		});
		listview_main.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* 点击此目录，则从sd卡中删除此目录文件,添加对话框进行确认 */
				long size = Opera.getFileSize(new File(Array_paths.get(arg2)));
				if (size == -1) {
					Opera.DisplayToast(DelConfig.this, getString(R.string.cannotdelit), 0);
				} else {
					try {
						showDelAlarmDlg(arg2 + 2, getString(R.string.suretodelonefile) + "\n" + Array_paths.get(arg2) + "\n\n" + getString(R.string.onefilesize) + Opera.sizeLongToString(size));// 您确定要从存储卡中删除此文件？
					} catch (Exception e) {
						/* 若无法获取大小等信息， 则可能是权限不足 */
						Opera.DisplayToast(DelConfig.this, getString(R.string.cannotdelit), 0);
					}
				}
			}

		});
		listview_main.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* 长按此目录，则删除对此目录的记录 */
				DataBaseAdapter db_adapter = new DataBaseAdapter(DelConfig.this);
				db_adapter.open();
				db_adapter.deleteOnePath(Array_paths.get(arg2));
				db_adapter.close();
				Opera.DisplayToast(DelConfig.this, getString(R.string.removepathsuccess), 0);
				if (showList() && m_menu != null) {
					onCreateOptionsMenu(m_menu);// 刷新菜单
				}
				return true;
			}
		});

	}
	/**
	 * 显示列表
	 */
	private boolean showList() {
		/* 查询数据库，并显示到listview */
		DataBaseAdapter db_adapter = new DataBaseAdapter(DelConfig.this);
		db_adapter.open();
		Array_paths = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_PATH);
		Array_fileorpath = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_FILE_PATH);
		db_adapter.close();
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		int pathsCount = Array_paths.size();
		File file = null;
		for (int i = 0; i < pathsCount; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (Array_fileorpath.get(i).equals("1")) {
				File a = new File(Array_paths.get(i));
				File[] childFiles = a.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					map.put("ItemIcon", R.drawable.folder_empty);
				} else {
					map.put("ItemIcon", R.drawable.folder_full);
				}

			} else {
				map.put("ItemIcon", R.drawable.file);
			}

			file = new File(Array_paths.get(i));
			// Log.e("exists???? " + Array_paths.get(i), files.exists() + "");
			if (!file.exists()) {
				map.put("ItemTitle", Array_paths.get(i) + getString(R.string.deletedtag));
			} else {
				map.put("ItemTitle", Array_paths.get(i));
			}
			listItem.add(map);
		}
		/* 生成适配器的Item和动态数组对应的元素 */
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem, R.layout.file_row, new String[] { "ItemIcon", "ItemTitle" }, new int[] { R.id.file_icon, R.id.file_text });
		if (listItemAdapter != null) {
			listview_main.setAdapter(listItemAdapter);
		}
		if (Array_paths.size() == 0) {
			btn_delAll.setEnabled(false);
			text_hint.setText(getString(R.string.pleaseaddfile));
		} else {
			btn_delAll.setEnabled(true);
			text_hint.setText(R.string.hint_delconfig);
		}
		/* 开启线程更新Widget */
		thread_updateWidget = new updateThread();
		thread_updateWidget.start();
		return true;
	}

	/**
	 * 显示警告对话框
	 * 
	 * @param a
	 *            警告类型
	 * @param content
	 *            警告内容
	 */
	private void showDelAlarmDlg(final int a, String content) {
		AlertDialog.Builder testDialog = new AlertDialog.Builder(DelConfig.this);
		testDialog.setTitle(getString(R.string.alarm));
		testDialog.setMessage(content);
		testDialog.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (a == 0) {
					/* 清空目录 */
					DataBaseAdapter db_adapter = new DataBaseAdapter(DelConfig.this);
					db_adapter.open();
					db_adapter.clearTable();
					db_adapter.close();
					Opera.DisplayToast(DelConfig.this, getString(R.string.allpathsremoved), 0);
				} else if (a == 1) {
					/* 删除所有文件 */
					Opera.delAllFiles(DelConfig.this, Array_paths);
				} else {
					/* 删除一个文件 */
					Opera.delOneFile(DelConfig.this, a, Array_paths);
				}
				if (showList() && m_menu != null) {
					onCreateOptionsMenu(m_menu);// 刷新菜单
				}
			}
		}).create();
		testDialog.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create();

		testDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		m_menu = menu;
		menu.clear();
		if (Opera.BUFILE.exists()) {
			if (Array_paths.size() == 0) {
				getMenuInflater().inflate(R.menu.menu_backup_creatandimport, menu);
			} else {
				getMenuInflater().inflate(R.menu.menu_backup_creatandimport_clear, menu);
			}
		} else if (Array_paths.size() != 0) {
			getMenuInflater().inflate(R.menu.menu_backup_creat, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_addfiles, menu);
		}
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_menu_goback: {
			/* 直接恢复数据 */
			if (Opera.goBackData(DelConfig.this)) {
				if (showList()) {
					onCreateOptionsMenu(m_menu);// 刷新菜单
				}
			}
			break;
		}
		case R.id.item_menu_export: {
			/* 导出数据 */
			if (Array_paths.size() == 0) {
				Opera.DisplayToast(DelConfig.this, getString(R.string.noneedbackup), 0);
			} else {
				Opera.exportData(DelConfig.this);
				if (showList()) {
					onCreateOptionsMenu(m_menu);// 刷新菜单
				}
			}
			break;
		}
		case R.id.item_menu_import: {
			/* 导入数据 */
			if (Opera.importData(DelConfig.this, Array_paths, Array_fileorpath)) {
				if (showList()) {
					onCreateOptionsMenu(m_menu);// 刷新菜单
				}
			}
			break;
		}
		case R.id.item_menu_clear: {
			/* 清空目录 */
			showDelAlarmDlg(0, getString(R.string.suretoclear));// 您确定要清空要删除的文件列表？
			break;
		}
		case R.id.item_menu_add: {
			/* 添加目录 */
			Intent i = new Intent();
			i.setClass(DelConfig.this, Explorer.class);
			finish();
			startActivity(i);
			break;
		}
		case R.id.item_menu_delbackup: {
			/* 删除备份文件 */
			try {
				Opera.BUFILE.delete();
				Opera.BUPATH.delete();
			} catch (Exception e) {
				Log.e("DelConfig", "Delete backup file error!");
			}
			if (!Opera.BUFILE.exists()) {
				Opera.DisplayToast(DelConfig.this, getString(R.string.backfiledeled), 0);
			} else {
				Opera.DisplayToast(DelConfig.this, getString(R.string.backfiledelfail), 0);
			}
			onCreateOptionsMenu(m_menu);// 刷新菜单
			break;
		}
		case R.id.item_menu_help: {
			/* 帮助 */
			startActivity(new Intent(DelConfig.this, DelHelp.class));
			break;
		}
		default:
			break;
		}
		return false;
	}

	/**
	 * 更新widget
	 * 
	 * @author ZhuangYu
	 * 
	 */
	class updateThread extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				Opera.updateAllWidgets(DelConfig.this);// 更新
			} catch (Exception e) {
				Log.e("DelConfig therad", "Widget update ERROR");
			} finally {
			}
		}
	}

}