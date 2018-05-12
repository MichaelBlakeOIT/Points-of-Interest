package poi.michael.pointsofinterest.models;

/**
 * Created by micha on 5/12/2018.
 */

public class SuccessResponse {
    String mMessage;
    SuccessResponse(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }
}
