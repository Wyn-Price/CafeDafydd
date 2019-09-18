package com.wynprice.cafedafydd.common.utils;

public enum RequestType {
    GET, SEARCH;

    public boolean isSearch() {
        return this == SEARCH;
    }
}
