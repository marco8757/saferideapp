package com.cylim.saferide;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by marco on 12/4/15.
 */
public class MapActivity extends ActionBarActivity implements OnMapReadyCallback, OnMapLoadedCallback {

    private static final int CAMERA_PICTURE = 1337;
    private static final String REPORTS_URL = "http://saferidebymarco.herokuapp.com/reports.json";
    private final String reportURL = "http://saferidebymarco.herokuapp.com/api/v1/reports.json";
    Bitmap thumbnail;
    List<String> list_lat, list_lng, list_by, list_category;
    GPSTagger gps;
    double lat, lng;
    GoogleMap gm;
    private SharedPreferences mPreferences;
    private String userID;
    private ProgressDialog pDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getString("UserID", "");
//        GetReportTask getReport = new GetReportTask(MapActivity.this);
//        getReport.setMessageLoading("Loading reports...");
//        getReport.execute(REPORTS_URL);
        pDialog = ProgressDialog.show(MapActivity.this,"","Setting up map...", true, false);

        LoadCachedReports loadCachedReports = new LoadCachedReports();
        loadCachedReports.execute();
    }

    private void setupMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapM);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapReady", "true");
        double lat = 0, lng = 0;
        GPSTagger gpsTagger = new GPSTagger(MapActivity.this);
        gm = googleMap;
        if (gpsTagger.canGetLocation()) {

            lat = gpsTagger.getLatitude();
            lng = gpsTagger.getLongitude();

            Log.d("GPSTagger Location", lat + " " + lng);

        } else {
            gpsTagger.showSettingsAlert();
        }

        LatLng mylocation = new LatLng(lat, lng);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 13));
        googleMap.setOnMapLoadedCallback(this);

        for (int i = 0; i < list_lat.size(); i++) {
            if (list_category.get(i).equals("Camera")) {
                gm.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(list_lat.get(i)), Double.parseDouble(list_lng.get(i)))));
            } else {
                CircleOptions cOption = new CircleOptions().radius(20).strokeWidth(1f).strokeColor(10).strokeColor(0x20ff0000).fillColor(0x20ff0000)
                        .center(new LatLng(Double.parseDouble(list_lat.get(i)), Double.parseDouble(list_lng.get(i))));

                Circle circle = gm.addCircle(cOption);
            }
            /// add if else for lists
            ///later can add title, snippet for more information
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_view_report:
                Intent viewreport = new Intent(MapActivity.this, ReportLists.class);
                startActivity(viewreport);
                break;
            case R.id.action_new_report:

                final Dialog dialog = new Dialog(MapActivity.this);
                dialog.setContentView(R.layout.custom_alert_dialog);
                dialog.setTitle("Reporting Methods");

                Button bAuto = (Button) dialog.findViewById(R.id.bCADAuto);
                Button bCamera = (Button) dialog.findViewById(R.id.bCADPicture);
                // if button is clicked, close the custom dialog
                bAuto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ser = new Intent(MapActivity.this, AccelService.class);
                        startService(ser);
                        dialog.dismiss();
                    }
                });
                bCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent newreport = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(newreport, CAMERA_PICTURE);
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case R.id.action_profile:
                Intent profile = new Intent(MapActivity.this, Profile.class);
                startActivity(profile);
                break;
            case R.id.action_logout:
                break;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PICTURE) {
            try {
                thumbnail = (Bitmap) data.getExtras().get("data");
                Log.d("ImageRaw", data.getExtras().get("data").toString());
                gps = new GPSTagger(MapActivity.this);

                if (gps.canGetLocation()) {

                    lat = gps.getLatitude();
                    lng = gps.getLongitude();

                    Log.d("GPSTagger Location", lat + " " + lng);

                    NewReport nr = new NewReport(MapActivity.this);
                    nr.setMessageLoading("Uploading picture...");
                    nr.execute(reportURL);

                } else {
                    gps.showSettingsAlert();
                }


            } catch (RuntimeException e) {
                Log.d("Dashboard", e.toString());
                Toast.makeText(MapActivity.this, "Oops, failed to capture picture.", Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    public void onMapLoaded() {
        Log.d("MapLoaded", "true");
        pDialog.dismiss();
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

                    reportObj.put("picture", "data:image/jpg;base64,(" + encodeTobBase64(thumbnail) + ")");
                    reportObj.put("defects_lat", lat);
                    reportObj.put("defects_lng", lng);
                    reportObj.put("user_id", userID);
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
                    Toast.makeText(MapActivity.this, "Successfully submitted your report.", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    private class GetReportTask extends UrlJsonAsyncTask {
        public GetReportTask(Context context) {
            super(MapActivity.this);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonReports = json.getJSONObject("data").getJSONArray("reports");
                int length = jsonReports.length();
                List<String> reportID = new ArrayList<String>(length);
                List<String> reportLat = new ArrayList<String>(length);
                List<String> reportLng = new ArrayList<String>(length);
                List<String> reportBy = new ArrayList<String>(length);
                List<String> reportType = new ArrayList<String>(length);


                for (int i = 0; i < length; i++) {
                    reportID.add(jsonReports.getJSONObject(i).getString("id"));
                    reportLat.add(jsonReports.getJSONObject(i).getString("defects_lat"));
                    reportLng.add(jsonReports.getJSONObject(i).getString("defects_lng"));
                    reportBy.add(jsonReports.getJSONObject(i).getString("name"));
                    reportType.add(jsonReports.getJSONObject(i).getString("cat"));
                }
                list_lat = new ArrayList<String>(reportLat);
                list_lng = new ArrayList<String>(reportLng);
                list_by = new ArrayList<String>(reportBy);
                list_category = new ArrayList<String>(reportType);

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
                setupMap();
            }
        }
    }

    private class LoadCachedReports extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        Context context;

        String result[][];
        List<String> reportLat;
        List<String> reportLng;
        List<String> reportBy;
        List<String> reportType;

        @Override
        protected void onPreExecute() {
            try {
                progressDialog = ProgressDialog.show(MapActivity.this, "", "Loading Reports...", true, false);
            } catch (Exception e) {
                Log.e("ProgressDialog", e.toString());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            DatabaseHandler db = new DatabaseHandler(MapActivity.this);
            result = db.getCachedReports();
            reportLat = new ArrayList<String>(result.length);
            reportLng = new ArrayList<String>(result.length);
            reportBy = new ArrayList<String>(result.length);
            reportType = new ArrayList<String>(result.length);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            for (int i = 0; i < result.length; i++) {
                reportLat.add(i, result[i][1]);
                reportLng.add(i, result[i][2]);
                reportBy.add(i, result[i][3]);
                reportType.add(i, result[i][4]);
            }

            list_lat = new ArrayList<String>(reportLat);
            list_lng = new ArrayList<String>(reportLng);
            list_by = new ArrayList<String>(reportBy);
            list_category = new ArrayList<String>(reportType);

            setupMap();

            if (progressDialog != null) {
                progressDialog.dismiss();
            }


        }
    }
}
