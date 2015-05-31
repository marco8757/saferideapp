package com.cylim.saferide;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * Created by marco on 3/4/15.
 */
public class Splash extends Activity {

    SharedPreferences mPreferences;

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        Intent cache = new Intent(Splash.this, BackgroundCacheService.class);
        startService(cache);

        Thread timer = new Thread() {
            public void run() {
                try {
                    //pause for 1000 milisecond
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //check if user has logged in with authtoken
                    String token = mPreferences.getString("AuthToken", "");
                    Intent i = new Intent(Splash.this, MainActivity.class);

                    if (token.length() > 0) {
                        i = new Intent(Splash.this, MapActivity.class);
                    }

                    startActivity(i);
                }
            }
        };
        timer.start();

    }
}
