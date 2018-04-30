package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class POIActivity extends Activity {

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
        final Float rating = intentExtras.getFloatExtra("rating", 0);
        mId = intentExtras.getIntExtra("id", 0);

        TextView title_view = (TextView) findViewById(R.id.poi_title);
        TextView description_view = (TextView) findViewById(R.id.poi_description);
        TextView username_view = (TextView) findViewById(R.id.poi_username);
        Button photos = (Button) findViewById(R.id.poi_view_photos);
        RatingBar ratingbar = (RatingBar) findViewById(R.id.ratingBar);
        Button comments = (Button) findViewById(R.id.poi_view_comments);

        title_view.setText(title);
        description_view.setText(description);
        username_view.setText("Created by " + username);
        ratingbar.setRating(rating);

        username_view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent poiActivityIntent = new Intent(POIActivity.this, ProfileActivity.class);
                poiActivityIntent.putExtra("username", username);
                startActivity(poiActivityIntent);
            }
        });

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                new RatePOITask((int) v).execute();
            }
        });

        photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photosIntent = new Intent(POIActivity.this, PhotosActivity.class);
                photosIntent.putExtra("poi_id", mId);
                POIActivity.this.startActivity(photosIntent);
            }
        });

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentsIntent = new Intent(POIActivity.this, CommentActivity.class);
                commentsIntent.putExtra("poi_id", mId);
                POIActivity.this.startActivity(commentsIntent);
            }
        });

        final Button saveButton = (Button) findViewById(R.id.save_poi);

        saveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SavePOITask().execute();
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
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success") != true) {
                                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
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

    private class SavePOITask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mId + "/save";

            mContext = getApplicationContext();

            StringRequest saveRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(saveRequest);
            return true;
        }
    }
}
