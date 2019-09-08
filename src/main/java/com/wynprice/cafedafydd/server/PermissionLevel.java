package com.wynprice.cafedafydd.server;

import io.netty.util.AttributeKey;

public enum PermissionLevel {
    USER,
    STAFF_MEMBER,
    ADMINISTRATOR;

    public static final AttributeKey<PermissionLevel> PERMISSION_LEVEL = AttributeKey.newInstance("cd_permissions");
}
