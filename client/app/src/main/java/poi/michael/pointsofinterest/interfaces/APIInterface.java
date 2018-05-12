package poi.michael.pointsofinterest.interfaces;

import java.util.List;

import poi.michael.pointsofinterest.models.Comment;
import poi.michael.pointsofinterest.models.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by micha on 5/11/2018.
 * Retrofit interface
 */

public interface APIInterface {
    @GET("/poi/{poi_id}/comments")
    Call<Response<List<Comment>>> commentList(@Path("poi_id") int poiId, @Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("/poi/{poi_id}/comments")
    Call<Response<Comment>> comment(@Path("poi_id") int poiId, @Field("comment") String comment, @Header("Authorization") String authHeader);
}
