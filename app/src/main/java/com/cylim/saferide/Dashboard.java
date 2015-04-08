package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by marco on 30/3/15.
 */
public class Dashboard extends Activity implements View.OnClickListener {

    private static final int CAMERA_PICTURE = 1337;
    private final String reportURL = "http://192.168.1.131:8080/api/v1/reports.json";
    ImageView ivNewReport, ivViewReport, ivProfile;
    TextView tvNewReport, tvViewReport, tvProfile;
    Bitmap thumbnail;
    GPSTagger gps;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        tvNewReport = (TextView) findViewById(R.id.tvDNewReport);
        tvViewReport = (TextView) findViewById(R.id.tvDViewReports);
        tvProfile = (TextView) findViewById(R.id.tvDMyProfile);

        ivNewReport = (ImageView) findViewById(R.id.ivDNewReport);
        ivViewReport = (ImageView) findViewById(R.id.ivDViewReports);
        ivProfile = (ImageView) findViewById(R.id.ivDMyProfile);

        ivNewReport.setOnClickListener(Dashboard.this);
        ivViewReport.setOnClickListener(Dashboard.this);
        ivProfile.setOnClickListener(Dashboard.this);
        tvNewReport.setOnClickListener(Dashboard.this);
        tvViewReport.setOnClickListener(Dashboard.this);
        tvProfile.setOnClickListener(Dashboard.this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tvDNewReport:
            case R.id.ivDNewReport:
                Intent newreport = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(newreport, CAMERA_PICTURE);
                break;
            case R.id.tvDViewReports:
            case R.id.ivDViewReports:
                Intent viewreport = new Intent(Dashboard.this, ReportLists.class);
                startActivity(viewreport);
                break;
            case R.id.tvDMyProfile:
            case R.id.ivDMyProfile:
                Intent profile = new Intent(Dashboard.this, NewReport.class);
                startActivity(profile);
                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PICTURE) {
            try {
                Toast.makeText(Dashboard.this, "CAMERA LAUNCHED", Toast.LENGTH_LONG).show();
                thumbnail = (Bitmap) data.getExtras().get("data");
                Log.d("ImageRaw", data.getExtras().get("data").toString());
                ivNewReport.setImageBitmap(thumbnail);
                gps = new GPSTagger(Dashboard.this);

                if (gps.canGetLocation()) {

                    lat = gps.getLatitude();
                    lng = gps.getLongitude();

                    Log.d("GPSTagger Location", lat + " " + lng);

                    NewReport nr = new NewReport(Dashboard.this);
                    nr.setMessageLoading("Uploading picture...");
                    nr.execute(reportURL);

                } else {
                    gps.showSettingsAlert();
                }


            } catch (RuntimeException e) {
                Log.d("Dashboard", e.toString());
                Toast.makeText(Dashboard.this, "Oops, failed to capture picture.", Toast.LENGTH_LONG).show();
            }

        }

    }

    public static String encodeTobBase64(Bitmap image) {

        if (image == null)
            return null;

        Bitmap bm = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] b = baos.toByteArray();

        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    private class NewReport extends UrlJsonAsyncTask {
        public NewReport(Context context) {
            super(context);
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

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
                    String currentTimeStamp = dateFormat.format(new Date());

                    reportObj.put("picture", "data:image/jpg;base64,("+ encodeTobBase64(thumbnail) + ")");
                    reportObj.put("defects_lat", lat);
                    reportObj.put("defects_lng", lng);
                    reportObj.put("report_time", currentTimeStamp);
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
                    Toast.makeText(Dashboard.this, "Successfully submitted your report.", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
