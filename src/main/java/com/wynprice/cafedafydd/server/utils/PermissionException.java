package com.wynprice.cafedafydd.server.utils;

import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * An exception to throw when the permission level isn't enough
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class PermissionException extends RuntimeException {
    /**
     * The minimum permission level needed.
     */
    private final PermissionLevel atLeast;

    /**
     * The operation that was trying to be done when the exception was thrown.
     */
    private final String operation;
}
