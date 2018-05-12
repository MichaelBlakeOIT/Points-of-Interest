package poi.michael.pointsofinterest.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by micha on 5/11/2018.
 */

public class User {
    @SerializedName("user_id")
    private int mUserId;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("first_name")
    private String mFirstName;

    @SerializedName("last_name")
    private String mLastName;

    @SerializedName("bio")
    private String mBio;

    @SerializedName("profile_photo")
    private int mHasProfilePicture;

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

    public int getmUserId() {
        return mUserId;
    }

    public String getmUsername() {
        return mUsername;
    }

    public String getmFirstName() {
        return mFirstName;
    }

    public String getmLastName() {
        return mLastName;
    }

    public void setmUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public void setmFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public void setmLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getmBio() {
        return mBio;
    }

    public int getmHasProfilePicture() {
        return mHasProfilePicture;
    }

    public void setmBio(String mBio) {
        this.mBio = mBio;
    }

    public void setmHasProfilePicture(int mHasProfilePicture) {
        this.mHasProfilePicture = mHasProfilePicture;
    }
}
