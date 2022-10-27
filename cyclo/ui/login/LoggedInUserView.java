package com.dev.cyclo.ui.login;

import java.io.Serializable;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView implements Serializable { //Serializable allow this object to be used by Intent.putExtra()
    private final String displayName;
    private final String displayImage;
    //... other data fields that may be accessible to the UI




    LoggedInUserView(String displayName, String displayImage) {
        this.displayImage = displayImage;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayImage() { return displayImage; }
}