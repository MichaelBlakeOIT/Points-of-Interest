package poi.michael.pointsofinterest.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.Comment;
import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.models.POI;

/**
 * Created by michael on 4/17/2018.
 * Class for implementing API requests.
 */

public class APIRequests {
    private final String base_api_url = "https://points-of-interest.herokuapp.com/";
    //private final String base_api_url = "https://6270b593-a1b0-4ef8-87dd-11877a1d8397.mock.pstmn.io";
    private Context mContext;
    private OkHttpClient client = new OkHttpClient();

    public enum PoiChoices {
            ALL, FOLLOWING, SAVED
    }

    public APIRequests(Context context) {
        mContext = context;
    }

    /**
     * Created by michael on 4/17/2018.
     * API request for logging into an account.
     *
     * Returns auth token
     */

    public String login(String username, String password) {
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
                //invalid login (probably) TODO: handle other errors
                return null;
            }

            return JSONResponse.getString("token");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Created by michael on 4/17/2018.
     * API request for registering a new account.
     *
     * Retuns true on successful sign up, false otherwise.
     */

    public boolean register(String username, String password, String first_name, String last_name, String email) {
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

            return JSONResponse.getBoolean("success");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Created by michael on 4/17/2018.
     * Get list of POIs
     *
     * returns ArrayList of NamedLocations
     */

    public ArrayList<POI> getPOIs(PoiChoices poiType) {
        String url = base_api_url;

        switch (poiType) {
            case ALL:
                url += "poi";
                break;
            case FOLLOWING:
                url += "users/pois/following";
                break;
            case SAVED:
                url += "users/pois/saved";
                break;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", getBearerToken())
                .build();
        try {
            Response response = client.newCall(request).execute();
            ArrayList<POI> locations = new ArrayList<>();

            if(!response.isSuccessful()) {
                return null;
            }

            String json_string = response.body().string();

            JSONObject JSONResponse = new JSONObject(json_string);

            if (!JSONResponse.getBoolean("success")) {
                return null;
            }

            JSONArray POIs = JSONResponse.getJSONArray("data");

            for(int i = 0; i < POIs.length(); i++) {
                JSONObject POI = POIs.getJSONObject(i);
                Double lat = POI.getDouble("lat");
                Double _long = POI.getDouble("long");
                String title = POI.getString("title");
                String description = POI.getString("description");
                int userId = POI.getInt("user_id");
                int poiId = POI.getInt("pio_id");
                String username = POI.getString("username");
                Float rating = BigDecimal.valueOf(POI.getDouble("rating")).floatValue();

                poi.michael.pointsofinterest.models.POI poi = new POI(title, lat, _long, poiId, userId, rating, description, username);

                locations.add(poi);
            }

            return locations;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean makeComment(int poiId, String commentText) {
        String url = base_api_url + "poi/" + poiId + "/comments";

        RequestBody formBody = new FormBody.Builder()
                .add("comment", commentText)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", getBearerToken())
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if(!response.isSuccessful()) {
                return false;
            }

            String json_string = response.body().string();

            JSONObject JSONResponse = new JSONObject(json_string);

            return JSONResponse.getBoolean("success");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> getComments(int poiId) {
        String url = base_api_url + "poi/" + poiId + "/comments";
        List<Comment> list_comments = new ArrayList<>();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", getBearerToken())
                .build();
        try {
            Response response = client.newCall(request).execute();
            ArrayList<Comment> comments = new ArrayList<>();

            if(!response.isSuccessful()) {
                return null;
            }

            String json_string = response.body().string();

            JSONObject JSONResponse = new JSONObject(json_string);

            if (!JSONResponse.getBoolean("success")) {
                return null;
            }

            JSONArray comments_json = JSONResponse.getJSONArray("data");

            for(int i = 0; i < comments_json.length(); i++) {
                JSONObject comment = comments_json.getJSONObject(i);
                String commentText = comment.getString("comment");
                int commentId = comment.getInt("comment_id");
                int userId = comment.getInt("user_id");
                String username = comment.getString("username");

                list_comments.add(new Comment(commentText, userId, poiId, commentId, username));
            }

            return list_comments;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getBearerToken() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.user_token), Context.MODE_PRIVATE);

        return "Bearer " + sharedPref.getString("token", "");
    }

    public APIInterface getInterface() {
        return RetrofitClient.getClient(base_api_url).create(APIInterface.class);
    }
}
