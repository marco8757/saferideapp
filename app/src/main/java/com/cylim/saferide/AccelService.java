package com.cylim.saferide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by marco on 21/4/15.
 */
public class AccelService extends Service {

    double lat, lng;
    GPSTagger gps;
    private SensorManager mSensorManager;
    private MovementDetector movementDetector;
    String lastTime = "";
    double lastLat = 0, lastLng = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        movementDetector = new MovementDetector();

        movementDetector.setOnShakeListener(new MovementDetector.OnShakeListener() {

            public void onShake() {
                gps = new GPSTagger(AccelService.this);

                if (gps.canGetLocation()) {

                    lat = gps.getLatitude();
                    lng = gps.getLongitude();

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    //check if previous report time, lat and lng is similar with the newly detected movement
                    if (!formattedDate.equals(lastTime) && !(lat == lastLat && lng == lastLng)) {
                        Log.d("GPSTagger Location", lat + " " + lng);
                        lastLat = lat;
                        lastLng = lng;
                        lastTime = formattedDate;
                        DatabaseHandler db = new DatabaseHandler(AccelService.this);
                        db.addReport(String.valueOf(lat), String.valueOf(lng), formattedDate);
                    } else {
                        Log.d("SimilarDefect", "true");
                    }
                } else {
                    gps.showSettingsAlert();
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(movementDetector);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mSensorManager.registerListener(movementDetector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }
}
