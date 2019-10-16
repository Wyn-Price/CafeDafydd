package com.wynprice.cafedafydd.common;

import lombok.Getter;

/**
 * An enum class containing all the pages that are used.
 *
 */
public enum Page {
    LOGIN_PAGE("login_page"),
    USER_PAGE("user_page"),
    CHANGE_PASSWORD("change_password_page"),

    CREATE_SESSION("create_session"),
    SEARCH_SESSION("search_sessions"),

    EDIT_USER_PAGE("edit_user"),
    SEARCH_USERS_PAGE("search_users"),

    STAFF_PANEL("staff_panel"),
    ADMINISTRATOR_PAGE("administrator_panel"),
    DIRECT_DATABASE_EDIT("database_direct_edit_page"),
    VIEW_BACKUPS("view_backups");

    @Getter private final String fileName;

    Page(String fileName) {
        this.fileName = fileName;
    }
}
