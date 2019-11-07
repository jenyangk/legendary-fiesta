package io.github.cmput301f19t19.legendary_fiesta;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

/**
 * A Class representing a user of the app
 * Username is unique to each user and not changeable
 */
public class User implements Parcelable {
    private String username;
    private Date birthDate;
    private String description;
    private String uid;

    private ArrayList<String> following;
    private ArrayList<String> followedBy;
    private ArrayList<String> requestedBy;

    /**
     * Constructor for a user
     *
     * @param username    String username. Must be unique
     * @param birthDate   Date of the users birth
     * @param description Short string to act as a user bio
     */
    public User(String username, Date birthDate, String description) {
        this.username = username;
        this.birthDate = birthDate;
        this.description = description;

        this.followedBy = new ArrayList<>();
        this.following = new ArrayList<>();
        this.requestedBy = new ArrayList<>();
    }

    /**
     * Constructor for a user (for use with Serializers)
     * for Firebase database automated serialization
     */
    public User() { }

    protected User(Parcel in) {
        username = in.readString();
        description = in.readString();
        uid = in.readString();

        long date = in.readLong();
        if (date == -1) {
            birthDate = null;
        } else {
            birthDate = new Date(date);
        }

        this.followedBy = new ArrayList<>();
        this.following = new ArrayList<>();
        this.requestedBy = new ArrayList<>();

        in.readStringList(following);
        in.readStringList(followedBy);
        in.readStringList(requestedBy);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * @return String unique username
     */
    public String getUsername() {
        return username;
    }


    /**
     * @return String bio/description of the user
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description String bio/description of the user
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Firebase generated UID
     */
    public String getUid() {
        return uid;
    }

    /**
     * set UID of the user
     *
     * @param uid UID generated by Firebase Auth
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * @return Date birth date of the user
     */
    public Date getBirthDate() {
        return birthDate;
    }

    /**
     * @param birthDate Date birth date of the user
     */
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Accept another users request to follow your mood events
     *
     * @param uid the userId of the requesting user
     */
    public void acceptFollowRequest(String uid) {
        // TODO
    }

    /**
     * Reject another users request to follow your mood events
     *
     * @param uid the userId of the requesting user
     */
    public void rejectFollowRequest(String uid) {
        // TODO
    }

    /**
     * Request to follow another users mood events
     *
     * @param uid the userId of the user to be followed
     */
    public void requestToFollow(String uid) {
        // TODO
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(description);
        parcel.writeString(uid);
        parcel.writeLong(birthDate == null ? -1 : birthDate.getTime());
        parcel.writeStringList(following);
        parcel.writeStringList(followedBy);
        parcel.writeStringList(requestedBy);
    }
}
