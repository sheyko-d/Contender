package com.moyersoftware.contender.menu.data;

/**
 * Immutable model class for a Friendship.
 */
public final class Friendship {

    public String user1Id;
    public String user2Id;

    public Friendship(String user1Id, String user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    public Friendship() {
        // Default constructor required for calls to DataSnapshot.getValue(Friendship.class)
    }

    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }
}
