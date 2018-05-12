package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.models.SuccessResponse;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.R;
import retrofit2.Call;
import retrofit2.Callback;

public class SignupActivity extends Activity {

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    private APIInterface mAPIInterface;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mUsernameView = findViewById(R.id.UsernameSignupField);
        mPasswordView = findViewById(R.id.PasswordSignupField);
        mFirstNameView = findViewById(R.id.FirstNameSignupField);
        mLastNameView = findViewById(R.id.LastNameSignupField);
        mEmailView = findViewById(R.id.EmailSignupField);
        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();
        mProgressDialog = new ProgressDialog(SignupActivity.this);

        Button mSignupButton = findViewById(R.id.sign_up_button);
        mSignupButton.setOnClickListener(mSignupButtonListener);
    }

    private View.OnClickListener mSignupButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();
            String firstName = mFirstNameView.getText().toString();
            String lastName = mLastNameView.getText().toString();
            String email = mEmailView.getText().toString();

            //TODO: check if all values are valid.
            signup(username, password, firstName, lastName, email);

        }
    };

    private void signup(String username, String password, String firstName, String lastName, String email) {
        mProgressDialog.setMessage("Registering…");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        mAPIInterface.register(username, password, firstName, lastName, email).enqueue(new Callback<Response<SuccessResponse>>() {
            @Override
            public void onResponse(@NonNull Call<Response<SuccessResponse>> call, @NonNull retrofit2.Response<Response<SuccessResponse>> response) {
                Response<SuccessResponse> body = response.body();

                if (body != null && response.isSuccessful() && body.isSuccess()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if (body != null && !body.isSuccess()){
                    Toast.makeText(getApplicationContext(), body.getData().getMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<SuccessResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}