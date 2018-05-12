package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.SuccessResponse;
import poi.michael.pointsofinterest.utils.APIRequests;
import retrofit2.Call;
import retrofit2.Callback;

public class ForgotPasswordActivity extends Activity {

    private APIInterface mAPIInterface;
    private EditText mUsernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mUsernameText = findViewById(R.id.resetpasswordusernamefield);

        mAPIInterface = new APIRequests(getApplicationContext()).getInterface();
    }

    private void sendEmail() {
        if(mUsernameText.getText() != null) {
            mAPIInterface.sendResetEmail(mUsernameText.getText().toString()).enqueue(new Callback<poi.michael.pointsofinterest.models.Response<SuccessResponse>>() {
                @Override
                public void onResponse(@NonNull Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, @NonNull retrofit2.Response<poi.michael.pointsofinterest.models.Response<SuccessResponse>> response) {
                    if (response.isSuccessful() && response.body().isSuccess()) {
                        Intent launchResetIntent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        launchResetIntent.putExtra("username", mUsernameText.getText().toString());
                        ForgotPasswordActivity.this.startActivity(launchResetIntent);

                        Toast.makeText(getApplicationContext(), response.body().getData().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<poi.michael.pointsofinterest.models.Response<SuccessResponse>> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void launchReset(View v) {
        sendEmail();
    }
}
