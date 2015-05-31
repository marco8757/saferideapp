package com.cylim.saferide;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by marco on 31/5/15.
 */
public class StopAccelService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent svc = new Intent(StopAccelService.this, AccelService.class);
        stopService(svc);
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.cancel(0);
        Log.d("AccelService", "Stopped");
        stopSelf();
    }
}
