package com.drewhannay.xkcdtime;

import android.app.AlarmManager;
import android.content.Context;
import android.widget.ArrayAdapter;

public class XKCDSpinnerAdapter extends ArrayAdapter<String>
{
	public static final long NEVER = -1;

	public XKCDSpinnerAdapter(Context context)
	{
		super(context, android.R.layout.simple_spinner_item, OPTIONS);
	}

	/**
	 * Returns -1 if interval is invalid 
	 */
	public int getPositionForInterval(long interval)
	{
		for (int i = 0; i < VALUES.length; i++)
		{
			if (VALUES[i] == interval)
				return i;
		}

		return -1;
	}

	/**
	 * Returns XKCDSpinnerAdapter.NEVER if selected interval is "Never" 
	 */
	public long getIntervalForPosition(int position)
	{
		return VALUES[position];
	}

	private static final String[] OPTIONS = new String[] { "Never", "Half Hour", "Hour", "Half Day", "Day" };
	private static final long[] VALUES = new long[] { NEVER, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HALF_DAY, AlarmManager.INTERVAL_DAY };
}
