package com.dev.cyclo.realm;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.util.ArrayMap;

import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.dev.cyclo.data.model.LoggedInUser;
import com.dev.cyclo.ride.Ride;
import com.dev.cyclo.ride.model.SavedRace;

import com.dev.cyclo.ui.login.LoginActivity;
import io.realm.Realm;
import io.realm.mongodb.*;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.sync.SyncConfiguration;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * This class manage the anonymous connection to Realm MongoDB and provide method to update the database
 */
public class MongoRequest extends HandlerThread {

    private final Logger logger = new Logger(this.getClass());

    private final App app;
    private Realm uiThreadRealm;
    private MongoClient client;
    private MongoDatabase database;

    //this is to have benchmark on database rapidity
    public static int nbGet = 0;
    public static int nbUpdate = 0;
    public static long timeGet = 0;
    public static long timeUpdate = 0;

    //define if a ride is in progress
    private boolean isOnRide;

    /**
     * this constructor connect the JavaVelo data base
     * the connection with the data base is anonymous
     *
     * @param context need the main context for the initialisation of Realm
     */
    public MongoRequest(Context context) {
        super("mongoThread");
        setPriority(10);

        logger.log(Logger.Severity.Debug, "mongoRequest constructor");

        //this allow to do synchronous request on database (useful for login)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Realm.init(context);
        String appID = "javavelorealmapplication-begxk";

        logger.log(Logger.Severity.Info, "app config started");
        app = new App(new AppConfiguration.Builder(appID)
                .requestTimeout(50, TimeUnit.MILLISECONDS)
                .build());

        Credentials credentials = Credentials.anonymous();



        app.login(credentials);
        String partitionValue = "My Project";

        User user = app.currentUser();


        logger.log(Logger.Severity.Debug, "utilisateur: " + user);
        if (user == null) {
            logger.log(Logger.Severity.Error, "database user is null");
        } else {
            SyncConfiguration config = new SyncConfiguration.Builder(
                    user,
                    partitionValue)
                    .allowWritesOnUiThread(true)
                    .allowQueriesOnUiThread(true)
                    .build();

            logger.log(Logger.Severity.Debug, "start getInstance");
            uiThreadRealm = Realm.getInstance(config);
            client = user.getMongoClient("mongodb-atlas");
            database = client.getDatabase("JavaVelo");

            logger.log(Logger.Severity.Debug, "Connection successful with database " + user);
        }


    }

    /**
     * Add in collection User a new user
     *
     * @param username the name of the user
     * @param password the password of the user
     * @return false if the username already exists, true if the user has been added
     */
    public boolean addUser(String image, String username, String password) {

        Set<String> usernames = LoginActivity.mongo.getAllUserName().keySet();

        if(!usernames.contains(username)) { // if the username not exists

            logger.log(Logger.Severity.Debug, "try to add user" + username);

            LoggedInUser loggedInUser = new LoggedInUser(image, username, password);

            uiThreadRealm.executeTransaction(transactionRealm -> {
                //logger.log(Logger.Severity.Error, "beginning transaction add");
                database.getCollection("User").insertOne(loggedInUser.toDocument()).get();
                //logger.log(Logger.Severity.Error, "end transaction add");
            });
            addRemoteData(username);
            return true;
        }
        // if the username already exits
        return false;

    }

    /**
     * Create a new document remoteData on database that will be use to share data with other user
     * @param username primary key to find a the remoteData on database
     */
    private void addRemoteData(String username) {
        //logger.log(Logger.Severity.Error, "try to add remoteData" + username);

        RemoteData remoteData = new RemoteData(username);

        uiThreadRealm.executeTransaction(transactionRealm -> {
            database.getCollection("RemoteDatas").insertOne(remoteData.toDocument()).get();
            logger.log(Logger.Severity.Debug, "RemoteData added to database for " + username);
        });
    }

    /**
     * Search a user on the database
     *
     * @param userName the name of the user
     * @param password the password of the user
     * @return a LoggedInUser or null if it's not found
     */
    public LoggedInUser getUser(String userName, String password) {
        logger.log(Logger.Severity.Debug, "try to find " + userName + " on database");

        Document documentToFind = new Document("username", userName).append("password", password);

        final LoggedInUser[] loggedInUser = new LoggedInUser[1];

        uiThreadRealm.executeTransaction(transactionRealm -> {
            logger.log(Logger.Severity.Error, "beginning transaction get");
            Document doc = database.getCollection("User").findOne(documentToFind).get();
            if(doc != null) {
                loggedInUser[0] = LoggedInUser.toLoggedInUser(doc);
            }
            else {
                loggedInUser[0] = null;
            }
        });

        return loggedInUser[0];
    }



    /**
     * Download all username in the database connected or not
     * @return an ArrayMap<String, Boolean> with username and connected or not to the database
     */
    public Map<String, Boolean> getAllUserName() {
        logger.log(Logger.Severity.Debug, "try to find all username");

        Map<String, Boolean> usernames = new ArrayMap<>();

        uiThreadRealm.executeTransaction(transactionRealm -> {
            logger.log(Logger.Severity.Debug, "beginning transaction getAll");
            MongoCursor<Document> mongoCursor = database.getCollection("User").find().iterator().get();
            while (mongoCursor.hasNext()) {
                Document user = mongoCursor.next();
                usernames.put((String) user.get("username") , (boolean) user.get("online"));
            }
            logger.log(Logger.Severity.Error, "end transaction getAll");
        });
        return usernames;
    }




    public RemoteData getRemoteData(String username) {
        Document data = database.getCollection("RemoteDatas").findOne(new Document("username", username)).get();
        //System.out.println("$$$$$$$$$$$$$$$$$$$$"+data.get("invited"));
        RemoteData remoteData = RemoteData.toRemoteData(data);
        return remoteData;
    }

    public Document getRemoteDataDoc(String username) {
        Document data = database.getCollection("RemoteDatas").findOne(new Document("username", username)).get();
        return data;
    }




    /**
     * This download the remoteData of the user define by the username in parameter
     * This method call Ride.updateOpponentData
     * @param username the username to download data
     */
    public void getRemoteDataByThread(String username) {
        if (!this.isAlive()) this.start();
        Handler handler = new Handler(this.getLooper());

        Runnable getData = new Runnable() {
            @Override
            public void run() {
                long chrono = System.currentTimeMillis();
                setPriority(10);
                try {
                    Document data = database.getCollection("RemoteDatas").findOne(new Document("username", username)).get();
                    System.out.println("the remote data we are getting is : " + data);
                    RemoteData remoteData = RemoteData.toRemoteData(data);
                    Ride.updateOpponentData(remoteData);

                    //for benchmark
                    timeGet += (System.currentTimeMillis() - chrono);
                    nbGet += 1;
                    //logger.log(Logger.Severity.Error, "timeget" + (System.currentTimeMillis() - chrono));

                } catch (AppException e) {
                    logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
                }

                if (isOnRide) {
                    handler.postDelayed(this, Main.config.getConfigData().getMongodb_request_delay());
                }
            }
        };
        handler.post(getData);
    }

    /**
     * This update the remoteData of the user define by the username in parameter
     * The data use to update is those in Ride.currentUserData
     *
     * @param username the username to update data
     */
    public void updateRemoteDataByThread(String username) {
        if (!this.isAlive()) this.start();
        Handler handler = new Handler(this.getLooper());

        Document doc = new Document("username", username);

        // 2.4 - Executing a new Runnable
        Runnable update = new Runnable() {
            @Override
            public void run() {
                long chrono = System.currentTimeMillis();
                setPriority(10);
                RemoteData newRemote = Ride.currentUserRemoteData;
                try {
                    database.getCollection("RemoteDatas").updateOne(doc, newRemote.toDocument()).get();
                    //logger.log(Logger.Severity.Info, "update data");
                } catch (AppException e) {
                    logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
                }

                //for benchmark
                nbUpdate += 1;
                timeUpdate += (System.currentTimeMillis() - chrono);
                //logger.log(Logger.Severity.Error, "timeupdate" + (System.currentTimeMillis() - chrono));

                if (isOnRide) {
                    handler.postDelayed(this, Main.config.getConfigData().getMongodb_request_delay());
                }
            }
        };
        handler.post(update);
    }

    /**
     * Methode to call to notify that a user is ready to start a race
     * @param username username of the user
     */
    public void setReady(String username) {
        if (!this.isAlive()) this.start();

        isOnRide = true;

        Handler handler = new Handler(this.getLooper());
        Document doc = new Document("username", username);

        Runnable update = () -> {
            setPriority(10);
            RemoteData newRemote = Ride.currentUserRemoteData;
            try {
                database.getCollection("RemoteDatas").updateOne(doc, newRemote.toDocument()).get();
                //logger.log(Logger.Severity.Info, "update data");
            } catch (AppException e) {
                logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
            }

        };
        handler.post(update);
    }

    /**
     * Methode to call to notify that a user is has finish a race
     * @param username username of the user
     */
    public void rideQuit(String username) {
        if (!this.isAlive()) this.start();

        isOnRide = false;

        Handler handler = new Handler(this.getLooper());
        Document doc = new Document("username", username);

        Runnable update = () -> {
            setPriority(10);
            RemoteData newRemote = Ride.currentUserRemoteData;
            //newRemote.setIdRoute(0);
            newRemote.setReady(false);
            try {
                database.getCollection("RemoteDatas").updateOne(doc, newRemote.toDocument()).get();
                //logger.log(Logger.Severity.Info, "update data");
            } catch (AppException e) {
                logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
            }

        };
        handler.post(update);
    }

    /** Define is a user in on the application or not
     * @param loggedInUser the user
     * @param online set to true if the user is online
     */
    public void setOnline(LoggedInUser loggedInUser, boolean online) {

        if (!this.isAlive()) this.start();

        Handler handler = new Handler(this.getLooper());
        Document doc = new Document("username", loggedInUser.getUsername());

        Runnable update = () -> {
            setPriority(10);
            loggedInUser.setOnline(online);
            try {
                database.getCollection("User").updateOne(doc, loggedInUser.toDocument()).get();
            } catch (AppException e) {
                logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
            }
        };
        handler.post(update);

        //logger.log(Logger.Severity.Debug, loggedInUser.getUsername() + " : " + online);
    }


    /** Add a SavedRace in the collection "Races"
     * @param savedRace the race to saved
     */
    public void addSavedRace(SavedRace savedRace) {
        if(!this.isAlive()){this.start();}
        Handler handler = new Handler(this.getLooper());

        logger.log(Logger.Severity.Debug, "try to add a saved race" + savedRace);

        Runnable add = () -> {
            //logger.log(Logger.Severity.Error, "beginning transaction add saved Race");
            database.getCollection("Races").insertOne(savedRace.toDocument()).get();
            //logger.log(Logger.Severity.Error, "end transaction add saved Race");
        };

        handler.post(add);
    }


    /**
     * @param username the username defining the user to find races
     * @return A LinkedList<SavedRace> containing all races done by the user
     */
    public List<SavedRace> getSavedRace(String username){
        if(!this.isAlive()){this.start();}
        List<SavedRace> savedRaces = new LinkedList<>();
        uiThreadRealm.executeTransaction(transactionRealm -> {

            try {
                Iterator<Document> races = database.getCollection("Races").find(new Document("username", username)).iterator().get();

                while (races.hasNext()) {
                    Document race = races.next();
                    savedRaces.add(SavedRace.toSavedRace(race));
                }

            } catch (AppException e) {
                logger.log(Logger.Severity.Error, e.getErrorType() + e.getErrorMessage());
            }
        });
        return savedRaces;
    }


    @NotNull
    public String toString() {
        return "MongoRequest{" +
                "app=" + app +
                ", logger=" + logger +
                ", uiThreadRealm=" + uiThreadRealm +
                ", client=" + client +
                ", database=" + database +
                '}';
    }
}