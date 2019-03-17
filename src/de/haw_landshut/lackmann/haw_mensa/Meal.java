package de.haw_landshut.lackmann.haw_mensa;

import com.google.gson.annotations.SerializedName;

public class Meal {
	@SerializedName("name")
	public String name;
	
	@SerializedName("notes")
	public String[] notes;
	
	@SerializedName("prices")
	public Prices prices;
	
	@SerializedName("category")
	public String category;
	
	// TODO: maybe auto-parse into Date from ISO8601
	@SerializedName("date")
	public String date;
	
	public Meal(String category, String name) {
		this.category = category;
		this.name = name;
	}
}
