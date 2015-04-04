package com.cylim.saferide;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

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


        //execute asynctask to retrieve data

    }
}
