package de.haw_landshut.lackmann.haw_mensa;

import com.google.gson.annotations.SerializedName;

class Prices {
	@SerializedName("students")
	float students;
	
	@SerializedName("employees")
	float employees;
	
	@SerializedName("pupils")
	float pupils;
	
	@SerializedName("others")
	float others;
}
