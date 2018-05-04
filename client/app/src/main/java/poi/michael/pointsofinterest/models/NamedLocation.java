package poi.michael.pointsofinterest.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by micha on 2/26/2018.
 */

public class NamedLocation {

    private String mName;
    private LatLng mLocation;
    private int mPoiId;
    private int mUserId;
    private float mRating;
    private String mDescription;
    private String mUsername;

    public NamedLocation(String name, LatLng location, int poi_id) {
        this.mName = name;
        this.mLocation = location;
        this.mPoiId = poi_id;
    }

    public NamedLocation(String name, LatLng location, int poi_id, int userId, float rating, String description, String username) {
        this.mName = name;
        this.mLocation = location;
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
        return mLocation;
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
