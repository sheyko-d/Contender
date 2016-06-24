package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Friend.
 */
public final class Friend {

    private final String mName;
    private final String mUsername;
    private final String mImage;

    public Friend(String name, String username, String image) {
        mName = name;
        mUsername = username;
        mImage = image;
    }

    public String getName() {
        return mName;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getImage() {
        return mImage;
    }
}
