package com.moyersoftware.contender.games.data;

/**
 * Immutable model class for a Game.
 */
public final class Game {

    private final String mName;
    private final Long mTime;
    private final String mImage;
    private final String mScore;

    public Game(String name, Long time, String image, String score) {
        mName = name;
        mTime = time;
        mImage = image;
        mScore = score;
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

}
