package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 4/4/15.
 */
public class ReportDetails extends Activity {

    RatingBar rb;
    Button bPost;
    EditText etComment;
    ListView lvComments;
    String URL;
    String lat,lng,rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_details);
        rb = (RatingBar) findViewById(R.id.rbRRating);
        bPost = (Button) findViewById(R.id.bRPost);
        etComment = (EditText) findViewById(R.id.etRComment);
        lvComments = (ListView) findViewById(R.id.lvRComments);
        rb.setStepSize(1);

        Bundle b = getIntent().getExtras();
        URL = b.getString("ReportURL");

        loadReportsFromServer(URL);
    }
    private void loadReportsFromServer(String url) {
        GetReportTask getReport = new GetReportTask(ReportDetails.this);
        getReport.setMessageLoading("Loading report...");
        getReport.execute(url);
    }

    private class GetReportTask extends UrlJsonAsyncTask {
        public GetReportTask(Context context) {
            super(ReportDetails.this);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                lat = json.getJSONObject("data").getString("defects_lat");
                lng = json.getJSONObject("data").getString("defects_lng");
                rating = json.getJSONObject("data").getString("ratings");

                JSONArray jsonComments = json.getJSONObject("data").getJSONArray("comments");
                int length = jsonComments.length();
                List<String> commentID = new ArrayList<String>(length);
                List<String> commentContent = new ArrayList<String>(length);

                for (int i = 0; i < length; i++) {
                    commentID.add(jsonComments.getJSONObject(i).getString("id"));
                    commentContent.add(jsonComments.getJSONObject(i).getString("content"));
                }

                if (lvComments != null) {
                    lvComments.setAdapter(new ArrayAdapter<String>(ReportDetails.this,
                            android.R.layout.simple_list_item_1, commentContent));

                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);

            }
        }
    }

}
