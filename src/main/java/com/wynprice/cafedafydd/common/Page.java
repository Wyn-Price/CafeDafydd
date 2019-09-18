package com.wynprice.cafedafydd.common;

import lombok.Getter;

public enum Page {
    LOGIN_PAGE("login_page"),
    USER_PAGE("user_page"),
    CHANGE_PASSWORD("change_password_page"),

    CREATE_SESSION("create_session"),

    ADD_USER_PAGE("create_user"),

    ADMINISTRATOR_PAGE("administrator_panel");

    @Getter private final String fileName;

    Page(String fileName) {
        this.fileName = fileName;
    }
}
