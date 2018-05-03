package poi.michael.pointsofinterest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
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

    boolean register(String username, String password, String first_name, String last_name, String email) {
        String url = base_api_url + "users";

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("firstname", first_name)
                .add("lastname", last_name)
                .add("email", email)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(!response.isSuccessful()) {
                return false;
            }

            String json_string = response.body().string();

            JSONObject JSONResponse = new JSONObject(json_string);

            if (!JSONResponse.getBoolean("success")) {
                return false;
            }

            return true;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
