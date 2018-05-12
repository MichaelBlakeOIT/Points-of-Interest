package poi.michael.pointsofinterest.interfaces;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import java.util.List;

import poi.michael.pointsofinterest.models.Comment;
import poi.michael.pointsofinterest.models.POI;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.models.SuccessResponse;
import poi.michael.pointsofinterest.models.UserResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
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
    Call<Response<List<Comment>>> getCommentList(@Path("poi_id") int poiId,
                                                 @Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("/poi/{poi_id}/comments")
    Call<Response<Comment>> createComment(@Path("poi_id") int poiId,
                                          @Field("comment") String comment,
                                          @Header("Authorization") String authHeader);

    @GET("/users/user/{username}")
    Call<Response<UserResponse>> getUser(@Path("username") String username,
                                         @Header("Authorization") String authHeader);

    @POST("/users/user/{username}/follow")
    Call<Response<SuccessResponse>> followUser(@Path("username") String username,
                                               @Header("Authorization") String authHeader);

    @DELETE("/users/user/{username}/follow")
    Call<Response<SuccessResponse>> unfollowUser(@Path("username") String username,
                                                 @Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("/session")
    Call<Response<String>> login(@Field("username") String username,
                                 @Field("password") String password);

    @GET("/poi")
    Call<Response<List<POI>>> getAllPois(@Header("Authorization") String authHeader);

    @GET("/users/pois/following")
    Call<Response<List<POI>>> getFollowingPois(@Header("Authorization") String authHeader);

    @GET("/users/pois/saved")
    Call<Response<List<POI>>> getSavedPois(@Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("/users")
    Call<Response<SuccessResponse>> register(@Field("username") String username,
                                             @Field("password") String password,
                                             @Field("firstname") String firstName,
                                             @Field("lastName") String lastName,
                                             @Field("email") String email);

    @FormUrlEncoded
    @POST("/poi")
    Call<Response<SuccessResponse>> addPoi(@Field("lat") double lat,
                                           @Field("long") double _long,
                                           @Field("title") String name,
                                           @Field("description") String description,
                                           @Header("Authorization") String authHeader);

    @FormUrlEncoded
    @POST("/users/reset")
    Call<Response<SuccessResponse>> sendResetEmail(@Field("username") String username);
}
