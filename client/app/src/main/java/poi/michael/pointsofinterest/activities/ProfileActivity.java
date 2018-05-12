package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.models.SuccessResponse;
import poi.michael.pointsofinterest.models.User;
import poi.michael.pointsofinterest.models.UserResponse;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.utils.ImageTools;
import poi.michael.pointsofinterest.R;
import retrofit2.Call;
import retrofit2.Callback;

public class ProfileActivity extends Activity {
    private String mProfileUsername;
    private String mLoggedInUsername;
    private String mBio;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private List<POI> list_locations = new ArrayList<>();
    private int mFollowing;
    private Button mFollowButton;
    private APIInterface mAPIInterface;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtras = getIntent();
        mProfileUsername = intentExtras.getStringExtra("username");

        if (mProfileUsername != null) {
            mProfileUsername = mProfileUsername.trim();
        }
        mLinearLayoutManager = new LinearLayoutManager(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mLoggedInUsername =  sharedPref.getString("username", "");
        mToken = "Bearer " + sharedPref.getString("token", "");

        setContentView(R.layout.activity_profile);

        mFollowButton = findViewById(R.id.profile_follow);

        //hide follow button if viewing own profile
        if (mProfileUsername == null || mLoggedInUsername.equals(mProfileUsername)) {
            mFollowButton.setVisibility(View.GONE);
        }

        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();

        loadProfileDetails();
    }

    private void loadProfileDetails() {
        mAPIInterface.getUser(mProfileUsername, mToken).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<UserResponse>>() {
            @Override
            public void onResponse(Call<poi.michael.pointsofinterest.models.Response<UserResponse>> call, retrofit2.Response<poi.michael.pointsofinterest.models.Response<UserResponse>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    User user = response.body().getData().getUser();

                    mBio = user.getBio();
                    mProfileUsername = user.getUsername();
                    mFollowing = user.getIsFollowing();

                    TextView username = findViewById(R.id.profile_username);
                    TextView bio = findViewById(R.id.profile_bio);
                    //new DownloadImageTask((ImageView) findViewById(R.id.profile_picture))
                    //        .execute("https://s3.us-east-2.amazonaws.com/points-of-interest/profile_photos/" + mProfileUsername.toLowerCase() + ".jpg");

                    username.setText(mProfileUsername);
                    bio.setText(mBio);

                    if(mFollowing == 1) {
                        mFollowButton.setText("Unfollow User");
                    }

                    list_locations = response.body().getData().getPois();

                    mRecyclerView = findViewById(R.id.profile_poi_feed);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                    mRecyclerView.setAdapter(new MapAdapter(list_locations));
                    mRecyclerView.setRecyclerListener(mRecycleListener);
                }
                else {
                    //something
                }
            }

            @Override
            public void onFailure(Call<poi.michael.pointsofinterest.models.Response<UserResponse>> call, Throwable t) {

            }
        });
    }

    private void followUser() {
        mAPIInterface.followUser(mProfileUsername, mToken).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<SuccessResponse>>() {
            @Override
            public void onResponse(Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, retrofit2.Response<poi.michael.pointsofinterest.models.Response<SuccessResponse>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    mFollowButton.setText(getResources().getString(R.string.unfollow));
                    mFollowing = 1;
                }
            }

            @Override
            public void onFailure(Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, Throwable t) {

            }
        });
    }

    private void unfollowUser() {
        mAPIInterface.unfollowUser(mProfileUsername, mToken).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<SuccessResponse>>() {
            @Override
            public void onResponse(Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, retrofit2.Response<poi.michael.pointsofinterest.models.Response<SuccessResponse>> response) {
                mFollowButton.setText(getResources().getString(R.string.follow));
                mFollowing = 0;
            }

            @Override
            public void onFailure(Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, Throwable t) {

            }
        });
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
            View layout;
            Button comments;

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                mapView = layout.findViewById(R.id.lite_listrow_map);
                title = layout.findViewById(R.id.saved_poi_title);
                comments = layout.findViewById(R.id.comments_button);
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

            private void bindView(int pos) {
                POI item = POIS.get(pos);
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

    public void followUser(View v) {

        if (mFollowing == 1) {
            unfollowUser();
        }
        else {
            followUser();
        }
    }
}
