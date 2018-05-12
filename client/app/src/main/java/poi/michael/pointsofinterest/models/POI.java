package poi.michael.pointsofinterest.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by micha on 2/26/2018.
 * point of interest model
 */

public class POI {

    @Expose
    @SerializedName("title")
    private String mName;

    @Expose
    @SerializedName("lat")
    private double mLat;

    @Expose
    @SerializedName("long")
    private double mLong;

    @Expose
    @SerializedName("pio_id")
    private int mPoiId;

    private int mUserId;

    @Expose
    @SerializedName("rating")
    private float mRating;

    @Expose
    @SerializedName("description")
    private String mDescription;

    private String mUsername;

    public POI(String name, double lat, double _long, int poi_id) {
        this.mName = name;
        this.mLat = lat;
        this.mLong = _long;
        this.mPoiId = poi_id;
    }

    public POI(String name, double lat, double _long, int poi_id, int userId, float rating, String description, String username) {
        this.mName = name;
        this.mLat = lat;
        this.mLong = _long;
        this.mPoiId = poi_id;
        this.mUserId = userId;
        this.mRating = rating;
        this.mDescription = description;
        this.mUsername = username;
    }

    public String getName() {
        return mName;
    }

    public LatLng getLocation() {
        return new LatLng(mLat, mLong);
    }

    public int getPoiId() {
        return mPoiId;
    }

    public int getUserId() {
        return mUserId;
    }

    public float getRating() {
        return mRating;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUsername() {
        return mUsername;
    }
}
