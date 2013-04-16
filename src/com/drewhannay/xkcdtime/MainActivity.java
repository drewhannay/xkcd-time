package com.drewhannay.xkcdtime;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
		mTimeView = (ImageView) findViewById(R.id.time_view);
		mNotifyCheckBox = (CheckBox) findViewById(R.id.notify_checkbox);

		mNotifyCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
		mNotifyCheckBox.setChecked(getPreferences(MODE_PRIVATE).getBoolean(NOTIFY_KEY, false));

		TextView xkcdSite = (TextView) findViewById(R.id.xkcd_site_textview);
		xkcdSite.setText(Html.fromHtml(getString(R.string.xkcd_site)));
		Linkify.addLinks(xkcdSite, Linkify.ALL);
		xkcdSite.setMovementMethod(LinkMovementMethod.getInstance());

		TextView forums = (TextView) findViewById(R.id.forum_textview);
		forums.setText(Html.fromHtml(getString(R.string.xkcd_forum_thread)));
		Linkify.addLinks(forums, Linkify.ALL);
		forums.setMovementMethod(LinkMovementMethod.getInstance());

		TextView aubronwood = (TextView) findViewById(R.id.aubronwood_textview);
		aubronwood.setText(Html.fromHtml(getString(R.string.aubron_wood_interactive_gif)));
		Linkify.addLinks(aubronwood, Linkify.ALL);
		aubronwood.setMovementMethod(LinkMovementMethod.getInstance());

		if (savedInstanceState != null)
		{
			mBitmap = savedInstanceState.getParcelable(BITMAP_KEY);
			mTimeView.setImageBitmap(mBitmap);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (mBitmap == null)
		{
			mDownloadTimeTask = new DownloadTimeTask();
			mDownloadTimeTask.execute(TIME_URL);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (mDownloadTimeTask != null)
		{
			mDownloadTimeTask.cancel(true);
			mDownloadTimeTask = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable(BITMAP_KEY, mBitmap);

		super.onSaveInstanceState(outState);
	}

	public static void scheduleAlarm(Context context)
	{
		Intent intent = new Intent(context, RepeatingAlarm.class);
		PendingIntent notificationIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		long nextAlarmTime = getNextAlarmTime(Calendar.getInstance());

		AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmTime, AlarmManager.INTERVAL_HALF_HOUR, notificationIntent);
	}

	private void cancelAlarm()
	{
		// Create the same intent, and thus a matching IntentSender, for the one that was scheduled...
		Intent intent = new Intent(this, RepeatingAlarm.class);
		PendingIntent notificationIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

		// ...and cancel the alarm
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(notificationIntent);
	}

	private static long getNextAlarmTime(Calendar now)
	{
		int minutes = now.get(Calendar.MINUTE);
		if (minutes >= 30)
		{
			now.set(Calendar.MINUTE, 0);
			now.add(Calendar.HOUR, 1);
		}
		else
		{
			now.set(Calendar.MINUTE, 30);
		}

		return now.getTimeInMillis();
	}

	private final OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			preferences.edit().putBoolean(NOTIFY_KEY, isChecked).commit();

			cancelAlarm();

			if (isChecked)
				scheduleAlarm(MainActivity.this);
		}
	};

	private final class DownloadTimeTask extends AsyncTask<String, Void, Bitmap>
	{
		@Override
		protected void onPreExecute()
		{
			mTimeView.setVisibility(View.GONE);
			mProgressSpinner.setVisibility(View.VISIBLE);
			mProgressSpinner.setIndeterminate(true);
		}

		@Override
		protected Bitmap doInBackground(String... params)
		{
			return BitmapUtility.getBitmapFromURL(params[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			mDownloadTimeTask = null;

			if (isCancelled())
				return;

			mBitmap = result;
			mTimeView.setImageBitmap(result);
			mTimeView.setVisibility(View.VISIBLE);
			mProgressSpinner.setVisibility(View.GONE);
		};
	}

	private static final String TIME_URL = "http://imgs.xkcd.com/comics/time.png";
	private static final String BITMAP_KEY = "bitmap";
	private static final String NOTIFY_KEY = "notify";

	private DownloadTimeTask mDownloadTimeTask;
	private ProgressBar mProgressSpinner;
	private ImageView mTimeView;
	private CheckBox mNotifyCheckBox;
	private Bitmap mBitmap;
}
