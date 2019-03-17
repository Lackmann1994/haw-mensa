package de.haw_landshut.lackmann.haw_mensa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvRow;


public class RetrieveDaysFeedTaskCSV extends RetrieveFeedTask {

    private Days days;
    private OnFinishedFetchingDaysCSVListener fetchListener;
    protected String name = "Days";
    protected Canteen canteen;
    public String dateString = "";

    public RetrieveDaysFeedTaskCSV(Context context, Activity activity, OnFinishedFetchingDaysCSVListener fetchListener, Canteen canteen, String dateString) {
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

    protected void parseFromCSV(CsvParser parser, CsvParser parser2) throws IOException, ParseException {
        days = new Days();
        List<Meals> mealslist2 = new ArrayList<>();
        CsvRow row;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat input = new SimpleDateFormat("dd.MM.yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
        while ((
                (row = parser.nextRow()) != null) ||
                ((row = parser2.nextRow()) != null)
                ) {
            if (row.getField(0).equals("datum")) continue;

            //Convert to format of openmensa.org
            String date = output.format(input.parse(row.getField(0)));

            String name = row.getField(3);
            name = name.replaceAll("\\s*\\([^\\)]*\\)\\s*", " ");

            String category = row.getField(2);
            switch (category) {
                case "HG1":
                case "HG2":
                case "HG3":
                case "HG4":
                    category = "Hauptgericht";
                    break;
                case "B1":
                case "B2":
                case "B3":
                case "B4":
                    category = "Beilage";
                    break;
                case "N1":
                case "N2":
                case "N3":
                case "N4":
                    category = "Nachspeise";
                    break;
            }


            String[] notes = row.getField(4).split("\\,");
            for (int i = 0; i < notes.length; i++) {
                switch (notes[i]) {
                    case "V":
                        notes[i] = "vegetarisch";
                        break;
                    case "VG":
                        notes[i] = "vegan";
                        break;
                    case "G":
                        notes[i] = "Geflügel";
                        break;
                    case "S":
                        notes[i] = "Schweinefleisch";
                        break;
                    case "R":
                        notes[i] = "Rindfleisch";
                        break;
                    case "L":
                        notes[i] = "Lamm";
                        break;
                    case "W":
                        notes[i] = "Wild";
                        break;
                    case "F":
                        notes[i] = "Fisch";
                        break;
                    case "A":
                        notes[i] = "Alcohol";
                        break;
                    case "M":
                        notes[i] = "Mensa Vital";
                        break;
                    case "J":
                        notes[i] = "Juradistl";
                        break;
                    case "BI":
                        notes[i] = "bioland";
                        break;
                    case "B":
                        notes[i] = "mit ausschließlich biologisch erzeugten Rohstoffen";
                        break;
                }
            }


            if (mealslist2.size() == 0 || !mealslist2.get(mealslist2.size() - 1).get(0).date.equals(date))
                mealslist2.add(new Meals());
            Meal meal = new Meal(category, name);
            meal.notes = notes;
            Prices prices = new Prices();
            prices.students = Float.parseFloat(row.getField(6).replace(',', '.'));
            prices.employees = Float.parseFloat(row.getField(7).replace(',', '.'));
            prices.others = Float.parseFloat(row.getField(8).replace(',', '.'));
            meal.prices = prices;
            meal.date = date;
            mealslist2.get(mealslist2.size() - 1).add(meal);
            List list = row.getFields();
            Log.d("readerDebug", "Read line: " + list);


        }
        for (Meals meals : mealslist2) {
            Day day = new Day(meals.get(0).date);
            day.meals = meals;
            days.add(day);
        }

        Log.d("tag", "done");


//		days = gson.fromJson(jsonString, Days.class);
//		for (Day day : days) {
//			if (day.meals == null || day.date == null) {
//				Log.w(MainActivity.TAG, "Incomplete json response from server. Meals or date is null");
//				this.exception = new Exception("Incomplete response from server.");
//			}
//		}
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