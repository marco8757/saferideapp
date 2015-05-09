package com.cylim.saferide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by marco on 3/4/15.
 */
public class Profile extends Activity {
    Button bUpdate, bCancel;
    TextView tvName, tvEmail;
    EditText etName;
    ImageView ivProfilePicture;
    Boolean editMode = false;
    private SharedPreferences mPreferences;

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

        emailHash = md5Hash(mPreferences.getString("Email", "").trim());

        if (!emailHash.contains("null")){
            Picasso.with(Profile.this).load("http://www.gravatar.com/avatar/" + emailHash + "?s=200").into(ivProfilePicture);
        }



        bUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editMode == false) {
                    editMode = true;
                    bUpdate.setText("Save");
                    bCancel.setVisibility(View.VISIBLE);
                    etName.setVisibility(View.VISIBLE);
                    tvName.setVisibility(View.GONE);
                } else {
                    editModeOff();
                }
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editModeOff();
            }
        });

    }

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

    private void editModeOff() {
        editMode = false;
        bUpdate.setText("Update Profile");
        bCancel.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
    }
}
