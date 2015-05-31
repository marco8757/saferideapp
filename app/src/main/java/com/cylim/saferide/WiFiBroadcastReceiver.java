package com.cylim.saferide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by marco on 29/4/15.
 */
public class WiFiBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final boolean isWifiConn = networkInfo.isConnected();
        Log.d("WiFi connected", String.valueOf(isWifiConn));

        Intent i = new Intent(context, UploadService.class);
        context.startService(i);
    }
}
