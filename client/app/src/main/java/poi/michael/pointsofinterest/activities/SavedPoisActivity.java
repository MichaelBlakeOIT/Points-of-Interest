package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.adapters.MapRecyclerAdapter;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.utils.volleySingleton;
import retrofit2.Call;
import retrofit2.Callback;

public class SavedPoisActivity extends Activity {
    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private APIInterface mAPIInterface;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_pois);

        mGridLayoutManager = new GridLayoutManager(this, 2);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mToken = "Bearer " + sharedPref.getString("token", "");

        loadSavedPOIs();
        //new GetSavedPOIsTask().execute();
    }

    /*private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

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
    };*/

    private void loadSavedPOIs() {
        mAPIInterface.getSavedPois(mToken).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<List<POI>>>() {
            @Override
            public void onResponse(@NonNull Call<poi.michael.pointsofinterest.models.Response<List<POI>>> call, @NonNull retrofit2.Response<poi.michael.pointsofinterest.models.Response<List<POI>>> response) {
                mRecyclerView = findViewById(R.id.recycler_view);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mRecyclerView.setAdapter(new MapRecyclerAdapter(response.body().getData(), getApplicationContext()));
                //mRecyclerView.setAdapter(new MapAdapter(response.body().getData()));
               // mRecyclerView.setRecyclerListener(mRecycleListener);
            }

            @Override
            public void onFailure(@NonNull Call<poi.michael.pointsofinterest.models.Response<List<POI>>> call, @NonNull Throwable t) {

            }
        });
    }

    /*private class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {

        private List<POI> pois;
        //private List<NamedLocation> namedLocations;

        private MapAdapter(List<POI> locations) {
            super();
            namedLocations = locations;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.poi_list_fragment, parent, false));
        }

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

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                mapView = layout.findViewById(R.id.lite_listrow_map);
                title = layout.findViewById(R.id.saved_poi_title);
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
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.location, 13f));
                map.addMarker(new MarkerOptions().position(data.location));

                // Set the map type back to normal.
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                mapView.onResume();
            }

            private void bindView(int pos) {
                NamedLocation item = namedLocations.get(pos);
                // Store a reference of the ViewHolder object in the layout.
                layout.setTag(this);
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView.setTag(item);
                setMapLocation();
                title.setText(item.name);
            }
        }
    }*/

    /*private List<NamedLocation> list_locations = new ArrayList<>();

    private class GetSavedPOIsTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private int mRating;

        @Override
        protected Boolean doInBackground(Void... params) {
            mContext = getApplicationContext();
            String url = getResources().getString(R.string.base_url) + "/users/pois/saved";

            StringRequest saveRequest = new StringRequest(Request.Method.GET, url,
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
                                    int userId = POI.getInt("creator_id");
                                    int poiId = POI.getInt("pio_id");
                                    String username = POI.getString("creator");

                                    list_locations.add(new NamedLocation(title, new LatLng(lat, _long)));
                                }

                                mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
                                mRecyclerView.setHasFixedSize(true);
                                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                                mRecyclerView.setAdapter(new MapAdapter(list_locations));
                                mRecyclerView.setRecyclerListener(mRecycleListener);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
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
            volleySingleton.getInstance(mContext).getRequestQueue().add(saveRequest);
            return true;
        }
    }*/
}