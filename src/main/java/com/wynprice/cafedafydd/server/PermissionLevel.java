package com.wynprice.cafedafydd.server;

/**
 * The permission level that the user can have.
 */
public enum PermissionLevel {
    USER(0),
    STAFF_MEMBER(1),
    ADMINISTRATOR(2),
    IMPOSSIBLE(99); //Used for testing

    private final int permIndex;
    
    PermissionLevel(int permIndex) {
        this.permIndex = permIndex;
    }

    public int getPermIndex() {
        return this.permIndex;
    }

    public static PermissionLevel getLevel(String level) {
        for (PermissionLevel value : values()) {
            if (value.name().equals(level)) {
                return value;
            }
        }
        return USER;
    }
}
