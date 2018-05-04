package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.utils.volleySingleton;

public class addPoiActivity extends Activity {

    private double mLatitude = 0;
    private double mLongitude = 0;
    private Button mAddPoiButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);
        mAddPoiButton = (Button) findViewById(R.id.AddPoiButton);

        Intent intentExtras = getIntent();
        mLatitude = intentExtras.getDoubleExtra("latitude", 0);
        mLongitude = intentExtras.getDoubleExtra("longitude", 0);

        mAddPoiButton.setOnClickListener(mAddPoiListener);
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
            Intent sendPointInfo = new Intent();
            sendPointInfo.putExtra("latitude", mLat);
            sendPointInfo.putExtra("longitude", mLong);
            sendPointInfo.putExtra("name", mName);
            sendPointInfo.putExtra("description", mDescription);
            setResult(Activity.RESULT_OK, sendPointInfo);
            finish();
        }

        @Override
        protected void onCancelled() {

        }
    }

}