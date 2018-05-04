package poi.michael.pointsofinterest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class SignupActivity extends Activity {

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.UsernameSignupField);
        mPasswordView = (EditText) findViewById(R.id.PasswordSignupField);
        mFirstNameView = (EditText) findViewById(R.id.FirstNameSignupField);
        mLastNameView = (EditText) findViewById(R.id.LastNameSignupField);
        mEmailView = (EditText) findViewById(R.id.EmailSignupField);

        Button mSignupButton = (Button) findViewById(R.id.sign_up_button);
        mSignupButton.setOnClickListener(mSignupButtonListener);
    }

    private View.OnClickListener mSignupButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();
            String first_name = mFirstNameView.getText().toString();
            String last_name = mLastNameView.getText().toString();
            String email = mEmailView.getText().toString();

            //TODO: check if all values are valid.
            new UserSignupTask().execute(username, password, first_name, last_name, email);

        }
    };

    public class UserSignupTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog progDialog = new ProgressDialog(SignupActivity.this);
            progDialog.setMessage("Registering…");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String first_name = params[2];
            String last_name = params[3];
            String email = params[4];

            boolean success = new APIRequests(getApplicationContext()).register(username, password, first_name, last_name, email);

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }
}