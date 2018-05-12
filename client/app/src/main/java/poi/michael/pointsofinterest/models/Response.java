package poi.michael.pointsofinterest.models;

/**
 * Created by micha on 5/11/2018.
 */

public class Response<T> {
    boolean success;
    T data;

    public Response(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }
}
