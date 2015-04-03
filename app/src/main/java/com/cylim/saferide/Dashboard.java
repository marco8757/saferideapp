package com.cylim.saferide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by marco on 30/3/15.
 */
public class Dashboard extends Activity implements View.OnClickListener {

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
                Intent newreport = new Intent(Dashboard.this, NewReport.class);
                startActivity(newreport);
                break;
            case R.id.tvDViewReports:
            case R.id.ivDViewReports:
                Intent viewreport = new Intent(Dashboard.this, NewReport.class);
                startActivity(viewreport);
                break;
            case R.id.tvDMyProfile:
            case R.id.ivDMyProfile:
                Intent profile = new Intent(Dashboard.this, NewReport.class);
                startActivity(profile);
                break;

        }

    }
}
