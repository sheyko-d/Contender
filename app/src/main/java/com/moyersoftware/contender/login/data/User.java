package com.moyersoftware.contender.login.data;

/**
 * Immutable model class for a User.
 */
public final class User {

    private final String mName;
    private final String mUsername;
    private final String mEmail;
    private final String mImage;

    public User(String name, String username, String email, String image) {
        mName = name;
        mUsername = username;
        mEmail = email;
        mImage = image;
    }

    public String getName() {
        return mName;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getImage() {
        return mImage;
    }
}
