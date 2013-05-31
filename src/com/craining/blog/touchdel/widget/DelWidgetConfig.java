package com.craining.blog.touchdel.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * ����һ��Widget
 *
 */
public class DelWidgetConfig extends Activity {
	public static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	public DelWidgetConfig() {
		super();
	}
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setResult(RESULT_CANCELED);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			DelWidgetConfig.this.finish();
		}
		toShowWidget();// ��ʾWidget
	}

	private void toShowWidget() {
		// ȡ��AppWidgetManagerʵ��
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(DelWidgetConfig.this);
		// ����AppWidget
		DelWidgetProvider.updateAppWidget(DelWidgetConfig.this, appWidgetManager, mAppWidgetId);
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		Log.e("1111111111111", "" + resultValue);
		finish();
	}

}
