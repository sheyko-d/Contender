package com.moyersoftware.contender.game.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Immutable model class for a Game.
 */
@IgnoreExtraProperties
public class Game {

    public String id;
    public String name;
    public long time;
    public String image;
    public String score;
    public String authorUsername;
    public String password;
    public int squarePrice;
    public int quarter1Price;
    public int quarter2Price;
    public int quarter3Price;
    public int finalPrice;
    public int totalPrice;

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Game(String id, String name, Long time, String image, String score,
                String authorUsername, String password, int squarePrice, int quarter1Price,
                int quarter2Price, int quarter3Price, int finalPrice, int totalPrice) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.image = image;
        this.score = score;
        this.authorUsername = authorUsername;
        this.password = password;
        this.squarePrice = squarePrice;
        this.quarter1Price = quarter1Price;
        this.quarter2Price = quarter2Price;
        this.quarter3Price = quarter3Price;
        this.finalPrice = finalPrice;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }

    public String getImage() {
        return image;
    }

    public String getScore() {
        return score;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getPassword() {
        return password;
    }

    public int getSquarePrice() {
        return squarePrice;
    }

    public int getQuarter1Price() {
        return quarter1Price;
    }

    public int getQuarter2Price() {
        return quarter2Price;
    }

    public int getQuarter3Price() {
        return quarter3Price;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}
