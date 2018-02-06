package poi.michael.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private String mProfileUsername;
    private String mLoggedInUsername;
    private String mBio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentExtras = getIntent();
        mProfileUsername = intentExtras.getStringExtra("username");

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        mLoggedInUsername =  sharedPref.getString("username", "");

        setContentView(R.layout.activity_profile);

        //hide follow button if viewing own profile
        if (mProfileUsername == null || mLoggedInUsername.equals(mProfileUsername)) {
            Button followButton = (Button) findViewById(R.id.profile_follow);
            followButton.setVisibility(View.GONE);
        }

        final View button = findViewById(R.id.profile_follow);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new FollowUserTask().execute();
            }
        });

        new GetProfileDetailsTask().execute();

    }

    private class GetProfileDetailsTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            //Base URL. If username ends up NULL, this url will return your own profile info
            String url = getResources().getString(R.string.base_url) + "/user";

            //If you're viewing someone's profile other than your own
            if (mProfileUsername != null && !mProfileUsername.equals(mLoggedInUsername)) {
                url += "/" + mProfileUsername;
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
                                    mProfileUsername = info.getString("username");

                                    TextView username = (TextView) findViewById(R.id.profile_username);
                                    TextView bio = (TextView) findViewById(R.id.profile_bio);

                                    username.setText(mProfileUsername);
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

    private class FollowUserTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/user/follow";

            mContext = getApplicationContext();

            StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
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

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("username", mProfileUsername);

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
            String url = getResources().getString(R.string.base_url) + "/user/follow";

            mContext = getApplicationContext();

            StringRequest putRequest = new StringRequest(Request.Method.DELETE, url,
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

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("username", mProfileUsername);

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(putRequest);
            return true;
        }
    }
}
