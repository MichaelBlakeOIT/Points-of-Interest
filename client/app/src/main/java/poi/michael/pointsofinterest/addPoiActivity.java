package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class addPoiActivity extends AppCompatActivity {

    private double mLatitude = 0;
    private double mLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);

        Intent intentExtras = getIntent();
        mLatitude = intentExtras.getDoubleExtra("latitude", 0);
        mLongitude = intentExtras.getDoubleExtra("longitude", 0);

        final Button addPoiButton = (Button) findViewById(R.id.AddPoiButton);
        addPoiButton.setOnClickListener(mAddPoiListener);
    }

    private View.OnClickListener mAddPoiListener = new View.OnClickListener() {
        public void onClick(View v) {
            EditText PoiName = (EditText) findViewById(R.id.PoiName);
            EditText PoiDescription = (EditText) findViewById(R.id.PoiDescription);

            new AddPoiTask(mLatitude, mLongitude, PoiName.getText().toString(), PoiDescription.getText().toString()).execute();
        }
    };

    private class AddPoiTask extends AsyncTask<Void, Void, Boolean> {

        private final double mLat;
        private final double mLong;
        private final String mName;
        private final String mDescription;

        private Context mContext;

        AddPoiTask(double lat, double _long, String name, String description) {
            mLat = lat;
            mLong = _long;
            mName = name;
            mDescription = description;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String url = getResources().getString(R.string.base_url) + "/poi";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_SHORT).show();
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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("lat", Double.toString(mLat));
                    params.put("long", Double.toString(mLong));
                    params.put("title", mName);
                    params.put("description", mDescription);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);


                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));
                    params.put("Accept-Language", "fr");

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            MapActivity addPoint = new MapActivity();
            Intent sendPointInfo = new Intent();
            sendPointInfo.putExtra("latitude", mLat);
            sendPointInfo.putExtra("longitude", mLong);
            sendPointInfo.putExtra("name", mName);
            sendPointInfo.putExtra("description", mDescription);
            setResult(Activity.RESULT_OK, sendPointInfo);
            //addPoint.addMarker(new LatLng(mLat, mLong), mName);
            finish();
        }

        @Override
        protected void onCancelled() {

        }
    }

}