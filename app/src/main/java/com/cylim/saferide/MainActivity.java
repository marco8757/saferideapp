package com.cylim.saferide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity {

    private final static String LOGIN_API_ENDPOINT_URL = "http://saferidebymarco.herokuapp.com/api/v1/sessions.json";
    private final static String REGISTER_API_ENDPOINT_URL = "http://saferidebymarco.herokuapp.com/api/v1/registrations";
    Button bLogin, bRegister;
    EditText etEmail, etPassword;
    private String rEmail, rPassword, rPassword2;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bLogin = (Button) findViewById(R.id.bMLogin);
        bRegister = (Button) findViewById(R.id.bMRegister);
        etEmail = (EditText) findViewById(R.id.etMEmail);
        etPassword = (EditText) findViewById(R.id.etMPassword);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);


        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new Dialog(MainActivity.this);
                d.setContentView(R.layout.registration);
                d.setTitle("Registration");

                final EditText etREmail = (EditText) d.findViewById(R.id.etREmail);
                final EditText etRPassword = (EditText) d.findViewById(R.id.etRPassword);
                final EditText etRPassword2 = (EditText) d.findViewById(R.id.etRPassword2);
                final Button bRSubmit = (Button) d.findViewById(R.id.bRRegister);

                bRSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hideKeyboard();

                        if (etREmail.getText().toString().length() == 0 || etRPassword.getText().toString().length() == 0 || etRPassword2.getText().toString().length() == 0) {
                            // input fields are empty
                            Toast.makeText(MainActivity.this, "Please complete all the fields",
                                    Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            if (!etRPassword.getText().toString().equals(etRPassword2.getText().toString())) {
                                // password doesn't match confirmation
                                Toast.makeText(MainActivity.this, "Password Mismatch",
                                        Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                rEmail = etREmail.getText().toString();
                                rPassword = etRPassword.getText().toString();
                                rPassword2 = etRPassword2.getText().toString();
                                // everything is ok!
                                RegisterTask registerTask = new RegisterTask(MainActivity.this);
                                registerTask.setMessageLoading("Registering new account...");
                                registerTask.execute(REGISTER_API_ENDPOINT_URL);
                                d.dismiss();
                            }
                        }

                    }
                });

                d.show();

            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();

                if (etEmail.getText().toString().length() == 0 || etPassword.getText().toString().length() == 0) {
                    // input fields are empty
                    Toast.makeText(MainActivity.this, "Please complete all the fields",
                            Toast.LENGTH_LONG).show();
                    return;

                } else {
                    LoginTask loginTask = new LoginTask(MainActivity.this);
                    loginTask.setMessageLoading("Logging in...");
                    loginTask.execute(LOGIN_API_ENDPOINT_URL);
                }
            }
        });


    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private class LoginTask extends UrlJsonAsyncTask {
        public LoginTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    // add the user email and password to
                    // the params
                    userObj.put("email", etEmail.getText().toString());
                    userObj.put("password", etPassword.getText().toString());
                    holder.put("user", userObj);
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
                    json.put("info", "Email and/or password are invalid. Retry!");
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
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
                    editor.putString("UserID", json.getJSONObject("data").getString("user_id"));
                    editor.putString("Username", json.getJSONObject("data").getString("username"));
                    editor.putString("Email", etEmail.getText().toString().trim());
                    editor.commit();

                    // launch the HomeActivity and close this one
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(intent);
                    finish();
                }
                Toast.makeText(MainActivity.this, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }


    private class RegisterTask extends UrlJsonAsyncTask {
        public RegisterTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    // add the users's info to the post params
                    userObj.put("email", rEmail);
                    userObj.put("password", rPassword);
                    userObj.put("password_confirmation", rPassword2);
                    holder.put("user", userObj);
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
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
                    editor.putString("UserID", json.getJSONObject("data").getString("user_id"));
                    editor.putString("Username", rEmail);
                    editor.putString("Email", rEmail);
                    editor.commit();

//                    // launch the HomeActivity and close this one
//                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    startActivity(intent);
//                    finish();
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
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
