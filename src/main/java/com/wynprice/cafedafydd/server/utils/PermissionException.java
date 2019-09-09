package com.wynprice.cafedafydd.server.utils;

import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class PermissionException extends RuntimeException {
    private final PermissionLevel atLeast;
}
