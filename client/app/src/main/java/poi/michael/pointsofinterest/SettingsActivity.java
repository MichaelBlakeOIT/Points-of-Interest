package poi.michael.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveButton = (Button) findViewById(R.id.save_settings_button);
        saveButton.setOnClickListener(mSaveSettingsListener);
    }

    private View.OnClickListener mSaveSettingsListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            EditText password = (EditText) findViewById(R.id.change_password_text);
            EditText repeat = (EditText) findViewById(R.id.change_password_repeat_text);
            EditText bio = (EditText) findViewById(R.id.bio_text);
            if(password.getText().toString().equals(repeat.getText().toString())) {
                new ChangeSettingsTask(bio.getText().toString(), password.getText().toString()).execute();
            }
            else {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class ChangeSettingsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mBio;
        private final String mPassword;
        private Context mContext;

        ChangeSettingsTask(String bio, String password) {
            mBio = bio;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String url = getResources().getString(R.string.base_url) + "/user";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    SettingsActivity.this.finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener()
                    {
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
                    params.put("bio", mBio);
                    if(!mPassword.equals("")) {
                        params.put("password", mPassword);
                    }
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
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }
    }
}
