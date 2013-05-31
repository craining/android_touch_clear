package com.craining.blog.touchdel.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.craining.blog.touchdel.DataBaseAdapter;
import com.craining.blog.touchdel.Opera;

public class UpdateWidgetService extends Service {

	private Thread thread_update = null;
	DataBaseAdapter db = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("UpdateWidgetService", "Service on created!");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.e("UpdateWidgetService", "Service on started!");
		/* ����Widgetʱ�� �жϸ��·����Ƿ�������δ����������.... */
		db = new DataBaseAdapter(UpdateWidgetService.this);
		db.open();
		if (Opera.WIDGET_EXIST.exists()&& !db.isEmpty()) {
			Log.e("service", "start  update !");
			thread_update = new updateThread();
			thread_update.start();
		} else {
			Log.e("service", "stop self service!");
			stopSelf();
		}
		db.close();
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("UpdateWidgetService", "Service on destroyed!");
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
				do {
					sleep(10000);
					Opera.updateAllWidgets(UpdateWidgetService.this);// ����
				} while ( Opera.WIDGET_EXIST.exists() );
			} catch (Exception e) {
				Log.e("UpdateWidgetService", "Thread Error!");
				e.printStackTrace();
			}
		}
	}

}
