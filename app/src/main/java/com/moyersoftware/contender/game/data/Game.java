package com.moyersoftware.contender.game.data;

/**
 * Immutable model class for a Game.
 */
public final class Game {

    private final String mName;
    private final Long mTime;
    private final String mImage;
    private final String mScore;
    private final String mUsername;

    public Game(String name, Long time, String image, String score, String username) {
        mName = name;
        mTime = time;
        mImage = image;
        mScore = score;
        mUsername = username;
    }

    public String getName() {
        return mName;
    }

    public Long getTime() {
        return mTime;
    }

    public String getImage() {
        return mImage;
    }

    public String getScore() {
        return mScore;
    }

    public String getUsername() {
        return mUsername;
    }
}
