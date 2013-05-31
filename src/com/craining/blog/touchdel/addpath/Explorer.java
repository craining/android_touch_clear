package com.craining.blog.touchdel.addpath;

/**
 * �ļ����
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.craining.blog.touchdel.DataBaseAdapter;
import com.craining.blog.touchdel.DelConfig;
import com.craining.blog.touchdel.Opera;
import com.craining.blog.touchdel.R;

public class Explorer extends Activity {
	private ArrayList<String> allSelectedPathsandfiles = null;
	private ArrayList<String> allselectedPaths = null;
	private ArrayList<String> allselectedfiles = null;
	private ArrayList<String> newAddedFiles = new ArrayList<String>();
	private String rootPath = "/sdcard";// ����Ŀ¼����Ϊsd��
	private String parentPath = "/sdcard";
	private String nowPath = "/sdcard";

	private Button btn_return = null;// ���ص���Ŀ¼
	private Button btn_back = null;// ������һ��Ŀ¼
	private Button btn_finish = null;// ��ɣ���������ҳ��
	private ListView listview_file = null;
	private TextView text_showNowPath = null;
	private TextView text_hintTop = null;

	private Thread thread_updateWidget = null;
	private Menu my_menu = null;

	@Override
	protected void onCreate(Bundle save) {
		super.onCreate(save);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.file_explorer);
		allSelectedPathsandfiles = DelConfig.Array_paths;

		btn_return = (Button) findViewById(R.id.btn_return);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_finish = (Button) findViewById(R.id.btn_finish);
		text_showNowPath = (TextView) findViewById(R.id.text_nowpath);
		text_hintTop = (TextView) findViewById(R.id.textview_top);
		listview_file = (ListView) findViewById(R.id.file_list);

		if (Opera.isSdPresent()) {
			getFileDir(nowPath);
		} else {
			Opera.DisplayToast(Explorer.this, getString(R.string.sdcardunpresent), 0);
			Intent i = new Intent();
			i.setClass(Explorer.this, DelConfig.class);
			finish();
			startActivity(i);
		}

		listview_file.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* ��������Ŀ���ж��ļ�����Ŀ¼������ */
				if (arg2 < allselectedPaths.size()) {
					// ��Ŀ¼
					String prePath = "";
					Log.e("getFileDir", nowPath + "/" + allselectedPaths.get(arg2));
					try {
						prePath = nowPath;
						getFileDir(nowPath + "/" + allselectedPaths.get(arg2));
					} catch (Exception e) {
						nowPath = prePath;
						getFileDir(nowPath);
						Opera.DisplayToast(Explorer.this, getString(R.string.cannouintothis), 0);
					}

				} else {
					// ��
					Opera.DisplayToast(Explorer.this, getString(R.string.cannotinto), 0);
				}
			}
		});

		listview_file.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* ��������Ŀ������� */
				// Log.e("", "LongClicked" + arg2 + "   ");
				String fileorpath = arg2 < allselectedPaths.size() ? "1" : "0";
				String name = nowPath + "/" + (arg2 < allselectedPaths.size() ? allselectedPaths.get(arg2) : allselectedfiles.get(arg2 - allselectedPaths.size()));
				DataBaseAdapter db_adapter = new DataBaseAdapter(Explorer.this);
				db_adapter.open();
				db_adapter.insertData(name, fileorpath);
				db_adapter.close();
				Opera.DisplayToast(Explorer.this, getString(R.string.addpathsuccess), 0);
				allSelectedPathsandfiles.add(name);
				newAddedFiles.add(name);
				/* ����˵����������������ʾ�˵� */
				if (my_menu != null) {
					onCreateOptionsMenu(my_menu);
				}
				Opera.tryStartService(Explorer.this);// ��̨����Widget����
				thread_updateWidget = new updateThread();
				thread_updateWidget.start();
				getFileDir(nowPath);
				return true;
			}
		});
		btn_return.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				getFileDir(rootPath);
			}
		});
		btn_back.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				getFileDir(parentPath);
			}
		});
		btn_finish.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(Explorer.this, DelConfig.class);
				finish();
				startActivity(i);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		my_menu = menu;
		if (menu != null) {
			menu.clear();
		}
		if (newAddedFiles.size() <= 0) {
			getMenuInflater().inflate(R.menu.menu_null, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_cancle, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_cancle: {
			if (newAddedFiles.size() > 0) {
				/* �����Դ�Ŀ¼����� */
				DataBaseAdapter db_adapter = new DataBaseAdapter(Explorer.this);
				db_adapter.open();
				db_adapter.deleteOnePath(newAddedFiles.get(newAddedFiles.size() - 1));
				db_adapter.close();

				newAddedFiles.remove(newAddedFiles.size() - 1);
				allSelectedPathsandfiles.remove(allSelectedPathsandfiles.size() - 1);
				thread_updateWidget = new updateThread();
				thread_updateWidget.start();
				getFileDir(nowPath);
			}
			onCreateOptionsMenu(my_menu);
		}
		default:
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * ����Ŀ¼
	 * 
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		setProgressBarIndeterminate(true);
		/* ����Ŀǰ����·�� */
		nowPath = filePath;
		text_showNowPath.setText(nowPath);
		File f = new File(filePath);
		File[] files = f.listFiles();

		if (!filePath.equals(rootPath)) {
			btn_back.setEnabled(true);
			btn_return.setEnabled(true);
			parentPath = f.getParent();
		} else {
			btn_return.setEnabled(false);
			btn_back.setEnabled(false);
		}
		if (files == null || files.length == 0) {
			text_hintTop.setText(getString(R.string.hint_emptypath));
			SimpleAdapter listItemAdapter = new SimpleAdapter(this, new ArrayList<HashMap<String, Object>>(), R.layout.file_row, new String[] { "FileTitle", "FileIcon" }, new int[] { R.id.file_text,
					R.id.file_icon });
			listview_file.setAdapter(listItemAdapter);
		} else {
			text_hintTop.setText(getString(R.string.hint_addpath));
			allselectedfiles = new ArrayList<String>();
			allselectedPaths = new ArrayList<String>();
			ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

			/* �������ļ����ArrayList�� */
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (alreadyExcite(file.getPath())) {
					/* ���Ŀ¼�Ѿ���Ҫɾ�����б��У��򲻱���ʾ�ˡ� */
				} else {
					if (file.isDirectory()) {
						allselectedPaths.add(file.getName());
					} else {
						allselectedfiles.add(file.getName());
					}
				}
			}

			/* ���� + ��ʾ */
			String[] aa = new String[allselectedPaths.size()];
			allselectedPaths.toArray(aa);
			Arrays.sort(aa);
			allselectedPaths = new ArrayList<String>();
			for (String ee : aa) {
				allselectedPaths.add(ee);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("FileTitle", ee);
				/* �ж��Ƿ�Ϊ��Ŀ¼�� �������Ӧͼ�� */
				File a = new File(nowPath + "/" + ee);
				File[] childfiles = a.listFiles();
				if (childfiles == null || childfiles.length == 0) {
					map.put("FileIcon", R.drawable.folder_empty);
				} else {
					map.put("FileIcon", R.drawable.folder_full);
				}

				listItem.add(map);
			}
			String[] mm = new String[allselectedfiles.size()];
			allselectedfiles.toArray(mm);
			Arrays.sort(mm);
			allselectedfiles = new ArrayList<String>();
			for (String ff : mm) {
				allselectedfiles.add(ff);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("FileTitle", ff);
				map.put("FileIcon", R.drawable.file);
				listItem.add(map);
			}
			/* ������������Item�Ͷ�̬�����Ӧ��Ԫ�� */
			SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem, R.layout.file_row, new String[] { "FileTitle", "FileIcon" }, new int[] { R.id.file_text, R.id.file_icon });
			listview_file.setAdapter(listItemAdapter);
		}
		setProgressBarIndeterminate(false);
	}

	private boolean alreadyExcite(String testPath) {
		int count = allSelectedPathsandfiles.size();
		for (int j = 0; j < count; j++) {
			if (testPath.equals(allSelectedPathsandfiles.get(j))) {
				return true;
			}
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
				Opera.updateAllWidgets(Explorer.this);// ����
			} catch (Exception e) {
				Log.e("Explorer therad", "Widget update ERROR");
			} finally {
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (nowPath.equals(rootPath)) {
				Intent i = new Intent();
				i.setClass(Explorer.this, DelConfig.class);
				finish();
				startActivity(i);
			} else {
				getFileDir(parentPath);
			}
			return false;
		}
		return false;
	}
}
