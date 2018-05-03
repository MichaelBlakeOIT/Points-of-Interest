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
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;

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
            mUsernameView = (AutoCompleteTextView) findViewById(R.id.UsernameLoginField);
            mPasswordView = (EditText) findViewById(R.id.passwordLoginField);

            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();

            new UserLoginTask().execute(username, password);
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

    private class UserLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog progDialog;
            progDialog = new ProgressDialog(LoginActivity.this);
            progDialog.setMessage("Logging in...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            String response = new APIRequests().login(username, password);

            return response;
        }

        @Override
        protected void onPostExecute(final String token) {
            if (token != null) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString("token", token);
                //editor.putString("username", mUsername); TODO: Add username somewewhere else
                editor.apply();

                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                Intent MapActivityIntent = new Intent(LoginActivity.this, MapActivity.class);
                LoginActivity.this.startActivity(MapActivityIntent);

                LoginActivity.this.finish();
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect login", Toast.LENGTH_SHORT).show();
            }
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

