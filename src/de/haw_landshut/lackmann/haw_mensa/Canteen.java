package de.haw_landshut.lackmann.haw_mensa;

import java.util.Calendar;
import java.util.HashMap;

import android.util.Log;

import com.google.gson.annotations.SerializedName;


public class Canteen implements SpinnerItem {
	
	private static final int DAY_OUTDATED = 1000*60*60*1;
	
	@SerializedName("id")
	public String key;
	
	@SerializedName("name")
	public String name;
	
	@SerializedName("address")
	public String address;
	
	@SerializedName("coordinates")
	Float[] coordinates;

	/**
	 * date -> meals
	 */
	@SerializedName("_days")
	private HashMap<String, Day> days;
	
	/**
	 * Save when we last fetched for each day
	 */
	@SerializedName("_updates")
	private HashMap<String, Long> updates;

	public Canteen(String key, String name) {
		this.name = name;
		this.key = key;
	}

	@Override
	public String toString() {
		return name;
	}

	void updateDays(Days newDays) {
		if (days == null)
			days = new HashMap<>();
		for (Day day : newDays) {
			days.put(day.date, day);
			justUpdated(day.date);
		}
	}
	
	Day getDay(String date) {
		if (days == null) {
			return null;
		}
		return days.get(date);
	}
	
	void justUpdated(String date) {
		if (updates == null)
			updates = new HashMap<>();
		
		Calendar now = Calendar.getInstance();
		updates.put(date, now.getTimeInMillis());
	}

	boolean isOutOfDate(String date) {
		if (updates == null)
			return true;
		
		Calendar now = Calendar.getInstance();
		Long lastUpdate = updates.get(date);
		
		if (lastUpdate == null)
			return true;

		return now.getTimeInMillis() - lastUpdate > DAY_OUTDATED;
	}
	
	@Override
	public boolean execute(MainActivity mainActivity, int itemPosition) {
		Canteen c = SettingsUtils.getStorage(mainActivity).getFavouriteCanteens().get(itemPosition);
		Log.d(MainActivity.TAG, String.format("Chose canteen %s", c.key));
		mainActivity.changeCanteenTo(c);
		return true;
	}
}
