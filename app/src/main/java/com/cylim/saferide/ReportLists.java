package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 30/3/15.
 */
public class ReportLists extends Activity {

    private static final String REPORTS_URL = "http://192.168.1.131:8080/reports.json";
    ListView lvReport;

    List<String> reportURL;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_lists);
        lvReport = (ListView) findViewById (R.id.lvRList);
        loadReportsFromServer(REPORTS_URL);
    }

    private void loadReportsFromServer(String url) {
        GetReportTask getReport = new GetReportTask(ReportLists.this);
        getReport.setMessageLoading("Loading reports...");
        getReport.execute(url);
    }

    private class GetReportTask extends UrlJsonAsyncTask {
        public GetReportTask(Context context) {
            super(ReportLists.this);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonReports = json.getJSONObject("data").getJSONArray("reports");
                int length = jsonReports.length();
                List<String> reportID = new ArrayList<String>(length);
                List<String> reportLat = new ArrayList<String>(length);
                List<String> reportLng = new ArrayList<String>(length);
                reportURL = new ArrayList<String>(length);


                for (int i = 0; i < length; i++) {
                    reportID.add(jsonReports.getJSONObject(i).getString("id"));
                    reportLat.add(jsonReports.getJSONObject(i).getString("defects_lat"));
                    reportLng.add(jsonReports.getJSONObject(i).getString("defects_lng"));
                    reportURL.add(jsonReports.getJSONObject(i).getString("url"));
                }

                if (lvReport != null) {
                    lvReport.setAdapter(new ArrayAdapter<String>(ReportLists.this,
                            android.R.layout.simple_list_item_1, reportID));

                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);



                lvReport.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Intent i = new Intent(ReportLists.this,ReportDetails.class);
                        i.putExtra("ReportURL", reportURL.get((int) id));
                        startActivity(i);
                    }
                });

            }
        }
    }
}
