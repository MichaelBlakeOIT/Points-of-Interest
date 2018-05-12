package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.SuccessResponse;
import poi.michael.pointsofinterest.utils.APIRequests;
import retrofit2.Call;
import retrofit2.Callback;

public class addPoiActivity extends Activity {

    private double mLatitude = 0;
    private double mLongitude = 0;
    private Button mAddPoiButton;
    private EditText mPoiName;
    private EditText mPoiDescription;
    private APIInterface mAPIInterface;
    private String mToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        mAddPoiButton = findViewById(R.id.AddPoiButton);
        mPoiName = findViewById(R.id.PoiName);
        mPoiDescription = findViewById(R.id.PoiDescription);

        Intent intentExtras = getIntent();
        mLatitude = intentExtras.getDoubleExtra("latitude", 0);
        mLongitude = intentExtras.getDoubleExtra("longitude", 0);
        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mToken = "Bearer " + sharedPref.getString("token", "");

        mAddPoiButton.setOnClickListener(mAddPoiListener);
    }

    private View.OnClickListener mAddPoiListener = new View.OnClickListener() {
        public void onClick(View v) {
            addPoi();
        }
    };

    private void addPoi() {

        mAPIInterface.addPoi(mLatitude, mLongitude, mPoiName.getText().toString(), mPoiDescription.getText().toString(), mToken).enqueue(
                new Callback<poi.michael.pointsofinterest.models.Response<SuccessResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, @NonNull retrofit2.Response<poi.michael.pointsofinterest.models.Response<SuccessResponse>> response) {
                        if (response.isSuccessful() && response.body().isSuccess()) {
                            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                            Intent sendPointInfo = new Intent();
                            sendPointInfo.putExtra("latitude", mLatitude);
                            sendPointInfo.putExtra("longitude", mLongitude);
                            sendPointInfo.putExtra("name", mPoiName.getText().toString());
                            sendPointInfo.putExtra("description", mPoiDescription.getText().toString());
                            setResult(Activity.RESULT_OK, sendPointInfo);
                            finish();
                        }
                        else {
                            //something
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, @NonNull Throwable t) {

                    }
                });
    }
}