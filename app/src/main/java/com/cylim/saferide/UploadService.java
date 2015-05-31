package com.cylim.saferide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by marco on 30/4/15.
 */
public class UploadService extends Service {

    private SharedPreferences mPreferences;
    String userID;

    private final static String ACCEL_UPLOAD_ENDPOINT = "http://saferidebymarco.herokuapp.com/api/v1/accelreport.json";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getString("UserID","");

        DatabaseHandler db = new DatabaseHandler(UploadService.this);
        String[][] reports = db.getReports();

        for (int i = 0; i < reports.length; i++) {
            NewReport nr = new NewReport(UploadService.this, Double.parseDouble(reports[i][1]), Double.parseDouble(reports[i][2]),
                    Integer.parseInt(reports[i][0]), reports[i][3]);
            nr.execute(ACCEL_UPLOAD_ENDPOINT);
        }

    }


    private class NewReport extends UrlJsonAsyncTask {
        double lat, lng;
        int id;
        String time;

        public NewReport(Context context, double lat, double lng, int id, String time) {
            super(context);
            this.lat = lat;
            this.lng = lng;
            this.id = id;
            this.time = time;
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject reportObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    reportObj.put("defects_lat", lat);
                    reportObj.put("defects_lng", lng);
                    reportObj.put("report_time", time);
                    reportObj.put("user_id", userID);
                    holder.put("report", reportObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    DatabaseHandler db = new DatabaseHandler(UploadService.this);
                    db.deleteReport(String.valueOf(id));
                    Log.d("RemovedReport", "Report " + id +" is removed from local database.");
                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}
