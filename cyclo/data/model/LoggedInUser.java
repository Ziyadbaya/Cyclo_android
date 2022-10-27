package com.dev.cyclo.data.model;

import androidx.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 * This is to be store in dataBase
 */
@RealmClass
public class LoggedInUser extends RealmObject {

    @PrimaryKey
    private ObjectId _id;
    private String image;
    private String username;
    private String password;
    private boolean online;

    /** Create a new user
     * @param username username
     * @param password password
     * @param online define if the user is online or not
     */
    public LoggedInUser(String image, @NonNull String username, @NonNull String password, boolean online) {
        this._id = new ObjectId();
        this.image = image;
        this.username = username;
        this.password = password;
        this.online = online;
    }

    public LoggedInUser(String image, @NonNull String username, @NonNull String password) {
        this._id = new ObjectId();
        this.image = image;
        this.username = username;
        this.password = password;
        this.online = true;
    }


    public LoggedInUser(String username, String password) {
        this._id = new ObjectId();
        this.username = username;
        this.password = password;
        this.online = true;
    }

    //needed by Realm database
    public LoggedInUser(){}

    public String getUsername() {
        return username;
    }

    public String getImage() { return image; }

    public String getPassword() {
        return password;
    }


    /**
     * @return a new document that contains all the field in LoggedInUser
     */
    public Document toDocument(){
        return new Document("username", username).append("password", password).append("online", online).append("image",image);
    }


    /**
     * @param doc the document must fit with a LoggedInUser
     * @return a new LoggedInUser
     */
    public static LoggedInUser toLoggedInUser(Document doc){
        return new LoggedInUser((String) doc.get("image"), (String) doc.get("username"), (String) doc.get("password"), (boolean) doc.get("online"));
    }


    /**
     * @return id the user is connected on the database
     */
    public boolean isOnline() {
        return online;
    }


    /** to be call on login and logout on database
     * @param online set if the user is connected or not on the database
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    @NotNull
    @Override
    public String toString() {
        return "LoggedInUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}