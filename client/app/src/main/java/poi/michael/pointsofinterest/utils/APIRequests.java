package poi.michael.pointsofinterest.utils;

import poi.michael.pointsofinterest.interfaces.APIInterface;

/**
 * Created by michael on 4/17/2018.
 * Class for implementing API requests.
 */

public class APIRequests {
    private static final String base_api_url = "https://points-of-interest.herokuapp.com/";

    public APIInterface getInterface() {
        return RetrofitClient.getClient(base_api_url).create(APIInterface.class);
    }
}
