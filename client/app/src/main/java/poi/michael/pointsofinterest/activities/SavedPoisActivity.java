package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.List;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.adapters.MapRecyclerAdapter;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.utils.APIRequests;
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
        mAPIInterface = new APIRequests().getInterface();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mToken = "Bearer " + sharedPref.getString("token", "");

        loadSavedPOIs();
    }

    private void loadSavedPOIs() {
        mAPIInterface.getSavedPois(mToken).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<List<POI>>>() {
            @Override
            public void onResponse(@NonNull Call<poi.michael.pointsofinterest.models.Response<List<POI>>> call, @NonNull retrofit2.Response<poi.michael.pointsofinterest.models.Response<List<POI>>> response) {
                mRecyclerView = findViewById(R.id.recycler_view);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mRecyclerView.setAdapter(new MapRecyclerAdapter(response.body().getData(), getApplicationContext()));
            }

            @Override
            public void onFailure(@NonNull Call<poi.michael.pointsofinterest.models.Response<List<POI>>> call, @NonNull Throwable t) {

            }
        });
    }
}