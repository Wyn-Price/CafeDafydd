package com.wynprice.cafedafydd.common;

import lombok.Getter;

public enum Page {
    LOGIN_PAGE("login_page"),
    USER_PAGE("user_page"),
    CHANGE_PASSWORD("change_password_page"),

    CREATE_SESSION("create_session"),

    EDIT_USER_PAGE("edit_user"),
    SEARCH_USERS_PAGE("search_users"),

    ADMINISTRATOR_PAGE("administrator_panel");

    @Getter private final String fileName;

    Page(String fileName) {
        this.fileName = fileName;
    }
}
