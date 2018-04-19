package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends Activity {
    private String mProfileUsername;
    private String mLoggedInUsername;
    private String mBio;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private List<NamedLocation> list_locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtras = getIntent();
        mProfileUsername = intentExtras.getStringExtra("username");
        if (mProfileUsername != null) {
            mProfileUsername = mProfileUsername.replaceAll("\\s+","");
        }
        mLinearLayoutManager = new LinearLayoutManager(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mLoggedInUsername =  sharedPref.getString("username", "");

        setContentView(R.layout.activity_profile);

        //hide follow button if viewing own profile
        if (mProfileUsername == null || mLoggedInUsername.equals(mProfileUsername)) {
            Button followButton = (Button) findViewById(R.id.profile_follow);
            followButton.setVisibility(View.GONE);
        }

        /*Button button = (Button) findViewById(R.id.profile_follow);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new FollowUserTask().execute();
            }
        });*/

        new GetProfileDetailsTask().execute();
    }

    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            MapAdapter.ViewHolder mapHolder = (MapAdapter.ViewHolder) holder;
            if (mapHolder != null && mapHolder.map != null) {
                // Clear the map and free up resources by changing the map type to none.
                // Also reset the map when it gets reattached to layout, so the previous map would
                // not be displayed.
                mapHolder.map.clear();
                mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        }
    };

    private class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {

        private List<NamedLocation> namedLocations;

        private MapAdapter(List<NamedLocation> locations) {
            super();
            namedLocations = locations;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.poi_list_fragment, parent, false));
        }

        /**
         * This function is called when the user scrolls through the screen and a new item needs
         * to be shown. So we will need to bind the holder with the details of the next item.
         */
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder == null) {
                return;
            }
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return namedLocations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

            MapView mapView;
            TextView title;
            GoogleMap map;
            View layout;
            Button comments;

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                mapView = (MapView) layout.findViewById(R.id.lite_listrow_map);
                title = (TextView) layout.findViewById(R.id.saved_poi_title);
                comments = (Button) layout.findViewById(R.id.comments_button);
                if (mapView != null) {
                    // Initialise the MapView
                    mapView.onCreate(null);
                    // Set the map ready callback to receive the GoogleMap object
                    mapView.getMapAsync(this);
                }
            }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getApplicationContext());
                map = googleMap;
                setMapLocation();
            }

            private void setMapLocation() {
                if (map == null) return;

                NamedLocation data = (NamedLocation) mapView.getTag();
                if (data == null) return;

                // Add a marker for this item and set the camera
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.getLocation(), 13f));
                map.addMarker(new MarkerOptions().position(data.getLocation()));

                // Set the map type back to normal.
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            private void bindView(int pos) {
                NamedLocation item = namedLocations.get(pos);
                // Store a reference of the ViewHolder object in the layout.
                layout.setTag(this);
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView.setTag(item);
                setMapLocation();
                title.setText(item.getName());
            }
        }
    }

    private class GetProfileDetailsTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/users/user/";

            //viewing another profile or your own
            url = (mProfileUsername != null) ? (url += mProfileUsername) : (url += mLoggedInUsername);

            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    JSONObject info = JSONResponse.getJSONObject("message");

                                    JSONObject user = info.getJSONObject("user");
                                    JSONArray POIs = info.getJSONArray("pois");

                                    mBio = user.getString("bio");
                                    mProfileUsername = user.getString("username");

                                    TextView username = (TextView) findViewById(R.id.profile_username);
                                    TextView bio = (TextView) findViewById(R.id.profile_bio);
                                    new DownloadImageTask((ImageView) findViewById(R.id.profile_picture))
                                            .execute("https://s3.us-east-2.amazonaws.com/points-of-interest/profile_photos/" + mProfileUsername.toLowerCase() + ".jpg");

                                    username.setText(mProfileUsername);
                                    bio.setText(mBio);

                                    for(int i = 0; i < POIs.length(); i++) {
                                        JSONObject POI = POIs.getJSONObject(i);
                                        Double lat = POI.getDouble("lat");
                                        Double _long = POI.getDouble("long");
                                        String title = POI.getString("title");
                                        String description = POI.getString("description");
                                        int poiId = POI.getInt("pio_id");
                                        list_locations.add(new NamedLocation(title, new LatLng(lat, _long), poiId));
                                    }

                                    mRecyclerView = (RecyclerView) findViewById(R.id.profile_poi_feed);
                                    mRecyclerView.setHasFixedSize(true);
                                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                                    mRecyclerView.setAdapter(new MapAdapter(list_locations));
                                    mRecyclerView.setRecyclerListener(mRecycleListener);

                                } else {
                                    //I don't know what to do...
                                }
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
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            Bitmap resized = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                resized = ImageTools.cropToSquare(mIcon11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return resized;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }


    }

    private class FollowUserTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/users/user/" + mProfileUsername + "/follow";
            Log.e("url", url);

            mContext = getApplicationContext();

            StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Button followButton = (Button) findViewById(R.id.profile_follow);
                                    followButton.setText("Unfollow User");

                                    final View button = findViewById(R.id.profile_follow);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            new UnfollowUserTask().execute();
                                        }
                                    });
                                }
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
            volleySingleton.getInstance(mContext).getRequestQueue().add(putRequest);
            return true;
        }
    }

    private class UnfollowUserTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/users/user/" + mProfileUsername + "/follow";

            mContext = getApplicationContext();

            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Button followButton = (Button) findViewById(R.id.profile_follow);
                                    followButton.setText("Follow User");

                                    final View button = findViewById(R.id.profile_follow);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            new FollowUserTask().execute();
                                        }
                                    });
                                }
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
            volleySingleton.getInstance(mContext).getRequestQueue().add(deleteRequest);
            return true;
        }
    }

    public void followUser(View v) {
        new FollowUserTask().execute();
    }
}
