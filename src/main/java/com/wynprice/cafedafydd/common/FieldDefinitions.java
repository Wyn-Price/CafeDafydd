package com.wynprice.cafedafydd.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.wynprice.cafedafydd.common.RecordType.*;

public class FieldDefinitions {

    public static final FieldDefinition<Integer> ID = FieldDefinition.of("record_id", INTEGER_TYPE);

    public static class Users {
        public static final String FILE_NAME = "users";

        public static final FieldDefinition<String> USERNAME =  FieldDefinition.of("username", STRING_TYPE);
        public static final FieldDefinition<String> PASSWORD_HASH =  FieldDefinition.of("password_hash", STRING_TYPE);
        public static final FieldDefinition<String> PERMISSION_LEVEL =  FieldDefinition.of("permission_level", STRING_TYPE);
        public static final FieldDefinition<String> EMAIL =  FieldDefinition.of("email", STRING_TYPE);

        public static final FieldDefinition[] SCHEMA = { USERNAME, PASSWORD_HASH, PERMISSION_LEVEL, EMAIL };
    }

    public static class Sessions {
        public static final String FILE_NAME = "sessions";

        public static final FieldDefinition<Integer> USER_ID =  FieldDefinition.of("user_id", INTEGER_TYPE);
        public static final FieldDefinition<Integer> COMPUTER_ID =  FieldDefinition.of("computer_id", INTEGER_TYPE);
        public static final FieldDefinition<Date> ISO8601_START =  FieldDefinition.of("startime", DATE_TYPE);
        public static final FieldDefinition<Date> ISO8601_END =  FieldDefinition.of("endtime", DATE_TYPE);
        public static final FieldDefinition<Float> CALCULATED_PRICE =  FieldDefinition.of("price", FLOAT_TYPE);
        public static final FieldDefinition<Boolean> PAID =  FieldDefinition.of("has_paid", BOOLEAN_TYPE);

        public static final FieldDefinition[] SCHEMA = { USER_ID, COMPUTER_ID, ISO8601_START, ISO8601_END, CALCULATED_PRICE, PAID };
    }

    public static class Computers {
        public static final String FILE_NAME = "computers";

        public static final FieldDefinition<String> OS = FieldDefinition.of("os", STRING_TYPE);
        public static final FieldDefinition<Integer> SESSION_ID = FieldDefinition.of("session_id", INTEGER_TYPE);
        public static final FieldDefinition<Float> PRICE_PER_HOUR = FieldDefinition.of("pph", FLOAT_TYPE);
        public static final FieldDefinition<Integer[]> INSTALLED_GAMES = FieldDefinition.of("installed_games", createArray(INTEGER_TYPE));

        public static final FieldDefinition[] SCHEMA = { OS, SESSION_ID, PRICE_PER_HOUR, INSTALLED_GAMES };

    }

    public static class Games {
        public static final String FILE_NAME = "games";

        public static final FieldDefinition<String> GAME_NAME = FieldDefinition.of("game_name", STRING_TYPE);
        public static final FieldDefinition<String> GAME_RATING = FieldDefinition.of("game_rating", STRING_TYPE);

        public static final FieldDefinition[] SCHEMA = { GAME_NAME, GAME_RATING };
    }

    public static final Map<String, FieldDefinition[]> NAME_TO_SCHEMA = new HashMap<>();

    static {
        NAME_TO_SCHEMA.put(Users.FILE_NAME, Users.SCHEMA);
        NAME_TO_SCHEMA.put(Sessions.FILE_NAME, Sessions.SCHEMA);
        NAME_TO_SCHEMA.put(Computers.FILE_NAME, Computers.SCHEMA);
        NAME_TO_SCHEMA.put(Games.FILE_NAME, Games.SCHEMA);
    }
}
