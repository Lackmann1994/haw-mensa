package de.haw_landshut.lackmann.haw_mensa;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import de.siegmar.fastcsv.reader.CsvParser;

public class RetrieveCanteenFeedTask extends RetrieveFeedTask {
	
	private Canteens canteens;
	private OnFinishedFetchingCanteensListener fetchListener;
	protected String name = "Canteens";
	private int currentPage = 1;
	private String url;
	
	public RetrieveCanteenFeedTask(Context context, Activity activity, OnFinishedFetchingCanteensListener fetchListener, String url) {
		super(context, activity);
		
		this.url = url;
		this.canteens = new Canteens();
		this.fetchListener = fetchListener;
		this.visible = true;
	}
	
	public Canteens getCanteens() {
		return canteens;
	}
	
	protected void parseFromJSON(String jsonString)  {
		Canteen[] canteens_arr = gson.fromJson(jsonString, Canteen[].class);
		for(Canteen canteen : canteens_arr) {
			canteens.put(canteen.key, canteen);
		}

		if (canteens_arr.length > 0 && (totalPages == null || currentPage < totalPages)) {
			currentPage++;
			doInBackground(url + "&hasCoordinates=true&page=" + currentPage);
		}
	}

	@Override
	protected void parseFromCSV(CsvParser parser, CsvParser parser2) throws IOException {

	}


	protected void onPostExecuteFinished() {
		Log.d(TAG, String.format("Fetched %s canteen items", canteens.size()));
		
		// notify that we are done
		fetchListener.onCanteenFetchFinished(this);
	}
}