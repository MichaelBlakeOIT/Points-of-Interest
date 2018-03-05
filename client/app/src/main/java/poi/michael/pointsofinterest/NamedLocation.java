package poi.michael.pointsofinterest;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by micha on 2/26/2018.
 */

public class NamedLocation {

    public final String name;
    public final LatLng location;

    NamedLocation(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }
}
