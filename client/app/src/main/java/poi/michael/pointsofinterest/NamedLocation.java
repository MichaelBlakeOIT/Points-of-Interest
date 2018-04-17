package poi.michael.pointsofinterest;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by micha on 2/26/2018.
 */

public class NamedLocation {

    public final String name;
    public final LatLng location;
    public final int poiId;

    NamedLocation(String name, LatLng location, int poi_id) {
        this.name = name;
        this.location = location;
        this.poiId = poi_id;
    }
}
