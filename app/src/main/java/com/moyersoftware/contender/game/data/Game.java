package com.moyersoftware.contender.game.data;

import java.util.ArrayList;

/**
 * Immutable model class for a Game.
 */
public class Game {

    public String eventId;
    public String id;
    public String name;
    public long time;
    public String image;
    public String score;
    public String authorId;
    public String authorUsername;
    public String password;
    public int squarePrice;
    public int quarter1Price;
    public int quarter2Price;
    public int quarter3Price;
    public int finalPrice;
    public int totalPrice;
    public double latitude;
    public double longitude;
    public ArrayList<String> players;
    public ArrayList<Integer> rowNumbers;
    public ArrayList<Integer> columnNumbers;
    public ArrayList<SelectedSquare> selectedSquares;

    public Game() {
        // Default constructor required for calls to DataSnapshot.getValue(Game.class)
    }

    public Game(String eventId, String id, String name, Long time, String image, String score,
                String authorId, String authorUsername, String password, int squarePrice,
                int quarter1Price, int quarter2Price, int quarter3Price, int finalPrice,
                int totalPrice, double latitude, double longitude, ArrayList<String> players,
                ArrayList<Integer> rowNumbers, ArrayList<Integer> columnNumbers,
                ArrayList<SelectedSquare> selectedSquares) {
        this.eventId = eventId;
        this.id = id;
        this.name = name;
        this.time = time;
        this.image = image;
        this.score = score;
        this.authorId = authorId;
        this.authorUsername = authorUsername;
        this.password = password;
        this.squarePrice = squarePrice;
        this.quarter1Price = quarter1Price;
        this.quarter2Price = quarter2Price;
        this.quarter3Price = quarter3Price;
        this.finalPrice = finalPrice;
        this.totalPrice = totalPrice;
        this.latitude = latitude;
        this.longitude = longitude;
        this.players = players;
        this.rowNumbers = rowNumbers;
        this.columnNumbers = columnNumbers;
        this.selectedSquares = selectedSquares;
    }

    public String getEventId() {
        return eventId;
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

    public String getAuthorId() {
        return authorId;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public ArrayList<Integer> getRowNumbers() {
        return rowNumbers;
    }

    public ArrayList<Integer> getColumnNumbers() {
        return columnNumbers;
    }

    public ArrayList<SelectedSquare> getSelectedSquares() {
        return selectedSquares;
    }
}
