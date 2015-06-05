package com.cylim.saferide;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    //setup camera response code
    private static final int CAMERA_PICTURE = 1337;
    private final String reportURL = "http://mysaferide.herokuapp.com/api/v1/reports.json";
    Bitmap thumbnail;
    List<String> list_lat, list_lng, list_by, list_category;
    GPSTagger gps;
    double lat, lng;
    GoogleMap gm;
    private SharedPreferences mPreferences;
    private String userID;
    private ProgressDialog pDialog;

    //convert bitmap images into base64 code
    public static String encodeTobBase64(Bitmap image) {

        if (image == null)
            return null;

        Bitmap bm = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //compress image into JPEG with quality of 100 into baos
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] b = baos.toByteArray();

        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        //retrieval of userID from sharedpreferences
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getString("UserID", "");

        //setup progress dialog
        pDialog = ProgressDialog.show(MapActivity.this, "", "Setting up map...", true, false);

        //load cache from local database to avoid lag
        LoadCachedReports loadCachedReports = new LoadCachedReports();
        loadCachedReports.execute();
    }

    //setup map fragment and get async map in mapactivity context
    private void setupMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapM);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapReady", "true");
        double lat = 0, lng = 0;

        //retrieve user's coordinate to zoom into this location on map
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
        //zoom into user's location with zoom level of 13/18
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 13));
        googleMap.setOnMapLoadedCallback(this);

        //setup markers and circles on map
        for (int i = 0; i < list_lat.size(); i++) {
            if (list_category.get(i).equals("Camera")) {
                gm.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(list_lat.get(i)), Double.parseDouble(list_lng.get(i)))));
            } else {
                CircleOptions cOption = new CircleOptions().radius(20).strokeWidth(1f).strokeColor(10).strokeColor(0x20ff0000).fillColor(0x20ff0000)
                        .center(new LatLng(Double.parseDouble(list_lat.get(i)), Double.parseDouble(list_lng.get(i))));

                Circle circle = gm.addCircle(cOption);
            }
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

                //setup custom dialog for report method selection
                final Dialog dialog = new Dialog(MapActivity.this);
                dialog.setContentView(R.layout.custom_alert_dialog);
                dialog.setTitle("Reporting Methods");

                Button bAuto = (Button) dialog.findViewById(R.id.bCADAuto);
                Button bCamera = (Button) dialog.findViewById(R.id.bCADPicture);
                bAuto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ser = new Intent(MapActivity.this, AccelService.class);
                        startService(ser);
                        Notification();
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
                mPreferences.edit().clear().commit();
                finish();
                Toast.makeText(MapActivity.this, "Successfully logged out.", Toast.LENGTH_LONG).show();
                Intent login = new Intent(MapActivity.this, MainActivity.class);
                startActivity(login);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //setup notification
    public void Notification() {
        //setup intent and pending intent to stop AccelService later
        Intent ser = new Intent(MapActivity.this, StopAccelService.class);

        PendingIntent pIntent = PendingIntent.getService(this, 0, ser,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //setup notification with details and intent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.logo)
                        //ticker message when notification first come up
                .setTicker("SafeRide is up, drive safe!")
                        //title of notification
                .setContentTitle("SafeRide")
                        //secondary text of notification
                .setContentText("Drive safe!")
                        //add actionbutton into notification
                .addAction(R.mipmap.logo, "Stop Driving.", pIntent)
                        //set pending intent into notification
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        //display notification that is set up
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(0, builder.build());

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //when camera return with result
        if (requestCode == CAMERA_PICTURE) {
            try {
                //retrieve data and save into thumbnail
                thumbnail = (Bitmap) data.getExtras().get("data");
                Log.d("ImageRaw", data.getExtras().get("data").toString());
                gps = new GPSTagger(MapActivity.this);

                if (gps.canGetLocation()) {

                    lat = gps.getLatitude();
                    lng = gps.getLongitude();

                    Log.d("GPSTagger Location", lat + " " + lng);

                    //execute upload of image
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
        //dismiss progress dialog that shows loading map when map is loaded.
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

                    //get current timestamp
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

                    //setup the request headers
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
                //display progress dialog
                progressDialog = ProgressDialog.show(MapActivity.this, "", "Loading Reports...", true, false);
            } catch (Exception e) {
                Log.e("ProgressDialog", e.toString());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //retrieve data from local database
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
