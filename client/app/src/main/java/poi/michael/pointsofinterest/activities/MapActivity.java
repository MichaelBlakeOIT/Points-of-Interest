package poi.michael.pointsofinterest.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.utils.MapTools;
import retrofit2.Call;
import retrofit2.Callback;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private Toolbar mToolbar;
    private CheckBox mCheckbox;
    private View mFloatingButton;
    private Location mLastKnownLocation;
    public static final int CREATE_POI_REQUEST = 2;
    private View.OnClickListener mSnackbarClickListener;
    private View.OnClickListener mSnackbarNoLocationListener;
    private MapTools.LocationHelper mLocationHelper;
    private APIInterface mAPIInterface;
    private String mToken;
    private LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = MapActivity.class.getName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mToolbar = findViewById(R.id.my_toolbar);
        mCheckbox = findViewById(R.id.checkBox);
        mFloatingButton = findViewById(R.id.new_poi);

        setSupportActionBar(mToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAPIInterface = new APIRequests().getInterface();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mToken = "Bearer " + sharedPref.getString("token", "");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent AddPoiActivityIntent = new Intent(MapActivity.this, addPoiActivity.class);
                AddPoiActivityIntent.putExtra("latitude", mLastKnownLocation.getLatitude());
                AddPoiActivityIntent.putExtra("longitude", mLastKnownLocation.getLongitude());
                startActivityForResult(AddPoiActivityIntent, CREATE_POI_REQUEST);
            }
        });

        mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCheckbox.isChecked() && mMap != null) {
                    mMap.clear();
                    loadAllPoints();
                } else if (mMap != null) {
                    mMap.clear();
                    //new loadPoints(false).execute();
                }
            }
        });

        mSnackbarClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllPoints();
            }
        };

        mSnackbarNoLocationListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationHelper.getCurrentLocation();
            }
        };

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    mLastKnownLocation = locationResult.getLastLocation();
                    if (mFloatingButton.getVisibility() == View.INVISIBLE) {
                        mFloatingButton.setVisibility(View.VISIBLE);
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), 15));
                }
            }
        };

        loadAllPoints();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (mRequestingLocationUpdates) {
            startLocationUpdates();
        //}
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
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
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name));
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // some stuff that will happen if there's no result
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_FINE_LOCATION);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(findViewById(android.R.id.content), "Location Permissions Required", Snackbar.LENGTH_INDEFINITE)
                    .show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    void loadAllPoints() {
        mAPIInterface.getAllPois(mToken).enqueue(new Callback<Response<List<POI>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<List<POI>>> call, @NonNull retrofit2.Response<Response<List<POI>>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    Marker marker;

                    for (POI poi : response.body().getData()) {
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
                else {
                    Snackbar.make(findViewById(android.R.id.content), "Error loading points", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", mSnackbarClickListener)
                            .show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<List<POI>>> call, @NonNull Throwable t) {
                Snackbar.make(findViewById(android.R.id.content), "Error loading points", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", mSnackbarClickListener)
                        .show();
            }
        });
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}