package poi.michael.pointsofinterest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LatLng mUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final View button = findViewById(R.id.new_poi);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // createFragment = new CreatePoiDialogFragment();
                //createFragment.show(getSupportFragmentManager(), "this is a test");
                Location location = getLastKnownLocation();

                Intent AddPoiActivityIntent = new Intent(MapActivity.this, addPoiActivity.class);
                AddPoiActivityIntent.putExtra("latitude", location.getLatitude());
                AddPoiActivityIntent.putExtra("longitude", location.getLongitude());
                startActivity(AddPoiActivityIntent);
            }
        });

        //mUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }


    private void StartLocationTracking()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(mLocationManager.isProviderEnabled(mLocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(mLocationManager.NETWORK_PROVIDER, 2, 2000, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    mUserLocation = new LatLng(latitude, longitude);
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
        else if(mLocationManager.isProviderEnabled(mLocationManager.GPS_PROVIDER))
        {
            mLocationManager.requestLocationUpdates(mLocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude;
                    double longitude;

                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng latLong = new LatLng(latitude, longitude);
                    }
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Location location = getLastKnownLocation();
        LatLng coordinate = null;

        if(location != null) {
            coordinate = new LatLng(location.getLatitude(), location.getLongitude());
        }

        //CameraUpdate cLocation = CameraUpdateFactory.newLatLngZoom(
         //       coordinate, 15);

        if(coordinate != null) {
            /*CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(coordinate)
                    .zoom(16)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 10000, null);*/
            googleMap.addMarker(new MarkerOptions().position(coordinate).title("test123"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15));
            //mapView.onResume();
        }
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, 11 );
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

}
