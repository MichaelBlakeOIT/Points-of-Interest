package poi.michael.pointsofinterest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class POIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        Intent intentExtras = getIntent();

        intentExtras.getExtras();
        String title = intentExtras.getStringExtra("title");
        String description = intentExtras.getStringExtra("description");
        final String username = intentExtras.getStringExtra("username");

        TextView title_view = (TextView) findViewById(R.id.poi_title);
        TextView description_view = (TextView) findViewById(R.id.poi_description);
        TextView username_view = (TextView) findViewById(R.id.poi_username);

        title_view.setText(title);
        description_view.setText(description);
        username_view.setText("Created by " + username);

        final View button = findViewById(R.id.poi_username);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent poiActivityIntent = new Intent(POIActivity.this, ProfileActivity.class);
                poiActivityIntent.putExtra("username", username);
                startActivity(poiActivityIntent);
            }
        });
    }
}
