package de.haw_landshut.lackmann.haw_mensa;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import de.siegmar.fastcsv.reader.CsvParser;


public class RetrieveDaysFeedJsonTask extends RetrieveFeedTask {

	private Days days;
	private OnFinishedFetchingDaysJsonListener fetchListener;
	protected String name = "Days";
	protected Canteen canteen;
	public String dateString = "";

	public RetrieveDaysFeedJsonTask(Context context, Activity activity, OnFinishedFetchingDaysJsonListener fetchListener, Canteen canteen, String dateString) {
		super(context, activity);
		this.dateString = dateString;
		this.canteen = canteen;
		this.fetchListener = fetchListener;
	}
	
	Days getDays() {
		return days;
	}
	
	Canteen getCanteen() {
		return canteen;
	}
	
	protected void parseFromJSON(String jsonString) {
		days = gson.fromJson(jsonString, Days.class);
		for (Day day : days) {
			if (day.meals == null || day.date == null) {
				Log.w(MainActivity.TAG, "Incomplete json response from server. Meals or date is null");
				this.exception = new Exception("Incomplete response from server.");
			}
		}
	}

	@Override
	protected void parseFromCSV(CsvParser parser, CsvParser parser2) throws IOException {

	}

	protected void onPostExecuteFinished() {
		if (days == null) {
			throw new IllegalStateException("Days cannot be null.");
		}
		Log.d(TAG, String.format("Fetched %s days", days.size()));
		
		// notify that we are done
		fetchListener.onDaysFetchFinished(this);
	}
}