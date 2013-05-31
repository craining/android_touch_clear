package com.craining.blog.touchdel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.craining.blog.touchdel.widget.UpdateWidgetService;

public class TouchReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context, UpdateWidgetService.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(i);
		}
	}
	
}
