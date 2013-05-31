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
	 * ���Widgetʱ����
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
	 * ��AppWidgetProvider�ṩ�ĵ�һ������������ʱ����
	 */
	public void onEnabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.craining.blog.touchdel", ".MyBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> Oncreated");
		/*���widget���ڵı��*/
		try {
			Opera.WIDGET_EXIST.mkdir();
			Log.e("DelWidgetProvider", "Widget tag file is created!");
		} catch (Exception e) {
			Log.e("DelWidgetProvider", "Widget tag file is not created!");
		}
	}
	
	/**
	 * ��ɾ����һ��Widgetʱ�͵��ô˷���
	 */
	public void onDeleted(Context context, int[] appWidgetIds) {
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> DeleteOne");
	}

	// ��AppWidgetProvider�ṩ�����һ��������ɾ��ʱ����
	public void onDisabled(Context context) {
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName("com.craining.blog.touchdel", ".MyBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		Log.e("DelWidgetProvider", "++++++++++++++++++++++++++++++++++++++++============> OnDelAll");
		/*ɾ��widget���ڵı��*/
		try {
			Opera.WIDGET_EXIST.delete();
			Log.e("DelWidgetProvider", "Widget tag file is deleted!");
		} catch (Exception e) {
			Log.e("DelWidgetProvider", "Widget tag file is not deleted!");
		}
		
	}

	/**
	 * ����һ��Widget
	 * @param context
	 * @param appWidgetManager 
	 * @param appWidgetId
	 */
	public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.e("DelWidgetProvider", "UpdateAppWidget Methord is Running");
		/* ����RemoteViews�����������沿�����и��� */
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
		linkButtons(context, views, true);// ���Widget�еİ�ť����¼�
		appWidgetManager.updateAppWidget(appWidgetId, views);

		/*����Widgetʱ�� �жϸ��·����Ƿ�������δ����������....*/
		Opera.tryStartService(context);
	}
	
	private static void linkButtons(Context context, RemoteViews views, boolean playerActive) {
		// �����ť���¼���Ӧ
		Intent intent0 = new Intent(context, TouchDelWidgetClicked.class);
		PendingIntent pendingIntent0 = PendingIntent.getActivity(context, 0, intent0, 0);
		views.setOnClickPendingIntent(R.id.img_widget_del, pendingIntent0);// ��������
	}
	
}
