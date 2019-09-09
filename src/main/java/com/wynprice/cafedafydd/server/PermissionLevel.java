package com.wynprice.cafedafydd.server;

import io.netty.util.AttributeKey;

public enum PermissionLevel {
    USER(0),
    STAFF_MEMBER(1),
    ADMINISTRATOR(2);

    private final int perIndex;

    public static final AttributeKey<PermissionLevel> PERMISSION_LEVEL = AttributeKey.newInstance("cd_permissions");

    PermissionLevel(int perIndex) {
        this.perIndex = perIndex;
    }

    public int getPerIndex() {
        return this.perIndex;
    }
}
