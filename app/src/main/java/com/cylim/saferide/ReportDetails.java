package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 4/4/15.
 */
public class ReportDetails extends Activity {

    private final String ratingURL = "http://saferidebymarco.herokuapp.com/api/v1/rate.json";
    private final String commentURL = "http://saferidebymarco.herokuapp.com/api/v1/comment.json";
    RatingBar rb;
    Button bPost;
    EditText etComment;
    ListView lvComments;
    String URL;
    String comment;
    String reportID, lat, lng, rate, userID;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_details);
        rb = (RatingBar) findViewById(R.id.rbRRating);
        bPost = (Button) findViewById(R.id.bRPost);
        etComment = (EditText) findViewById(R.id.etRComment);
        lvComments = (ListView) findViewById(R.id.lvRComments);
        rb.setStepSize(1);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getString("UserID", "");

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
                rate = json.getJSONObject("data").getString("ratings");
                reportID = json.getJSONObject("data").getString("id");

                JSONArray jsonComments = json.getJSONObject("data").getJSONArray("comments");
                int length = jsonComments.length();
                List<String> commentID = new ArrayList<String>(length);
                List<String> commentContent = new ArrayList<String>(length);
                List<String> commentAuthor = new ArrayList<String>(length);

                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        commentID.add(jsonComments.getJSONObject(i).getString("id"));
                        commentContent.add(jsonComments.getJSONObject(i).getString("content"));
                        commentAuthor.add(jsonComments.getJSONObject(i).getString("name"));
                    }
                }

                if (lvComments != null) {

                    CommentCustomListAdapter adapter = new CommentCustomListAdapter(ReportDetails.this,
                            R.layout.custom_comment_list_item, commentContent , commentAuthor);
                    lvComments.setAdapter(adapter);

                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);

                bPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        comment = etComment.getText().toString();
                        if (!comment.isEmpty()) {
                            PostCommentTask commentTask = new PostCommentTask(ReportDetails.this);
                            commentTask.setMessageLoading("Posting comment...");
                            commentTask.execute(commentURL);
                        }else{
                            Toast.makeText(ReportDetails.this, "Please fill in comment and try again.", Toast.LENGTH_LONG).show();
                        }

                    }
                });
                rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        rate = String.valueOf(rb.getRating());

                        RatingUpdateTask updateRate = new RatingUpdateTask(ReportDetails.this);
                        updateRate.setMessageLoading("Rating the report...");
                        updateRate.execute(ratingURL);
                    }
                });

            }
        }
    }


    private class RatingUpdateTask extends UrlJsonAsyncTask {
        public RatingUpdateTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject ratingObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    ratingObj.put("rate", rate);
                    ratingObj.put("report_id", reportID);
                    ratingObj.put("user_id", userID);
                    holder.put("rating", ratingObj);
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
                    Toast.makeText(ReportDetails.this, "Successfully rated this report.", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    private class PostCommentTask extends UrlJsonAsyncTask {
        public PostCommentTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject commentObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    commentObj.put("content", comment);
                    commentObj.put("report_id", reportID);
                    commentObj.put("user_id", userID);
                    holder.put("comment", commentObj);
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
                    Toast.makeText(ReportDetails.this, "Successfully posted your comment.", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
