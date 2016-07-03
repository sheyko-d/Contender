package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Player.
 */
public final class Player {

    private String userId;
    private String email;
    private String name;

    public Player() {
        // Default constructor required for calls to DataSnapshot.getValue(Player.class)
    }

    public Player(String userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object != null && object instanceof Player) {
            sameSame = this.userId.equals(((Player) object).userId);
        }
        return sameSame;
    }
}
