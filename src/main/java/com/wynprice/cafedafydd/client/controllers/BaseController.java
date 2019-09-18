package com.wynprice.cafedafydd.client.controllers;

public interface BaseController {
    default void onLoaded() {
        this.resync();
    }

    default void resync() {
        //NO-OP
    }
}
