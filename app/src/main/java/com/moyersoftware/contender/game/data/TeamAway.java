package com.moyersoftware.contender.game.data;

/**
 * Immutable model class for an Away Team.
 */
public class TeamAway {

    public String image;
    public String name;
    public Score score;

    public TeamAway() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public TeamAway(String image, String name, Score score) {
        this.image = image;
        this.name = name;
        this.score = score;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public Score getScore() {
        return score;
    }
}
