package poi.michael.pointsofinterest;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by michael on 4/17/2018.
 * Class for implementing API requests.
 */

public class APIRequests {
    private final String base_api_url = "https://points-of-interest.herokuapp.com/";
    private OkHttpClient client = new OkHttpClient();

    String login(String username, String password) {
        String url = base_api_url + "session";

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(!response.isSuccessful()) {
                return null;
            }

            String json_string = response.body().string();

            JSONObject JSONResponse = new JSONObject(json_string);

            if (!JSONResponse.getBoolean("success")) {
                //invalid login (probably)
                return null;
            }

            return JSONResponse.getString("token");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
