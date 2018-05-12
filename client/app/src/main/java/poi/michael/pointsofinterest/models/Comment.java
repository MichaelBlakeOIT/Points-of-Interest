package poi.michael.pointsofinterest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by micha on 3/12/2018.
 */

public class Comment {
    @SerializedName("comment")
    @Expose
    private String mComment;

    @SerializedName("user_id")
    @Expose
    private int mUserId;

    @SerializedName("point_of_interest_id")
    @Expose
    private int mPoiId;

    @SerializedName("comment_id")
    @Expose
    private int mCommentId;

    @SerializedName("username")
    @Expose
    private String mUsername;

    public Comment(String comment, int user_id, int poi_id, int comment_id, String username) {
        mComment = comment;
        mCommentId = comment_id;
        mUserId = user_id;
        mUsername = username;
        mPoiId = poi_id;
    }

    public String getComment() {
        return mComment;
    }

    public int getPoiId() {
        return mPoiId;
    }

    public String getUsername() {
        return mUsername;
    }

    public int getCommentId() {
        return mCommentId;
    }

    public int getUserId() {
        return mUserId;
    }
}