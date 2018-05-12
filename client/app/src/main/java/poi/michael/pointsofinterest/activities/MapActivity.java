package poi.michael.pointsofinterest.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.utils.MapTools;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Toolbar mToolbar;
    private CheckBox mCheckbox;
    private View mFloatingButton;
    private Location mLastKnownLocation;
    public static final int CREATE_POI_REQUEST = 2;
    private View.OnClickListener mSnackbarClickListener;
    private View.OnClickListener mSnackbarNoLocationListener;
    private MapTools.LocationHelper mLocationHelper;
    private static final String TAG = MapActivity.class.getName();

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

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

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
                } else if (mMap != null) {
                    mMap.clear();
                    new loadPoints(false).execute();
                }
            }
        });

        mSnackbarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new loadPoints(false).execute();
            }
        };

        mSnackbarNoLocationListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationHelper.getCurrentLocation();
            }
        };

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
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationHelper = new MapTools().getLocationHelper(this,
                new MapTools.LocationListener() {
                    @Override
                    public void onLocationRetrieved(Location location) {
                        mLastKnownLocation = location;

                        mFloatingButton.setVisibility(View.VISIBLE);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), 15));
                    }

                    @Override
                    public void onNullLocation() {
                        Snackbar.make(findViewById(android.R.id.content), "Error retrieving location", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", mSnackbarNoLocationListener)
                                .show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, e.toString());
                    }
        });

        mLocationHelper.getCurrentLocation();

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
        mMap.setMyLocationEnabled(true);
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

    private class loadPoints extends AsyncTask<Void, Void, ArrayList<POI>> {
        private boolean mOnlyFollowed;
        loadPoints(boolean onlyFollowed) {
            mOnlyFollowed = onlyFollowed;
        }

        @Override
        protected ArrayList<POI> doInBackground(Void... params) {
            ArrayList<POI> POIs;

            if (mOnlyFollowed)
                POIs = new APIRequests(getApplicationContext()).getPOIs(APIRequests.PoiChoices.FOLLOWING);
            else
                POIs = new APIRequests(getApplicationContext()).getPOIs(APIRequests.PoiChoices.ALL);

            return POIs;
        }

        @Override
        protected void onPostExecute(final ArrayList<POI> points) {

            //on successful retrieval of POIs
            if (points != null) {
                Marker marker;

                for(POI poi: points) {
                    marker = mMap.addMarker(new MarkerOptions().position(poi.getLocation()).title(poi.getName()));

                    marker.setTag(poi);
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        POI info = (POI) marker.getTag();
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