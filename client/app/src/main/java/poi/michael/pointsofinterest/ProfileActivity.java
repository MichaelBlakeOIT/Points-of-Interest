package poi.michael.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Context mContext;
    private String mUsername;
    private String mBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtras = getIntent();
        String username = intentExtras.getStringExtra("username");

        new GetProfileDetailsTask(username).execute();
        setContentView(R.layout.activity_profile);
    }

    private class GetProfileDetailsTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        GetProfileDetailsTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            //Base URL. If username ends up NULL, this url will return your own profile info
            String url = getResources().getString(R.string.base_url) + "/user";

            //If you're viewing someone's profile other than your own
            if (mUsername != null && !mUsername.isEmpty()) {
                url += "/" + mUsername;
            }
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

                                    mBio = info.getString("bio");
                                    mUsername = info.getString("username");

                                    TextView username = (TextView) findViewById(R.id.profile_username);
                                    TextView bio = (TextView) findViewById(R.id.profile_bio);

                                    username.setText(mUsername);
                                    bio.setText(mBio);
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
}
