package com.craining.blog.touchdel.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.craining.blog.touchdel.Opera;
import com.craining.blog.touchdel.R;

/**
 * AppWidgetProvider
 * @author Zhuang
 *
 */
public class DelWidgetProvider extends AppWidgetProvider {

	/**
	 * 添加Widget时调用
	 */
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		Log.e("DelWidgetProvider", "appWidgetIds.length++++++++++++++++++++++++++++++++++++++++============>" + N);
		String toSaveId = "";
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			toSaveId = toSaveId + appWidgetId + "_";
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	/**
	 * 当AppWidgetProvider提供的第一个部件被创建时调用
	 */
	public void onEnabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.craining.blog.touchdel", ".MyBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> Oncreated");
		/*添加widget存在的标记*/
		try {
			Opera.WIDGET_EXIST.mkdir();
			Log.e("DelWidgetProvider", "Widget tag file is created!");
		} catch (Exception e) {
			Log.e("DelWidgetProvider", "Widget tag file is not created!");
		}
	}
	
	/**
	 * 当删除了一个Widget时就调用此方法
	 */
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> DeleteOne");
	}

	// 当AppWidgetProvider提供的最后一个部件被删除时调用
	public void onDisabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.craining.blog.touchdel", ".MyBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> OnDelAll");
		/*删除widget存在的标记*/
		try {
			Opera.WIDGET_EXIST.delete();
			Log.e("DelWidgetProvider", "Widget tag file is deleted!");
		} catch (Exception e) {
			Log.e("DelWidgetProvider", "Widget tag file is not deleted!");
		}
		
	}

	/**
	 * 更新一个Widget
	 * @param context
	 * @param appWidgetManager 
	 * @param appWidgetId
	 */
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e("DelWidgetProvider", "UpdateAppWidget Methord is Running");
		/* 构建RemoteViews对象来对桌面部件进行更新 */
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
		long sizeAll =  Opera.filesAllSize(context) ;
		if( sizeAll == 0 ) {
			views.setTextViewText(R.id.text_widget_del, "EMPTY");
			views.setImageViewResource(R.id.img_widget_del, R.anim.widget_empty);
		} else if (sizeAll == -1) {
			views.setTextViewText(R.id.text_widget_del, "ERROR");
			views.setImageViewResource(R.id.img_widget_del, R.drawable.png_error);
		} else {
			views.setTextViewText(R.id.text_widget_del, Opera.sizeLongToString(sizeAll));
			views.setImageViewResource(R.id.img_widget_del, R.anim.widget_full);
		}
		linkButtons(context, views, true);// 添加Widget中的按钮点击事件
		appWidgetManager.updateAppWidget(appWidgetId, views);

		/*存在Widget时， 判断更新服务是否开启，若未开启，则开启....*/
		Opera.tryStartService(context);
	}
	
	private static void linkButtons(Context context, RemoteViews views, boolean playerActive) {
		// 点击按钮的事件响应
		Intent intent0 = new Intent(context, TouchDelWidgetClicked.class);
		PendingIntent pendingIntent0 = PendingIntent.getActivity(context, 0, intent0, 0);
		views.setOnClickPendingIntent(R.id.img_widget_del, pendingIntent0);// 打开主程序
	}
	
}
