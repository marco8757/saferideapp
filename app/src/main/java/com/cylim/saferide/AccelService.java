package com.cylim.saferide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by marco on 21/4/15.
 */
public class AccelService extends Service {


    private SensorManager mSensorManager;
    private MovementDetector movementDetector;

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
                Log.d("SHAKED", "true");
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
