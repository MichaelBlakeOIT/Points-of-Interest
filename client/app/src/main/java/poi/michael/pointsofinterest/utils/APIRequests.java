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


    public String getBearerToken() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(mContext.getString(R.string.user_token), Context.MODE_PRIVATE);

        return "Bearer " + sharedPref.getString("token", "");
    }

    public APIInterface getInterface() {
        return RetrofitClient.getClient(base_api_url).create(APIInterface.class);
    }
}
