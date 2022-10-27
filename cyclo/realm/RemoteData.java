package com.dev.cyclo.realm;

import com.google.android.gms.maps.model.LatLng;
import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

/**
 * This class represent the objet store in database
 * Every user have RemoteData and those are share with other
 */
@RealmClass
public class RemoteData implements RealmModel {

    @PrimaryKey
    private ObjectId _id; // needed as the realm Primary key
    private String username;
    private double lng;
    private double lat;
    private int idRoute;
    private boolean ready; //useful for start synchronisation
    private boolean invited;

    /**
     * @param username the username of the user, must be the same as loggedInUSer
     * @param lat the current latitude of the user
     * @param lng the current longitude og the user
     * @param idRoute the route on which the user is in ride, default is 0
     * @param ready define if the user is ready to start a race or in a race
     */
    public RemoteData(String username, double lat, double lng, int idRoute, boolean ready, boolean invited) {
        this.username = username;
        this.lng = lng;
        this.lat = lat;
        this.idRoute = idRoute;
        this.ready = ready;
        this.invited = invited;
    }

    /** Create a shallow copy of remote data and change the latitude and longitude in the new data
     * @param data the data to copy
     * @param latLng the new latitude and longitude
     */
    public RemoteData(RemoteData data, LatLng latLng){
        this.username = data.username;
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
        this.idRoute = data.idRoute;
        this.ready = data.ready;
        this.invited = data.invited;
    }

    public RemoteData(String username) {
        this.username = username;
        this.lat = 0;
        this.lng = 0;
        this.idRoute = 0;
        this.ready = false;
        this.invited = false;
    }

    //needed for Realm database
    public RemoteData(){}


    /**
     * @return a new document containing the fields in the remoteData
     */
    public Document toDocument(){
        return new Document("username", username).append("lng", lng).append("lat", lat).append("idRoute", idRoute).append("ready", ready).append("invited",invited);
    }

    /**
     * @param doc the document that must fit with remote data
     * @return a new remote data object corresponding to the document
     */
    public static RemoteData toRemoteData(Document doc){
        return new RemoteData((String) doc.get("username"), (double) doc.get("lat"), (double) doc.get("lng"), (int) doc.get("idRoute"), (boolean) doc.get("ready"), (boolean) doc.get("invited"));
    }

    public LatLng getLatLng(){
        return new LatLng(lat, lng);
    }

    public void setLatLng(LatLng latLng) {
        this.lng = latLng.longitude;
        this.lat = latLng.latitude;
    }

    public boolean getInvited(){
        return this.invited;
    }

    public boolean isReady(){
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setIdRoute(int idRoute) {
        this.idRoute = idRoute;
    }

    public int getIdRoute() {return this.idRoute;}

    public void setInvited(boolean invited) { this.invited = invited; }

    @NotNull
    public String toString() {
        return "RemoteData{" +
                "username='" + username + '\'' +
                ", lng=" + lng +
                ", lat=" + lat +
                ", idRoute=" + idRoute +
                ", isReady=" + ready +
                '}';
    }
}