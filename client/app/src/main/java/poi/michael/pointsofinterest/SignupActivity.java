package poi.michael.pointsofinterest;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button mSignupButton = (Button) findViewById(R.id.sign_up_button);
        mSignupButton.setOnClickListener(mSignupButtonListener);
    }

    private View.OnClickListener mSignupButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            AutoCompleteTextView username = (AutoCompleteTextView) findViewById(R.id.UsernameSignupField);
            EditText password = (EditText) findViewById(R.id.PasswordSignupField);
            EditText first = (EditText) findViewById(R.id.FirstNameSignupField);
            EditText last = (EditText) findViewById(R.id.LastNameSignupField);
            EditText email = (EditText) findViewById(R.id.EmailSignupField);

            new UserSignupTask(email.getText().toString(), password.getText().toString(), username.getText().toString(),
                               first.getText().toString(), last.getText().toString()).execute();
            //mSignInButton.setText("TEST");
            // Instantiate the RequestQueue.

        }
    };

    public class UserSignupTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mFirst;
        private final String mLast;
        private final String mUsername;
        private Context mContext;

        UserSignupTask(String email, String password, String username, String first, String last) {
            mEmail = email;
            mPassword = password;
            mFirst = first;
            mLast = last;
            mUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url =  getResources().getString(R.string.base_url) + "/user";
            mContext = getApplicationContext();
            Boolean success = true;

            //Button mSignInButton = (Button) findViewById(R.id.sign_in_button);

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    JSONArray errorArray = JSONResponse.getJSONArray("message");
                                    String errorText = errorArray.getString(0);
                                    Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT).show();
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
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username", mUsername);
                    params.put("password", mPassword);
                    params.put("firstname", mFirst);
                    params.put("lastname", mLast);
                    params.put("email", mEmail);

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //finish();
        }

        @Override
        protected void onCancelled() {

        }
    }
}