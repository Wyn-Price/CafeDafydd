package com.wynprice.cafedafydd.server;

public enum PermissionLevel {
    USER(0),
    STAFF_MEMBER(1),
    ADMINISTRATOR(2),
    IMPOSSIBLE(99);

    private final int perIndex;
    
    PermissionLevel(int perIndex) {
        this.perIndex = perIndex;
    }

    public int getPerIndex() {
        return this.perIndex;
    }
}
