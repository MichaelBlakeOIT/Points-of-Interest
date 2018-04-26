package poi.michael.pointsofinterest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        TextView RegisterTextView = (TextView) findViewById(R.id.registerSelect);

        mSignInButton.setOnClickListener(mSignInButtonListener);
        RegisterTextView.setOnClickListener(mRegisterSelectListener);

        checkToken();
    }

    private View.OnClickListener mSignInButtonListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            AutoCompleteTextView username = (AutoCompleteTextView) findViewById(R.id.UsernameLoginField);
            EditText password = (EditText) findViewById(R.id.passwordLoginField);

            new UserLoginTask(username.getText().toString(), password.getText().toString()).execute();
        }
    };

    private View.OnClickListener mRegisterSelectListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View v) {
            Intent RegisterActivityIntent = new Intent(LoginActivity.this, SignupActivity.class);
            LoginActivity.this.startActivity(RegisterActivityIntent);
        }
    };

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        ProgressDialog progDialog;
        private Context mContext;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(LoginActivity.this);
            progDialog.setMessage("Logging in...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/session";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            progDialog.dismiss();

                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();

                                    editor.putString("token", JSONResponse.getString("token"));
                                    editor.putString("username", mUsername);
                                    editor.apply();

                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                                    Intent MapActivityIntent = new Intent(LoginActivity.this, MapActivity.class);
                                    LoginActivity.this.startActivity(MapActivityIntent);

                                    LoginActivity.this.finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Incorrect login", Toast.LENGTH_SHORT).show();
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
                            Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("username", mUsername);
                    params.put("password", mPassword);

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);

            return true;
        }
    }

    private void checkToken()
    {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.user_token), MODE_PRIVATE);
        if (sharedPrefs.contains("token"))
        {
            Intent MapActivityIntent = new Intent(LoginActivity.this, MapActivity.class);
            LoginActivity.this.startActivity(MapActivityIntent);
            LoginActivity.this.finish();
        }
    }

    public void launchForgot(View v) {
        Intent forgotIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        LoginActivity.this.startActivity(forgotIntent);
    }
}

