package com.craining.blog.touchdel.addpath;

/**
 * 文件浏览
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
	private String rootPath = "/sdcard";// 将根目录设置为sd卡
	private String parentPath = "/sdcard";
	private String nowPath = "/sdcard";

	private Button btn_return = null;// 返回到根目录
	private Button btn_back = null;// 返回上一级目录
	private Button btn_finish = null;// 完成，并返回主页面
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
				/* 单击此条目，判断文件还是目录并进入 */
				if (arg2 < allselectedPaths.size()) {
					// 是目录
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
					// 否
					Opera.DisplayToast(Explorer.this, getString(R.string.cannotinto), 0);
				}
			}
		});

		listview_file.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				/* 长按此条目进行添加 */
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
				/* 如果菜单弹起过，则重新显示菜单 */
				if (my_menu != null) {
					onCreateOptionsMenu(my_menu);
				}
				Opera.tryStartService(Explorer.this);// 后台更新Widget服务
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
				/* 撤销对此目录的添加 */
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
	 * 进入目录
	 * 
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		setProgressBarIndeterminate(true);
		/* 设置目前所在路径 */
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

			/* 将所有文件添加ArrayList中 */
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (alreadyExcite(file.getPath())) {
					/* 如果目录已经在要删除的列表中，则不必显示了。 */
				} else {
					if (file.isDirectory()) {
						allselectedPaths.add(file.getName());
					} else {
						allselectedfiles.add(file.getName());
					}
				}
			}

			/* 排序 + 显示 */
			String[] aa = new String[allselectedPaths.size()];
			allselectedPaths.toArray(aa);
			Arrays.sort(aa);
			allselectedPaths = new ArrayList<String>();
			for (String ee : aa) {
				allselectedPaths.add(ee);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("FileTitle", ee);
				/* 判断是否为空目录， 并添加相应图标 */
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
			/* 生成适配器的Item和动态数组对应的元素 */
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
				Opera.updateAllWidgets(Explorer.this);// 更新
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
