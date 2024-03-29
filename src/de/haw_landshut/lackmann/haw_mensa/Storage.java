package de.haw_landshut.lackmann.haw_mensa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;


public class Storage extends Observable {
	
	@SerializedName("canteens")
	Canteens canteens;


	@SerializedName("canteen")
	Canteen canteen = new Canteen("197", "Mensa HS Landshut");


	@SerializedName("currentCanteen")
	public String currentCanteen;


	@SerializedName("lastUpdate")
	public Calendar lastCanteensUpdate;


	@SerializedName("favouriteCanteensKeys")
	public Set<String> favouriteCanteensKeys = new HashSet<String>();
	

	public Boolean areCanteensOutOfDate() {
		if (lastCanteensUpdate == null) {
			Log.d(MainActivity.TAG, "Out of date because no last fetch date is set.");
			return true;
		}
		
		Calendar now = Calendar.getInstance();
		// 14 days
		int maxDiff = 1000*60*60*24*14;
		if (now.getTimeInMillis() - lastCanteensUpdate.getTimeInMillis() > maxDiff) {
			return true;
		}
		return false;
	}
	
	public Canteens getCanteens(Context context) {
		if (canteens == null) {
			loadFromPreferences(context);
		}
		return getCanteens();
	}
	
	public Canteens getCanteens() {
		if (canteens == null)
			canteens = new Canteens();

		return canteens;
	}
	
	public void setCanteens(Canteens newCanteens) {
		getCanteens().clear();
		canteens.putAll(newCanteens);
		setChanged();
		
	}

	public void saveCanteens(Context context, Canteens canteens) {
		setCanteens(canteens);
		lastCanteensUpdate = Calendar.getInstance();
		
		saveToPreferences(context);
	}
	
	public ArrayList<Canteen> getFavouriteCanteens() {
		ArrayList<Canteen> favouriteCanteens = new ArrayList<Canteen>();
		Canteens canteens = getCanteens();
		for (String key : favouriteCanteensKeys) {
			Canteen canteen = canteens.get(key);
			if (canteen != null)
				favouriteCanteens.add(canteen);
			else
				Log.w(MainActivity.TAG, String.format("A favourite canteen was requested that is not in the storage. Key: %s", key));
		}
		return favouriteCanteens;
	}


	public void loadFromPreferences(Context context) {
		Storage storage = SettingsUtils.getStorage(context);
		canteen = storage.canteen;
		canteens = storage.canteens;
		lastCanteensUpdate = storage.lastCanteensUpdate;
		currentCanteen = storage.currentCanteen;
		favouriteCanteensKeys = storage.favouriteCanteensKeys;

		SettingsUtils.updateFavouriteCanteensFromPreferences(context);
		setChanged();
	}
	

	public void saveToPreferences(Context context) {
		SettingsUtils.setStorage(context, this);
	}

	public void setCurrentCanteen(Canteen canteen) {
		currentCanteen = canteen.key;
		setChanged();
	}

	public Canteen getCurrentCanteen() {
		if (currentCanteen == null || currentCanteen.isEmpty()) {
			if (getFavouriteCanteens().size() > 0) {
				currentCanteen = getFavouriteCanteens().get(0).key;
			} else {
				return null;
			}	
		}
		return getCanteens().get(currentCanteen);
	}

	public boolean isEmpty() {
		return getCanteens().size() == 0;
	}

	public void setFavouriteCanteens(Set<String> favourites) {
		Log.d(MainActivity.TAG, String.format("Update favourites: %s", favourites));
		favouriteCanteensKeys = favourites;
		setChanged();
	}
}
