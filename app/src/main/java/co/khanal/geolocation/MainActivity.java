package co.khanal.geolocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener,
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    TextView data;

    private String provider;
    LocationManager locationManager;
    Location location;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        data = (TextView) findViewById(R.id.data);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert (fab != null);
        fab.setOnClickListener(this);

        SupportMapFragment fragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_frag);
        fragment.getMapAsync(this);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int coarseLocationCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if(coarseLocationCheck == PackageManager.PERMISSION_GRANTED){
            provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        int coarseLocationCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        if(coarseLocationCheck == PackageManager.PERMISSION_GRANTED){
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateData(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO: Handle the the status changed. Not necesary at the momment.
    }

    @Override
    public void onProviderEnabled(String provider) {
        Snackbar.make(findViewById(R.id.data), "Provider enabled " + provider, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Snackbar.make(findViewById(R.id.data), "Provider disabled " + provider, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            // handle the fab click. Loading the location if the permission has already been granted or requesting it if needed
            case R.id.fab:
                boolean locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if(locationPermission){
                    loadLocation();
                    LatLng co = new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions().position(co).title("Current Location"));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(co, 14));

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PackageManager.PERMISSION_GRANTED){

            // This means that the permission has been granted to access coarse location
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadLocation();
            }
        }

    }


    // Permission is good, so now we can get the location
    public void loadLocation(){
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        try {
            location = locationManager.getLastKnownLocation(provider);
            updateData(location);
        } catch (SecurityException e){
            e.printStackTrace();
        }

    }


    // Update the textview based on the location
    public void updateData(Location location){
        this.location = location;
        String result = String.format(Locale.US, "Longitude: %5.2f, Lattitude: %5.2f", location.getLongitude(), location.getLatitude());
        data.setText(result);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if(location == null){
            double mLat = 36.7598717;
            double mLong = -76.5907847;
            LatLng co = new LatLng(mLat, mLong);

            map.addMarker(new MarkerOptions().position(co).title("default location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(co, 10));
        }
//        else {
//
//            Log.d("data", location.toString());
//
//            LatLng co = new LatLng(location.getLongitude(), location.getLatitude());
//            map.addMarker(new MarkerOptions().position(co).title("HOME!"));
//            map.moveCamera(CameraUpdateFactory.newLatLng(co));
//        }
    }

    @Override
    public void onMapLoaded() {
        Log.d("map", map.getCameraPosition().toString());
    }
}
