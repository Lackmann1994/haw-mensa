package de.haw_landshut.lackmann.haw_mensa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import de.siegmar.fastcsv.reader.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RetrieveDays extends AsyncTask<String, Integer, Void> {

    private Exception exception;
    private ProgressDialog dialog;
    private AlertDialog.Builder builder;
    @SuppressLint("StaticFieldLeak")
    protected Context context;
    protected Gson gson = new Gson();

    public static final String TAG = MainActivity.TAG;
    public static final Boolean LOGV = MainActivity.LOGV;

    private final int DEFAULT_BUFFER_SIZE = 1024;
    private Integer totalPages = null;
    private Boolean visible = false;


    private Activity mActivity;

    @Override
    protected Void doInBackground(String... urls) {

        if (urls.length != 2)
            throw new IllegalArgumentException("You need to provide exactly two.");
        String url = urls[0];


        try {
            URL feed = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) feed.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "Cp1252"));

            CsvReader csvReader = new CsvReader();

            csvReader.setFieldSeparator(';');
            CsvParser parser = csvReader.parse(in);

            CsvRow row;
            while ((row = parser.nextRow()) != null) {
                List list = row.getFields();
                Log.d("readerDebug", "Read line: " + list);
            }
            long fileLength = urlConnection.getContentLength();

            totalPages = urlConnection.getHeaderFieldInt("X-Total-Pages", -1);
            if (totalPages < 0) {
                totalPages = null;
            }

            if (visible) {
                if (fileLength < 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setIndeterminate(true);
                        }
                    });
                } else {
                    final int finalFileLength = (int) fileLength;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMax(finalFileLength);
                        }
                    });
                }
            }


        } catch (Exception ex) {
            this.exception = ex;
        } finally {
        }
        return null;
    }
}
