package poi.michael.pointsofinterest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by micha on 5/11/2018.
 */

public class User {
    @Expose
    @SerializedName("user_id")
    private int mUserId;

    @Expose
    @SerializedName("username")
    private String mUsername;

    @Expose
    @SerializedName("first_name")
    private String mFirstName;

    @Expose
    @SerializedName("last_name")
    private String mLastName;

    @Expose
    @SerializedName("bio")
    private String mBio;

    @Expose
    @SerializedName("profile_photo")
    private int mHasProfilePicture;

    @Expose
    @SerializedName("following")
    private int mIsFollowing;

    public User(int mUserId, String mUsername, String mFirstName, String mLastName, String mBio, int mHasProfilePicture) {
        this.mUserId = mUserId;
        this.mUsername = mUsername;
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mBio = mBio;
        this.mHasProfilePicture = mHasProfilePicture;
    }

    public User(int mUserId, String mUsername, String mFirstName, String mLastName) {
        this.mUserId = mUserId;
        this.mUsername = mUsername;
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
    }

    public int getUserId() {
        return mUserId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public int getIsFollowing() { return mIsFollowing; }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getBio() {
        return mBio;
    }

    public int getHasProfilePicture() {
        return mHasProfilePicture;
    }

    public void setBio(String mBio) {
        this.mBio = mBio;
    }

    public void setHasProfilePicture(int mHasProfilePicture) {
        this.mHasProfilePicture = mHasProfilePicture;
    }

    public void setIsFollowing(int following) {
        this.mIsFollowing = following;
    }
}
