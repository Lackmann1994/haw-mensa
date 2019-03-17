package de.haw_landshut.lackmann.haw_mensa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;


public abstract class RetrieveFeedTask extends AsyncTask<String, Integer, Void> {
    protected Exception exception;
    private Builder builder;
    protected Context context;
    protected Gson gson = new Gson();

    protected static Set<String> currentlyRequestingFrom = new HashSet<String>();

    protected Integer totalPages = null;

    protected Boolean visible = false;
    protected String name = "";

    public static final String TAG = MainActivity.TAG;
    public static final Boolean LOGV = MainActivity.LOGV;

    private final int DEFAULT_BUFFER_SIZE = 1024;


    public RetrieveFeedTask(Context context, Activity activity) {
        builder = new AlertDialog.Builder(context)
                .setNegativeButton("Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
    }

    protected abstract void onPostExecuteFinished();

    protected abstract void parseFromJSON(String jsonString);

    protected abstract void parseFromCSV(CsvParser parser, CsvParser parser2) throws IOException, ParseException;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Void doInBackground(String... urls) {

        String url = urls[0];
        currentlyRequestingFrom.add(url);

        if (url.startsWith("https://stwno.de")) {
            try {
                URL feed = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) feed.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream(), "Cp1252"));

                URL feed2 = new URL(urls[1]);
                HttpURLConnection urlConnection2 = (HttpURLConnection) feed2.openConnection();
                BufferedReader in2 = new BufferedReader(new InputStreamReader(
                        urlConnection2.getInputStream(), "Cp1252"));

                CsvReader csvReader = new CsvReader();
                csvReader.setFieldSeparator(';');

                parseFromCSV(csvReader.parse(in), csvReader.parse(in2));
            } catch (Exception ex) {
                this.exception = ex;
            } finally {
                currentlyRequestingFrom.remove(url);
            }
            return null;
        } else {
            try {
                URL feed = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) feed.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                int count;
                StringBuilder builder = new StringBuilder();

                char buf[] = new char[DEFAULT_BUFFER_SIZE];
                while ((count = in.read(buf, 0, DEFAULT_BUFFER_SIZE)) > 0) {
                    builder.append(buf, 0, count);
                }

                String json = builder.toString();

                if (urls.length == 2) {
                    URL feed2 = new URL(urls[1]);
                    HttpURLConnection urlConnection2 = (HttpURLConnection) feed2.openConnection();
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(
                            urlConnection2.getInputStream()));
                    int count2;
                    StringBuilder builder2 = new StringBuilder();

                    char buf2[] = new char[DEFAULT_BUFFER_SIZE];
                    while ((count2 = in2.read(buf2, 0, DEFAULT_BUFFER_SIZE)) > 0) {
                        builder2.append(buf2, 0, count2);
                    }
                    if (builder2.toString().length() > 3)
                        json = json.substring(0, builder.toString().length() - 1) + ',' + builder2.toString().substring(1);


                }

                Log.d("tag", json);
                parseFromJSON(json);
            } catch (Exception ex) {
                this.exception = ex;
            } finally {
                currentlyRequestingFrom.remove(url);
            }
            return null;
        }
    }

    protected void onPostExecute(Void v) {
        if (this.exception != null) {
            Log.w(TAG, "Exception: " + exception.getMessage());
            if (LOGV) {
                Log.d(TAG, Log.getStackTraceString(exception));
            }
            showErrorMessage(this.exception);
        } else {
            onPostExecuteFinished();
        }
    }

    private void showErrorMessage(Exception ex) {
        builder.setTitle(ex.getClass().getName());
        builder.setMessage(ex.toString());
        if (!((Activity) MainActivity.getAppContext()).isFinishing()) {
            builder.show();
        }
    }

    Boolean noPending() {
        return currentlyRequestingFrom.isEmpty();
    }
}
