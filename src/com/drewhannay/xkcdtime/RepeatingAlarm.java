package com.drewhannay.xkcdtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class RepeatingAlarm extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent activityIntent = new Intent(context, MainActivity.class);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(context.getString(R.string.notif_title));
		builder.setContentText(context.getString(R.string.notif_content));
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentIntent(PendingIntent.getActivity(context, 0, activityIntent, 0));
		builder.setAutoCancel(true);
		builder.setDefaults(Notification.DEFAULT_ALL);

		notificationManager.notify(1, builder.build());
	}
}
