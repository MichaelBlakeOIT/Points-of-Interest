package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

public class ForgotPasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private Context mContext;

        SendEmailTask(String username) {
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/users/reset";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (!JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("username", mUsername);
                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }
    }

    public void launchReset(View v) {
        EditText usernameText = (EditText) findViewById(R.id.resetpasswordusernamefield);
        String username = usernameText.getText().toString();

        if (!username.equals("")) {
            new SendEmailTask(usernameText.getText().toString()).execute();
            Intent launchResetIntent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
            launchResetIntent.putExtra("username", username);
            ForgotPasswordActivity.this.startActivity(launchResetIntent);
        }
    }
}
