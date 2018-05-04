package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.models.NamedLocation;
import poi.michael.pointsofinterest.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Toolbar mToolbar;
    private CheckBox mCheckbox;
    private View mFloatingButton;
    public static final int CREATE_POI_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mCheckbox = (CheckBox) findViewById(R.id.checkBox);
        mFloatingButton = findViewById(R.id.new_poi);

        setSupportActionBar(mToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location location = getLastKnownLocation();

                Intent AddPoiActivityIntent = new Intent(MapActivity.this, addPoiActivity.class);
                AddPoiActivityIntent.putExtra("latitude", location.getLatitude());
                AddPoiActivityIntent.putExtra("longitude", location.getLongitude());
                startActivityForResult(AddPoiActivityIntent, CREATE_POI_REQUEST);
            }
        });

        mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckbox.isChecked() && mMap != null) {
                    mMap.clear();
                    new loadPoints(true).execute();
                }
                else if (mMap != null) {
                    mMap.clear();
                    new loadPoints(false).execute();
                }
            }
        });

        new loadPoints(false).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_profile:
                viewProfile();
                return true;
            case R.id.action_settings:
                viewSettings();
                return true;
            case R.id.action_saved_pois:
                viewSavedPois();
                return true;
            case R.id.action_feed:
                viewFeed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CREATE_POI_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String name = data.getStringExtra("name");
                String description = data.getStringExtra("description");
                // do something with the result
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name));
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // some stuff that will happen if there's no result
            }
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

        if(coordinate != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 15));
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

    private class loadPoints extends AsyncTask<Void, Void, ArrayList<NamedLocation>> {
        private View.OnClickListener mSnackbarClickListener;
        private boolean mOnlyFollowed;
        loadPoints(boolean onlyFollowed) {
            mOnlyFollowed = onlyFollowed;
        }

        @Override
        protected ArrayList<NamedLocation> doInBackground(Void... params) {
            ArrayList<NamedLocation> POIs;

            if (mOnlyFollowed)
                POIs = new APIRequests(getApplicationContext()).getPOIs(true);
            else
                POIs = new APIRequests(getApplicationContext()).getPOIs(false);

            return POIs;
        }

        @Override
        protected void onPostExecute(final ArrayList<NamedLocation> points) {

            //on successful retrieval of POIs
            if (points != null) {
                Marker marker;

                mFloatingButton.setVisibility(View.VISIBLE);

                for(NamedLocation poi: points) {
                    marker = mMap.addMarker(new MarkerOptions().position(poi.getLocation()).title(poi.getName()));

                    marker.setTag(poi);
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        NamedLocation info = (NamedLocation) marker.getTag();
                        Intent POIActivityIntent = new Intent(MapActivity.this, POIActivity.class);

                        POIActivityIntent.putExtra("title", info.getName());
                        POIActivityIntent.putExtra("description", info.getDescription());
                        POIActivityIntent.putExtra("username", info.getUsername());
                        POIActivityIntent.putExtra("id", info.getPoiId());
                        POIActivityIntent.putExtra("rating", info.getRating());
                        POIActivityIntent.putExtra("Location", info.getLocation());

                        MapActivity.this.startActivity(POIActivityIntent);
                        return false;
                    }
                });
            }
            //error retrieving points
            else {
                mSnackbarClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new loadPoints(false).execute();
                    }
                };

                Snackbar.make(findViewById(android.R.id.content), "Error loading points", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", mSnackbarClickListener)
                        .show();
            }
        }
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_token), 0);
        preferences.edit().remove("token").apply();
        preferences.edit().remove("username").apply();

        Intent LoginActivityIntent = new Intent(MapActivity.this, LoginActivity.class);
        MapActivity.this.startActivity(LoginActivityIntent);

        MapActivity.this.finish();
    }

    private void viewProfile() {
        Intent ProfileActivityIntent = new Intent(MapActivity.this, ProfileActivity.class);
        MapActivity.this.startActivity(ProfileActivityIntent);
    }

    private void viewSettings() {
        Intent SettingsActivityIntent = new Intent(MapActivity.this, SettingsActivity.class);
        MapActivity.this.startActivity(SettingsActivityIntent);
    }

    private void viewSavedPois() {
        Intent SavedPOIsIntent = new Intent(MapActivity.this, SavedPoisActivity.class);
        MapActivity.this.startActivity(SavedPOIsIntent);
    }

    private void viewFeed() {
        Intent FeedIntent = new Intent(MapActivity.this, FeedActivity.class);
        MapActivity.this.startActivity(FeedIntent);
    }
}