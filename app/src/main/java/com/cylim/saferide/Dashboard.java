package com.cylim.saferide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by marco on 30/3/15.
 */
public class Dashboard extends Activity implements View.OnClickListener {

    private static final int CAMERA_PICTURE = 1337;
    ImageView ivNewReport, ivViewReport, ivProfile;
    TextView tvNewReport, tvViewReport, tvProfile;

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
        if (requestCode == CAMERA_PICTURE){
            try {
                Toast.makeText(Dashboard.this, "CAMERA LAUNCHED", Toast.LENGTH_LONG).show();
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                Log.d("ImageRaw", data.getExtras().get("data").toString());
                //post thumbnail to server
                ivNewReport.setImageBitmap(thumbnail);
                GPSTagger gps = new GPSTagger(Dashboard.this);

                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    Log.d("GPSTagger Location", latitude + " " + longitude);
                } else {
                    gps.showSettingsAlert();
                }

            }catch (RuntimeException e){
                Log.d("Dashboard", e.toString());
                Toast.makeText(Dashboard.this, "Oops, failed to capture picture.", Toast.LENGTH_LONG).show();
            }

        }
    }


}
