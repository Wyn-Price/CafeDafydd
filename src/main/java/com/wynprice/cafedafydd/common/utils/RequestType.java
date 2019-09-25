package com.wynprice.cafedafydd.common.utils;

/**
 * The request type, used in database requests.
 */
public enum RequestType {
    GET, SEARCH;

    public boolean isSearch() {
        return this == SEARCH;
    }
}
