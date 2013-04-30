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
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
		mNotifySpinner = (Spinner) findViewById(R.id.notify_spinner);

		mSpinnerAdapter = new XKCDSpinnerAdapter(this);
		mNotifySpinner.setAdapter(mSpinnerAdapter);
		mNotifySpinner.setOnItemSelectedListener(mSpinnerItemSelectedListener);
		mNotifySpinner.setSelection(mSpinnerAdapter.getPositionForInterval(
				PreferenceManager.getDefaultSharedPreferences(this).getLong(NOTIFY_INTERVAL_KEY, XKCDSpinnerAdapter.NEVER)));

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
		long interval = PreferenceManager.getDefaultSharedPreferences(context).getLong(NOTIFY_INTERVAL_KEY, XKCDSpinnerAdapter.NEVER);
		if (interval == XKCDSpinnerAdapter.NEVER)
		{
			cancelAlarm(context);
			return;
		}

		Intent intent = new Intent(context, RepeatingAlarm.class);
		PendingIntent notificationIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		long nextAlarmTime = getNextAlarmTime(Calendar.getInstance());

		AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmTime, interval, notificationIntent);
	}

	private static void cancelAlarm(Context context)
	{
		// Create the same intent, and thus a matching IntentSender, for the one that was scheduled...
		Intent intent = new Intent(context, RepeatingAlarm.class);
		PendingIntent notificationIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		// ...and cancel the alarm
		AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		am.cancel(notificationIntent);
	}

	private static long getNextAlarmTime(Calendar now)
	{
		int minutes = now.get(Calendar.MINUTE);
		if (minutes > 0)
		{
			now.set(Calendar.MINUTE, 0);
			now.add(Calendar.HOUR, 1);
		}

		return now.getTimeInMillis();
	}

	private final OnItemSelectedListener mSpinnerItemSelectedListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			long interval = mSpinnerAdapter.getIntervalForPosition(position);

			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			preferences.edit().putLong(NOTIFY_INTERVAL_KEY, interval).commit();

			cancelAlarm(MainActivity.this);

			if (interval != XKCDSpinnerAdapter.NEVER)
				scheduleAlarm(MainActivity.this);			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{
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
	private static final String NOTIFY_INTERVAL_KEY = "notifyInterval";

	private XKCDSpinnerAdapter mSpinnerAdapter;
	private DownloadTimeTask mDownloadTimeTask;
	private ProgressBar mProgressSpinner;
	private ImageView mTimeView;
	private Spinner mNotifySpinner;
	private Bitmap mBitmap;
}
