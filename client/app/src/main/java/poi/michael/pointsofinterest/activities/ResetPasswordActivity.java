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

public class ResetPasswordActivity extends Activity {

    String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Intent getExtras = getIntent();
        mUsername = getExtras.getStringExtra("username");
    }

    private class ResetPasswordTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mCode;
        private Context mContext;

        ResetPasswordTask(String username, String password, String code) {
            mUsername = username;
            mPassword = password;
            mCode = code;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/users/reset";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
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
                                else {
                                    Toast.makeText(getApplicationContext(), "Password reset", Toast.LENGTH_SHORT);
                                    Intent homescreen = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    ResetPasswordActivity.this.startActivity(homescreen);
                                    ResetPasswordActivity.this.finish();
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
                    params.put("code", mCode);
                    params.put("password", mPassword);
                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            // TODO: register the new account here.
            return true;
        }
    }

    public void resetPassword(View v) {
        EditText codeText = (EditText) findViewById(R.id.code);
        EditText passwordText = (EditText) findViewById(R.id.resetPasswordField);
        EditText passwordText2 = (EditText) findViewById(R.id.repeatPasswordReset);

        String code = codeText.getText().toString();
        String password = passwordText.getText().toString();
        String password2 = passwordText2.getText().toString();

        if (code.equals("") || password.equals("") || !password.equals(password2)) {
            Toast.makeText(getApplicationContext(), "Invalid form", Toast.LENGTH_SHORT);
        }
        else {
            new ResetPasswordTask(mUsername, password, code).execute();
            Log.e("Here", mUsername + password + code);
        }
    }
}
