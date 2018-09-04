package com.moyersoftware.contender.game.data;

/**
 * Immutable model class for a Home Team.
 */
public class TeamHome {

    public String image;
    public String name;
    public Score score;
    public String abbrev;
    public String color;
    public String location;
    public String img;

    public TeamHome() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public TeamHome(String image, String name, Score score, String abbrev, String color,
                    String location, String img) {
        this.image = image;
        this.name = name;
        this.score = score;
        this.abbrev = abbrev;
        this.color = color;
        this.location = location;
        this.img = img;
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

    public String getAbbrev() {
        return abbrev;
    }

    public String getColor() {
        return color;
    }

    public String getLocation() {
        return location;
    }

    public String getImg() {
        return img;
    }
}
