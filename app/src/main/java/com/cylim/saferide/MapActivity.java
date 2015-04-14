package com.cylim.saferide;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by marco on 12/4/15.
 */
public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapM);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        double lat = 0, lng = 0;
        GPSTagger gpsTagger = new GPSTagger(MapActivity.this);

        if (gpsTagger.canGetLocation()) {

            lat = gpsTagger.getLatitude();
            lng = gpsTagger.getLongitude();

            Log.d("GPSTagger Location", lat + " " + lng);

        } else {
            gpsTagger.showSettingsAlert();
        }

        LatLng mylocation = new LatLng(lat, lng);

        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 13));

        googleMap.addMarker(new MarkerOptions()
                .title("It's me here.")
                .snippet("This is where I am now.")
                .position(mylocation));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_report:
                break;
            case R.id.action_profile:
                break;
            case R.id.action_logout:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
