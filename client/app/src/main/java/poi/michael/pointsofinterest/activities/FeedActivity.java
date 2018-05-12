package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.utils.APIRequests;
import retrofit2.Call;
import retrofit2.Callback;

public class FeedActivity extends Activity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private APIInterface mAPIInterface;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mToken = "Bearer " + sharedPref.getString("token", "");

        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();
        mLinearLayoutManager = new LinearLayoutManager(this);

        loadFollowingPOIs();
    }

    private void loadFollowingPOIs() {
        mAPIInterface.getFollowingPois(mToken).enqueue(new Callback<Response<List<POI>>>() {
            @Override
            public void onResponse(Call<Response<List<POI>>> call, retrofit2.Response<Response<List<POI>>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    mRecyclerView = findViewById(R.id.following_feed);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                    mRecyclerView.setAdapter(new MapAdapter(response.body().getData()));
                    mRecyclerView.setRecyclerListener(mRecycleListener);
                }
                else {
                    //do something
                }
            }

            @Override
            public void onFailure(Call<Response<List<POI>>> call, Throwable t) {

            }
        });
    }

    private class MapAdapter extends RecyclerView.Adapter<MapAdapter.ViewHolder> {

        private List<POI> POIS;

        private MapAdapter(List<POI> locations) {
            super();
            POIS = locations;
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
            return POIS.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

            MapView mapView;
            TextView title;
            GoogleMap map;
            Button comments;
            Button photos;
            View layout;

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                mapView = layout.findViewById(R.id.lite_listrow_map);
                title = layout.findViewById(R.id.saved_poi_title);
                comments = layout.findViewById(R.id.comments_button);
                photos = layout.findViewById(R.id.photos_button);
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

                POI data = (POI) mapView.getTag();
                if (data == null) return;

                // Add a marker for this item and set the camera
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.getLocation(), 13f));
                map.addMarker(new MarkerOptions().position(data.getLocation()));

                // Set the map type back to normal.
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                mapView.onResume();
            }

            private void bindView(final int pos) {
                POI item = POIS.get(pos);
                // Store a reference of the ViewHolder object in the layout.
                layout.setTag(this);
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView.setTag(item);
                setMapLocation();
                title.setText(item.getName());
                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent commentsIntent = new Intent(FeedActivity.this, CommentActivity.class);
                        commentsIntent.putExtra("poi_id", POIS.get(pos).getPoiId());
                        FeedActivity.this.startActivity(commentsIntent);
                    }
                });
                photos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent photosIntent = new Intent(FeedActivity.this, PhotosActivity.class);
                        photosIntent.putExtra("poi_id", POIS.get(pos).getPoiId());
                        FeedActivity.this.startActivity(photosIntent);
                    }
                });
            }
        }
    }

    /*private class GetSavedPOIsTask extends AsyncTask<Void, Void, List<POI>> {
        @Override
        protected List<POI> doInBackground(Void... params) {
            ArrayList<POI> POIs;

            POIs = new APIRequests(getApplicationContext()).getPOIs(APIRequests.PoiChoices.FOLLOWING);

            return POIs;
        }

        @Override
        protected void onPostExecute(final List<POI> points) {

            //on successful retrieval of POIs
            if (points != null) {
                //populate list
                mRecyclerView = (RecyclerView) findViewById(R.id.following_feed);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mRecyclerView.setAdapter(new MapAdapter(points));
                mRecyclerView.setRecyclerListener(mRecycleListener);
            }
        }
    }*/

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
}
