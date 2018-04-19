package poi.michael.pointsofinterest;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by micha on 2/26/2018.
 */

class NamedLocation {

    private String mName;
    private LatLng mLocation;
    private int mPoiId;
    private int mUserId;
    private float mRating;
    private String mDescription;
    private String mUsername;

    NamedLocation(String name, LatLng location, int poi_id) {
        this.mName = name;
        this.mLocation = location;
        this.mPoiId = poi_id;
    }

    NamedLocation(String name, LatLng location, int poi_id, int userId, float rating, String description, String username) {
        this.mName = name;
        this.mLocation = location;
        this.mPoiId = poi_id;
        this.mUserId = userId;
        this.mRating = rating;
        this.mDescription = description;
        this.mUsername = username;
    }

    String getName() {
        return mName;
    }

    LatLng getLocation() {
        return mLocation;
    }

    int getPoiId() {
        return mPoiId;
    }

    int getUserId() {
        return mUserId;
    }

    float getRating() {
        return mRating;
    }

    String getDescription() {
        return mDescription;
    }

    String getUsername() {
        return mUsername;
    }
}
