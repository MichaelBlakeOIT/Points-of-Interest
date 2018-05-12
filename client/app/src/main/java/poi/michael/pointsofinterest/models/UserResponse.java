package poi.michael.pointsofinterest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by micha on 5/12/2018.
 * For handling API response containing user information and POIs
 */

public class UserResponse {
        @SerializedName("user")
        @Expose
        private User user;
        @SerializedName("pois")
        @Expose
        private List<POI> pois = null;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public List<POI> getPois() {
            return pois;
        }

        public void setPois(List<POI> pois) {
            this.pois = pois;
        }
}
