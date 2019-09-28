package com.wynprice.cafedafydd.common;

/**
 * A class containing all the database strings that are used in the databases.
 */
public class DatabaseStrings {

    public static final String ID = "record_id";
    public static final String NOT_PREFIX = "$$NOT$$";
    public static final String INLINE_REQUEST_PREFIX = "$$INLINEREQUEST$$";

    public static class Users {
        public static final String FILE_NAME = "users";

        public static final String USERNAME = "username";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String PERMISSION_LEVEL = "permission_level";
        public static final String EMAIL = "email";
    }

    public static class Sessions {
        public static final String FILE_NAME = "sessions";

        public static final String USER_ID = "user_id";
        public static final String COMPUTER_ID = "computer_id";
        public static final String ISO8601_START = "startime";
        public static final String ISO8601_END = "endtime";
        public static final String CALCULATED_PRICE = "price";
        public static final String PAID = "has_paid";
    }

    public static class Computers {
        public static final String FILE_NAME = "computers";

        public static final String OS = "os";
        public static final String SESSION_ID = "session_id";

        public static final String HASNT_ENDED = "notend";
    }

}
