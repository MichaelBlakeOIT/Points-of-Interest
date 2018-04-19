package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private Context mContext;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String url = getResources().getString(R.string.base_url) + "/session";
            mContext = getApplicationContext();

            //Button mSignInButton = (Button) findViewById(R.id.sign_in_button);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
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
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("username", mUsername);
                    params.put("password", mPassword);

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            //showProgress(false);

            if (success) {
                //finish();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //showProgress(false);
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

