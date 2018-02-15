package poi.michael.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
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

public class POIActivity extends AppCompatActivity {

    int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        Intent intentExtras = getIntent();

        intentExtras.getExtras();
        final String title = intentExtras.getStringExtra("title");
        final String description = intentExtras.getStringExtra("description");
        final String username = intentExtras.getStringExtra("username");
        mId = intentExtras.getIntExtra("id", 0);

        TextView title_view = (TextView) findViewById(R.id.poi_title);
        TextView description_view = (TextView) findViewById(R.id.poi_description);
        TextView username_view = (TextView) findViewById(R.id.poi_username);

        title_view.setText(title);
        description_view.setText(description);
        username_view.setText("Created by " + username);

        final View button = findViewById(R.id.poi_username);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent poiActivityIntent = new Intent(POIActivity.this, ProfileActivity.class);
                poiActivityIntent.putExtra("username", username);
                startActivity(poiActivityIntent);
            }
        });

        final RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingBar);

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                new RatePOITask((int)v).execute();
            }
        });
    }

    private class RatePOITask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private int mRating;

        RatePOITask(int rating) {
            mRating = rating;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mId + "/rating";

            mContext = getApplicationContext();

            StringRequest ratingRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
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
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("rating", Integer.toString(mRating));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(ratingRequest);
            return true;
        }
    }
}
