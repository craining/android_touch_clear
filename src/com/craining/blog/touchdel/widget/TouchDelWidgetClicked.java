package com.craining.blog.touchdel.widget;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.craining.blog.touchdel.DataBaseAdapter;
import com.craining.blog.touchdel.Opera;
import com.craining.blog.touchdel.R;

public class TouchDelWidgetClicked extends Activity {

	private TextView text_showPaths = null;
	private Button btn_sure = null;
	private CheckBox check_remember = null;
	private Button btn_cancle = null;
	private ArrayList<String> toshowPaths = new ArrayList<String>();

	private Thread thread_updateWidget = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dlg_widgetclicked);
		setTitle(getString(R.string.title_widgetdlg));

		text_showPaths = (TextView) findViewById(R.id.text_showpaths);
		btn_sure = (Button) findViewById(R.id.btn_dlg_sure);
		btn_cancle = (Button) findViewById(R.id.btn_dlg_cancle);
		check_remember = (CheckBox) findViewById(R.id.check_remember);

		updateTextView();

		check_remember.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// if(Opera.FILE_REMEMBER.exists()) {
				// Opera.FILE_REMEMBER.delete();
				// } else {
				// Opera.FILE_REMEMBER.mkdir();
				// }
			}
		});
		btn_sure.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (check_remember.isChecked()) {
					Opera.FILE_REMEMBER.mkdir();
				}
				doDelete();
				finish();
			}
		});
		btn_cancle.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void updateTextView() {

		DataBaseAdapter db_adapter = new DataBaseAdapter(TouchDelWidgetClicked.this);
		db_adapter.open();
		toshowPaths = DataBaseAdapter.getColumnThingsInf(DataBaseAdapter.KEY_PATH);
		db_adapter.close();
		if (toshowPaths == null || toshowPaths.size() == 0) {
			// δ���Ŀ¼
			Opera.DisplayToast(TouchDelWidgetClicked.this, getString(R.string.paths_empty), 0);
			finish();
		} else if (Opera.filesAllSize(TouchDelWidgetClicked.this) == 0) {
			// �ļ�������
			Opera.DisplayToast(TouchDelWidgetClicked.this, getString(R.string.pahts_alldeled), 0);
			finish();
		} else {

			if (Opera.FILE_REMEMBER.exists()) {
				// ֱ�ӽ���ɾ������
				doDelete();

			} else {
				check_remember.setChecked(false);
				String toshow = "";
				String[] allPaths = new String[toshowPaths.size()];
				toshowPaths.toArray(allPaths);
				for (String a : allPaths) {
					toshow = toshow + "\n" + a;
				}
				text_showPaths.setText(toshow);
			}
		}

	}

	private void doDelete() {
		Opera.delAllFiles(TouchDelWidgetClicked.this, toshowPaths);// ɾ��
		/* �����̸߳���Widget */
		thread_updateWidget = new updateThread();
		thread_updateWidget.start();
		finish();
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
				Opera.updateAllWidgets(TouchDelWidgetClicked.this);// ����
			} catch (Exception e) {
				Log.e("TouchDelWidgetClicked therad", "Widget update ERROR");
			} finally {
			}
		}
	}

}
