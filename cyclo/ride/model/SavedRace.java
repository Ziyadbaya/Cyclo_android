package com.dev.cyclo.ride.model;

import androidx.annotation.NonNull;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * remote data of a past ride which has to be saved on database
 * various data known after the end of the ride
 */

public class SavedRace extends RealmObject {

    @PrimaryKey
    private ObjectId _id;           //required
    private  String username;
    private  int routeId;
    private double totalDistance;
    private double time;
    private double meanSpeed;
    private double maxSpeed;
    private Date date;
    private String opponentUsername;
    private boolean result;

    public SavedRace(@NonNull String username,
                     int routeId,
                     double totalDistance,
                     double time,
                     double meanSpeed,
                     double maxSpeed,
                     @NonNull Date date,
                     @NonNull String opponentUsername,
                     boolean result) {
        this._id = new ObjectId();
        this.username = username;
        this.routeId = routeId;
        this.totalDistance = totalDistance;
        this.time = time;
        this.meanSpeed = meanSpeed;
        this.maxSpeed = maxSpeed;
        this.date = date;
        this.opponentUsername = opponentUsername;
        this.result = result;
    }

    public SavedRace(){}

    @NonNull
    public static com.dev.cyclo.ride.model.SavedRace toSavedRace(@NonNull Document doc){
        return new com.dev.cyclo.ride.model.SavedRace(
                doc.getString("username"),
                doc.getInteger("routeId"),
                doc.getDouble("totalDistance"),
                doc.getDouble("time"),
                doc.getDouble("meanSpeed"),
                doc.getDouble("maxSpeed"),
                doc.getDate("date"),
                doc.getString("opponentUsername"),
                doc.getBoolean("result")
        );
    }

    @NonNull
    public Document toDocument(){
        return new Document("username", username)
                .append("routeId", routeId)
                .append("totalDistance",totalDistance)
                .append("time",time)
                .append("meanSpeed", meanSpeed)
                .append("maxSpeed",maxSpeed)
                .append("date", date)
                .append("opponentUsername", opponentUsername)
                .append("result", result);
    }

    public int getRouteId() {
        return routeId;
    }

    public double getTime() {
        return time;
    }

    public double getMeanSpeed() {
        return meanSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public String getOpponentUsername() {
        return opponentUsername;
    }

    public boolean isResult() {
        return result;
    }

    public double getTotalDistance() { return totalDistance; }

    @NonNull
    @Override
    public String toString() {
        return "SavedRace{" +
                "routeId=" + routeId +
                ", totalDistance=" + totalDistance +
                ", time=" + time +
                ", meanSpeed=" + meanSpeed +
                ", maxSpeed=" + maxSpeed +
                ", date=" + date +
                ", opponentUsername='" + opponentUsername + '\'' +
                ", result=" + result +
                '}';
    }
}
