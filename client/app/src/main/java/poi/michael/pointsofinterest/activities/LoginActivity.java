package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.R;
import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends Activity {
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private Button mSignInButton;
    private TextView mRegisterTextView;
    private APIInterface mAPIInterface;
    private ProgressDialog mProgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInButton = findViewById(R.id.sign_in_button);
        mRegisterTextView = findViewById(R.id.registerSelect);
        mUsernameView = findViewById(R.id.UsernameLoginField);
        mPasswordView = findViewById(R.id.passwordLoginField);

        mSignInButton.setOnClickListener(mSignInButtonListener);
        mRegisterTextView.setOnClickListener(mRegisterSelectListener);

        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();
        mProgDialog = new ProgressDialog(LoginActivity.this);

        checkToken();
    }

    private View.OnClickListener mSignInButtonListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();


            mProgDialog.setMessage("Logging in…");
            mProgDialog.setIndeterminate(false);
            mProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgDialog.setCancelable(true);
            mProgDialog.show();

            login(username, password);
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

    private void login(String username, String password) {
        mAPIInterface.login(username, password).enqueue(new Callback<Response<String>>() {
            @Override
            public void onResponse(Call<Response<String>> call, retrofit2.Response<Response<String>> response) {
                if (response.isSuccessful() && response.body().isSuccess()) {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    String token = response.body().getData();

                    editor.putString("token", token);
                    editor.putString("username", mUsernameView.getText().toString());
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    mProgDialog.dismiss();

                    Intent MapActivityIntent = new Intent(LoginActivity.this, MapActivity.class);
                    LoginActivity.this.startActivity(MapActivityIntent);

                    LoginActivity.this.finish();
                }
                else if (response.isSuccessful() && !response.body().isSuccess()) {
                    Toast.makeText(getApplicationContext(), response.body().getData(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<String>> call, Throwable t) {

            }
        });
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

