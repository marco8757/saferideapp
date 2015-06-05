package com.cylim.saferide;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;
import com.squareup.picasso.Picasso;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by marco on 3/4/15.
 */
public class Profile extends ActionBarActivity {
    Button bUpdate, bCancel;
    TextView tvName, tvEmail;
    EditText etName;
    ImageView ivProfilePicture;
    Boolean editMode = false;
    private SharedPreferences mPreferences;
    private final static String PROFILE_ENDPOINT = "http://mysaferide.herokuapp.com/api/v1/update_profile.json";
    String emailHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        bUpdate = (Button) findViewById(R.id.bPUpdateProfile);
        bCancel = (Button) findViewById(R.id.bPCancel);
        etName = (EditText) findViewById(R.id.etPName);
        tvEmail = (TextView) findViewById(R.id.tvPEmail);
        tvName = (TextView) findViewById(R.id.tvPName);
        ivProfilePicture = (ImageView) findViewById(R.id.ivPProfilePicture);

        tvName.setText(mPreferences.getString("Username", ""));
        tvEmail.setText(mPreferences.getString("Email", ""));

        //convert email into md5hash
        emailHash = md5Hash(mPreferences.getString("Email", "").trim());

        if (!emailHash.contains("null")) {
            //retrieve user's image from Gravatar using Picasso library
            Picasso.with(Profile.this).load("http://www.gravatar.com/avatar/" + emailHash + "?s=200").into(ivProfilePicture);
        }


        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                if (editMode == false) {
                    editMode = true;
                    bUpdate.setText(getString(R.string.save));
                    bCancel.setVisibility(View.VISIBLE);
                    etName.setVisibility(View.VISIBLE);
                    tvName.setVisibility(View.GONE);
                } else {
                    if (etName.getText().toString().length() > 0) {
                        UpdateProfile updateProfile = new UpdateProfile(Profile.this);
                        updateProfile.execute(PROFILE_ENDPOINT);
                    } else {
                        Toast.makeText(Profile.this, getString(R.string.fillAllFields), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                editModeOff();
            }
        });

    }

    //convert email into md5hash
    private String md5Hash(String email) {
        String result = "null";
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            result = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    //trigger views visibilities
    private void editModeOff() {
        editMode = false;
        bUpdate.setText(getString(R.string.updateProfile));
        bCancel.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
    }


    private class UpdateProfile extends UrlJsonAsyncTask {

        public UpdateProfile(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject profileObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    profileObj.put("authentication_token", mPreferences.getString("AuthToken", ""));
                    profileObj.put("id", mPreferences.getString("UserID", ""));
                    profileObj.put("name", etName.getText().toString());
                    holder.put("user", profileObj);
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
                    editModeOff();
                    tvName.setText(etName.getText().toString());

                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("Username", etName.getText().toString());
                    editor.commit();
                }

            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}
