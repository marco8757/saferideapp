package com.cylim.saferide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.savagelook.android.UrlJsonAsyncTask;
import com.squareup.picasso.Picasso;

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
public class ReportDetails extends Activity implements OnMapReadyCallback {

    private final String ratingURL = "http://mysaferide.herokuapp.com/api/v1/rate.json";
    private final String commentURL = "http://mysaferide.herokuapp.com/api/v1/comment.json";
    RatingBar rb;
    Button bPost, bImage;
    EditText etComment;
    ListView lvComments;
    TextView tvAuthor;
    String URL;
    String comment;
    String reportID, lat, lng, rate, imageURL, userID;
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_details);

        rb = (RatingBar) findViewById(R.id.rbRRating);
        bPost = (Button) findViewById(R.id.bRPost);
        bImage = (Button) findViewById(R.id.bRViewImage);
        etComment = (EditText) findViewById(R.id.etRComment);
        lvComments = (ListView) findViewById(R.id.lvRComments);
        tvAuthor = (TextView) findViewById(R.id.tvRReportedBy);
        rb.setStepSize(1);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userID = mPreferences.getString("UserID", "");

        //get url from previous activity for information retrieval
        Bundle b = getIntent().getExtras();
        URL = b.getString("ReportURL");
        tvAuthor.setText(getString(R.string.reported) + b.getString("Name"));

        loadReportsFromServer(URL);
    }

    private void loadReportsFromServer(String url) {
        GetReportTask getReport = new GetReportTask(ReportDetails.this);
        getReport.setMessageLoading(getString(R.string.loadingReport));
        getReport.execute(url);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng defect = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

        //setup map property
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defect, 13));
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.addMarker(new MarkerOptions()
                .position(defect));
    }

    private void setupMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapR);
        mapFragment.getMapAsync(this);
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
                imageURL = json.getJSONObject("data").getString("picture_url");
                reportID = json.getJSONObject("data").getString("id");

                if (imageURL != "null") {
                    Log.d("image", imageURL);
                    bImage.setVisibility(View.VISIBLE);
                    bImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final Dialog dialog = new Dialog(ReportDetails.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.image_dialog);
                            dialog.getWindow().setLayout(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);

                            ImageView ivReport = (ImageView) dialog.findViewById(R.id.ivIDmain);

                            Picasso.with(context).load(imageURL).into(ivReport);

                            dialog.show();
                        }
                    });
                }

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
                            R.layout.custom_comment_list_item, commentContent, commentAuthor);
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
                            commentTask.setMessageLoading(getString(R.string.postingComment));
                            commentTask.execute(commentURL);
                        } else {
                            Toast.makeText(ReportDetails.this, getString(R.string.fillComment), Toast.LENGTH_LONG).show();
                        }

                    }
                });
                rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        rate = String.valueOf(rb.getRating());

                        RatingUpdateTask updateRate = new RatingUpdateTask(ReportDetails.this);
                        updateRate.setMessageLoading(getString(R.string.ratingReport));
                        updateRate.execute(ratingURL);
                    }
                });

                setupMap();

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
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    ratingObj.put("rate", rate);
                    ratingObj.put("report_id", reportID);
                    ratingObj.put("user_id", userID);
                    holder.put("rating", ratingObj);
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
                    Toast.makeText(ReportDetails.this, getString(R.string.rateSuccessful), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
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
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    commentObj.put("content", comment);
                    commentObj.put("report_id", reportID);
                    commentObj.put("user_id", userID);
                    holder.put("comment", commentObj);
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
                    Toast.makeText(ReportDetails.this, getString(R.string.commentSuccessful), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
