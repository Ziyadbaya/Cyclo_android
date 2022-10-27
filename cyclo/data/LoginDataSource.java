package com.dev.cyclo.data;

import com.dev.cyclo.Logger;
import com.dev.cyclo.data.model.LoggedInUser;
import com.dev.cyclo.realm.MongoRequest;
import com.dev.cyclo.ui.login.LoginActivity;
import io.realm.mongodb.AppException;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    Logger logger = new Logger(this.getClass());

    public Result login(String username, String password) {

        try {
            //the local account
            if (username.equals("Cyclome") && password.equals("JavaVelo")) {
                logger.log(Logger.Severity.Debug, "user connected : " + username, "IDENTIFICATION");
                return new Result.Success<>(new LoggedInUser(username, password));
            } else { // online accounts

                if(LoginActivity.online) {

                    try {
                        MongoRequest mongo = LoginActivity.mongo;
                        LoggedInUser result = mongo.getUser(username, password);

                        if (result == null) {
                            logger.log(Logger.Severity.Error, "fail to connect : " + username, "IDENTIFICATION");
                            return new Result.Error(new IOException("Error logging in"));
                        } else {
                            return new Result.Success<>(result);
                        }

                    } catch (AppException e) { //catch exception on database
                        logger.log(Logger.Severity.Error, e.toString(), "IDENTIFICATION");
                        return new Result.Error(new IOException("Error logging in", e));
                    }
                }

            }

        } catch (Exception e) { //catch exception on identification
            logger.log(Logger.Severity.Error, e.getMessage(), "IDENTIFICATION");
            return new Result.Error(new IOException("Error logging in", e));
        }

        return new Result.Error(new IOException("Error logging in"));

    }

    /** logout the user on database
     * @param user the user ti logout
     */
    public void logout(LoggedInUser user) {
        if(LoginActivity.online){
            LoginActivity.mongo.setOnline(user, false);
        }
    }
}