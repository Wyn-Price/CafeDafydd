package com.wynprice.cafedafydd.common;

import lombok.Getter;

public enum Page {
    LOGIN_PAGE("login_page"),
    USER_PAGE("user_page"),

    ADD_USER_PAGE("create_user"),

    ADMINISTRATOR_PAGE("administrator_panel");

    @Getter private final String fileName;

    Page(String fileName) {
        this.fileName = fileName;
    }
}
