package com.wynprice.cafedafydd.client;

import lombok.Getter;

public enum Page {
    MAIN_PAGE("mainpage");

    @Getter private final String fileName;

    Page(String fileName) {
        this.fileName = fileName;
    }
}
