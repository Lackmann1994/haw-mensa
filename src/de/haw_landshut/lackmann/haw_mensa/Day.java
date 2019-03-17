package de.haw_landshut.lackmann.haw_mensa;

import com.google.gson.annotations.SerializedName;

public class Day {
	public Day(String date) {
		this.date = date;
	}
	
	@SerializedName("date")
	public String date;
	
	@SerializedName("closed")
	public Boolean closed = false;
	
	@SerializedName("meals")
	public Meals meals = new Meals();
	
	@Override
	public String toString() {
		return date;
	}

	public Meals getMeals() {
		return meals;
	}
	
	/**
	 * Day can be a placeholder object for the case where
	 * we have not found any information at all.
	 */
	public boolean isNullObject() {
		return meals.isEmpty();
	}
}
