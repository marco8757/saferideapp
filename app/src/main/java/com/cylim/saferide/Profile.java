package com.cylim.saferide;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by marco on 3/4/15.
 */
public class Profile extends Activity {
    Button bUpdate, bCancel;
    TextView tvName, tvEmail;
    EditText etName;
    ImageView ivProfilePicture;
    Boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        bUpdate = (Button) findViewById(R.id.bPUpdateProfile);
        bCancel = (Button) findViewById(R.id.bPCancel);
        etName = (EditText) findViewById(R.id.etPName);
        tvEmail = (TextView) findViewById(R.id.tvPEmail);
        tvName = (TextView) findViewById(R.id.tvPName);
        ivProfilePicture = (ImageView) findViewById(R.id.ivPProfilePicture);

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

    private void editModeOff(){
        editMode = false;
        bUpdate.setText("Update Profile");
        bCancel.setVisibility(View.GONE);
        etName.setVisibility(View.GONE);
        tvName.setVisibility(View.VISIBLE);
    }
}
