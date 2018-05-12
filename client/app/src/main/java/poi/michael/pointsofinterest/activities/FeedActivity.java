package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import poi.michael.pointsofinterest.adapters.MapRecyclerAdapter;
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
                    mRecyclerView.setAdapter(new MapRecyclerAdapter(response.body().getData(), getApplicationContext()));
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
}
