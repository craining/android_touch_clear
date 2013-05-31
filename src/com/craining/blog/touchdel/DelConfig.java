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

		Opera.tryStartService(this);// ��̨����Widget����

		btn_help.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				/* ���� */
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
				/* ���Ŀ¼ */
				Intent i = new Intent();
				i.setClass(DelConfig.this, Explorer.class);
				finish();
				startActivity(i);
			}
		});
		btn_delAll.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				showDelAlarmDlg(1, getString(R.string.suretodelallfiles));// ��ȷ��Ҫ�Ӵ洢����ɾ���б��е������ļ���
			}
		});
		listview_main.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* �����Ŀ¼�����sd����ɾ����Ŀ¼�ļ�,��ӶԻ������ȷ�� */
				long size = Opera.getFileSize(new File(Array_paths.get(arg2)));
				if (size == -1) {
					Opera.DisplayToast(DelConfig.this, getString(R.string.cannotdelit), 0);
				} else {
					try {
						showDelAlarmDlg(arg2 + 2, getString(R.string.suretodelonefile) + "\n" + Array_paths.get(arg2) + "\n\n" + getString(R.string.onefilesize) + Opera.sizeLongToString(size));// ��ȷ��Ҫ�Ӵ洢����ɾ�����ļ���
					} catch (Exception e) {
						/* ���޷���ȡ��С����Ϣ�� �������Ȩ�޲��� */
						Opera.DisplayToast(DelConfig.this, getString(R.string.cannotdelit), 0);
					}
				}
			}

		});
		listview_main.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* ������Ŀ¼����ɾ���Դ�Ŀ¼�ļ�¼ */
				DataBaseAdapter db_adapter = new DataBaseAdapter(DelConfig.this);
				db_adapter.open();
				db_adapter.deleteOnePath(Array_paths.get(arg2));
				db_adapter.close();
				Opera.DisplayToast(DelConfig.this, getString(R.string.removepathsuccess), 0);
				if (showList() && m_menu != null) {
					onCreateOptionsMenu(m_menu);// ˢ�²˵�
				}
				return true;
			}
		});

	}
	/**
	 * ��ʾ�б�
	 */
	private boolean showList() {
		/* ��ѯ���ݿ⣬����ʾ��listview */
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
		/* ������������Item�Ͷ�̬�����Ӧ��Ԫ�� */
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
		/* �����̸߳���Widget */
		thread_updateWidget = new updateThread();
		thread_updateWidget.start();
		return true;
	}

	/**
	 * ��ʾ����Ի���
	 * 
	 * @param a
	 *            ��������
	 * @param content
	 *            ��������
	 */
	private void showDelAlarmDlg(final int a, String content) {
		AlertDialog.Builder testDialog = new AlertDialog.Builder(DelConfig.this);
		testDialog.setTitle(getString(R.string.alarm));
		testDialog.setMessage(content);
		testDialog.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (a == 0) {
					/* ���Ŀ¼ */
					DataBaseAdapter db_adapter = new DataBaseAdapter(DelConfig.this);
					db_adapter.open();
					db_adapter.clearTable();
					db_adapter.close();
					Opera.DisplayToast(DelConfig.this, getString(R.string.allpathsremoved), 0);
				} else if (a == 1) {
					/* ɾ�������ļ� */
					Opera.delAllFiles(DelConfig.this, Array_paths);
				} else {
					/* ɾ��һ���ļ� */
					Opera.delOneFile(DelConfig.this, a, Array_paths);
				}
				if (showList() && m_menu != null) {
					onCreateOptionsMenu(m_menu);// ˢ�²˵�
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
			/* ֱ�ӻָ����� */
			if (Opera.goBackData(DelConfig.this)) {
				if (showList()) {
					onCreateOptionsMenu(m_menu);// ˢ�²˵�
				}
			}
			break;
		}
		case R.id.item_menu_export: {
			/* �������� */
			if (Array_paths.size() == 0) {
				Opera.DisplayToast(DelConfig.this, getString(R.string.noneedbackup), 0);
			} else {
				Opera.exportData(DelConfig.this);
				if (showList()) {
					onCreateOptionsMenu(m_menu);// ˢ�²˵�
				}
			}
			break;
		}
		case R.id.item_menu_import: {
			/* �������� */
			if (Opera.importData(DelConfig.this, Array_paths, Array_fileorpath)) {
				if (showList()) {
					onCreateOptionsMenu(m_menu);// ˢ�²˵�
				}
			}
			break;
		}
		case R.id.item_menu_clear: {
			/* ���Ŀ¼ */
			showDelAlarmDlg(0, getString(R.string.suretoclear));// ��ȷ��Ҫ���Ҫɾ�����ļ��б�
			break;
		}
		case R.id.item_menu_add: {
			/* ���Ŀ¼ */
			Intent i = new Intent();
			i.setClass(DelConfig.this, Explorer.class);
			finish();
			startActivity(i);
			break;
		}
		case R.id.item_menu_delbackup: {
			/* ɾ�������ļ� */
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
			onCreateOptionsMenu(m_menu);// ˢ�²˵�
			break;
		}
		case R.id.item_menu_help: {
			/* ���� */
			startActivity(new Intent(DelConfig.this, DelHelp.class));
			break;
		}
		default:
			break;
		}
		return false;
	}

	/**
	 * ����widget
	 * 
	 * @author ZhuangYu
	 * 
	 */
	class updateThread extends Thread {

		@Override
		public void run() {
			super.run();
			try {
				Opera.updateAllWidgets(DelConfig.this);// ����
			} catch (Exception e) {
				Log.e("DelConfig therad", "Widget update ERROR");
			} finally {
			}
		}
	}

}