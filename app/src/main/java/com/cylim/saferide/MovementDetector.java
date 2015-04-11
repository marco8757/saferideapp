package com.cylim.saferide;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by marco on 10/4/15.
 */
public class MovementDetector implements SensorEventListener {
    private SensorManager mSensorManager;

    //minimum changes in force, aka, sensitivity
    private static final int MIN_FORCE = 10;

    //change of direction to consider a shake
    private static final int MIN_DIRECTION_CHANGE = 2;

    private static final int MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE = 500;

    private static final int MAX_TOTAL_DURATION_OF_SHAKE = 400;

    //time of shake started
    private long FirstDirectionChangeTime = 0;

    //time of shake ended/ last shake
    private long LastDirectionChangeTime;

    private int DirectionChangeCount = 0;

    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    private OnShakeListener ShakeListener;

    public interface OnShakeListener {
        //activate when shake is detected
        void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        ShakeListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        // get data from sensor manager
        float x = se.values[SensorManager.DATA_X];
        float y = se.values[SensorManager.DATA_Y];
        float z = se.values[SensorManager.DATA_Z];

        // calculate movement
        float totalMovement = Math.abs(x + y + z - lastX - lastY - lastZ);

        if (totalMovement > MIN_FORCE) {

            // get current timestamp
            long now = System.currentTimeMillis();

            //store first movement time
            if (FirstDirectionChangeTime == 0) {
                FirstDirectionChangeTime = now;
                LastDirectionChangeTime = now;
            }

            //check the interval between last changed of direction/shake
            long lastChangeWasAgo = now - LastDirectionChangeTime;
            if (lastChangeWasAgo < MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE) {

                LastDirectionChangeTime = now;
                DirectionChangeCount++;

                //store last sensor data
                lastX = x;
                lastY = y;
                lastZ = z;

                //check movement counts
                if (DirectionChangeCount >= MIN_DIRECTION_CHANGE) {

                    //check total shake duration
                    long totalDuration = now - FirstDirectionChangeTime;
                    if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
                        ShakeListener.onShake();
                        resetShakeParameters();
                    }
                }

            } else {
                resetShakeParameters();
            }
        }
    }

    private void resetShakeParameters() {
        FirstDirectionChangeTime = 0;
        DirectionChangeCount = 0;
        LastDirectionChangeTime = 0;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
