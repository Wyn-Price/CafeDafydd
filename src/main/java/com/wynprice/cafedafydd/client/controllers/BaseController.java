package com.wynprice.cafedafydd.client.controllers;

public interface BaseController {
    default void onLoaded() {
        //NO-OP
    }
}
