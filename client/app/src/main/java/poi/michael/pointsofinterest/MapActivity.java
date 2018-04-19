package poi.michael.pointsofinterest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LatLng mUserLocation;
    public static final int CREATE_POI_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        final CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox);
        final View button = findViewById(R.id.new_poi);

        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Location location = getLastKnownLocation();

                Intent AddPoiActivityIntent = new Intent(MapActivity.this, addPoiActivity.class);
                AddPoiActivityIntent.putExtra("latitude", location.getLatitude());
                AddPoiActivityIntent.putExtra("longitude", location.getLongitude());
                startActivityForResult(AddPoiActivityIntent, CREATE_POI_REQUEST);
            }
        });

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkbox.isChecked() && mMap != null) {
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

        //mUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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

    /*private void StartLocationTracking()
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
    }*/

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

    private class loadPoints extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private boolean mOnlyFollowed;
        loadPoints(boolean onlyFollowed) {
            mOnlyFollowed = onlyFollowed;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url;
            if(mOnlyFollowed)
                url = getResources().getString(R.string.base_url) + "/users/following";
            else
                url = getResources().getString(R.string.base_url) + "/poi";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);

                                JSONArray POIs = JSONResponse.getJSONArray("data");

                                for(int i = 0; i < POIs.length(); i++) {
                                    JSONObject POI = POIs.getJSONObject(i);
                                    Double lat = POI.getDouble("lat");
                                    Double _long = POI.getDouble("long");
                                    String title = POI.getString("title");
                                    String description = POI.getString("description");
                                    int userId = POI.getInt("user_id");
                                    int poiId = POI.getInt("pio_id");
                                    String username = POI.getString("username");
                                    Float rating = BigDecimal.valueOf(POI.getDouble("rating")).floatValue();

                                    //MarkerInfo info = new MarkerInfo(lat, _long, title, description, userId, poiId, username, (float)5);
                                    MarkerInfo info = new MarkerInfo(lat, _long, title, description, userId, poiId, username, rating);

                                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, _long)).title(title));

                                    marker.setTag(info);
                                }

                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker) {
                                        MarkerInfo info = (MarkerInfo)marker.getTag();
                                        Intent POIActivityIntent = new Intent(MapActivity.this, POIActivity.class);

                                        POIActivityIntent.putExtra("title", info.getTitle());
                                        POIActivityIntent.putExtra("description", info.getDescription());
                                        POIActivityIntent.putExtra("username", info.getUsername());
                                        POIActivityIntent.putExtra("id", info.getPoiId());
                                        POIActivityIntent.putExtra("rating", info.getRating());

                                        MapActivity.this.startActivity(POIActivityIntent);
                                        return false;
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            //Log.d("Error.Response", error.toString());
                        }
                    }
            ) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

        @Override
        protected void onCancelled() {

        }

        class MarkerInfo {
            private double mLat;
            private double mLong;
            private String mTitle;
            private String mDescription;
            private int mUserId;
            private int mPoiId;
            private String mUsername;
            private Float mRating;

            MarkerInfo(double lat, double _long, String title, String description, int userId, int poiId, String username, Float rating) {
                mLat = lat;
                mLong = _long;
                mTitle = title;
                mDescription = description;
                mUserId = userId;
                mPoiId = poiId;
                mUsername = username;
                mRating = rating;
            }

            public LatLng getLocation() {
                return new LatLng(mLat, mLong);
            }

            public String getTitle() {
                return mTitle;
            }

            public String getDescription() {
                return mDescription;
            }

            public int getUserId() {
                return mUserId;
            }

            public int getPoiId() {
                return mPoiId;
            }

            public String getUsername() {
                return mUsername;
            }

            public Float getRating() { return mRating; }
        }
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_token), 0);
        preferences.edit().remove("token").commit();
        preferences.edit().remove("username").commit();

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
        Intent SavedPOIsIntent = new Intent(MapActivity.this, SavedPois.class);
        MapActivity.this.startActivity(SavedPOIsIntent);
    }

    private void viewFeed() {
        Intent FeedIntent = new Intent(MapActivity.this, FeedActivity.class);
        MapActivity.this.startActivity(FeedIntent);
    }
}