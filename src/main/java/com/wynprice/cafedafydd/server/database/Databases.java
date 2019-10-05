package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static com.wynprice.cafedafydd.common.DatabaseStrings.*;
import static com.wynprice.cafedafydd.common.RecordEntry.stringRecord;

/**
 * All the database implementations
 */
@Log4j2
public abstract class Databases {

    private Databases() {
        //NO-OP
    }

    public static final Database USERS = new UserDatabase();
    public static final Database SESSIONS = new SessionsDatabase();
    public static final Database COMPUTERS = new ComputersDatabase();
    public static final Database GAMES = new GamesDatabase();

    private static final class UserDatabase extends Database {

        //The default values for the admin account. Should be generated if they don't exist
        private static final String ADMIN_USERNAME = "admin";
        private static final String ADMIN_PASSWORD_HASH = "0503cde860c5c486b73cfd25e05bb5c54f75d4107225215e52148a6270c0aa3c";
        private static final String ADMIN_EMAIL = "admin@cafedayfdd.com";

        private UserDatabase() {
            //If the admin credentials don't exist, generate them. (MAYBE THROW AN ERROR OR SOMETHING?)
            if(!this.generateIfNotPresent(
                NamedRecord.of(Users.USERNAME, stringRecord(ADMIN_USERNAME)),
                NamedRecord.of(Users.PASSWORD_HASH, stringRecord(ADMIN_PASSWORD_HASH)),
                NamedRecord.of(Users.PERMISSION_LEVEL, stringRecord(PermissionLevel.ADMINISTRATOR.name())),
                NamedRecord.of(Users.EMAIL, stringRecord(ADMIN_EMAIL)))) {
                this.writeToFile();
            }
        }

        @Override
        protected String getFilename() {
            return Users.FILE_NAME;
        }

        @Override
        protected Field[] getDefinition() {
            return new Field[] {
                Field.of(Users.USERNAME, RecordType.STRING),
                Field.of(Users.PASSWORD_HASH, RecordType.STRING),
                Field.of(Users.PERMISSION_LEVEL, RecordType.STRING),
                Field.of(Users.EMAIL, RecordType.STRING)
            };
        }

        @Override
        public String[] getPrimaryFields() {
            return new String[] { Users.USERNAME, Users.EMAIL };
        }

        @Override
        public boolean canEdit(DatabaseRecord record, int userID, PermissionLevel level) {
            return PermissionLevel.valueOf(record.getField(Users.PERMISSION_LEVEL).getAsString()).getPermIndex() < level.getPermIndex() || level == PermissionLevel.ADMINISTRATOR;
        }
    }

    private static final class SessionsDatabase extends Database {

        @Override
        protected String getFilename() {
            return Sessions.FILE_NAME;
        }

        @Override
        protected Field[] getDefinition() {
            return new Field[] {
                Field.of(Sessions.USER_ID, RecordType.INTEGER),
                Field.of(Sessions.COMPUTER_ID, RecordType.INTEGER),
                Field.of(Sessions.ISO8601_START, RecordType.DATE),
                Field.of(Sessions.ISO8601_END, RecordType.DATE),
                Field.of(Sessions.CALCULATED_PRICE, RecordType.FLOAT),
                Field.of(Sessions.PAID, RecordType.BOOLEAN),
            };
        }

        @Override
        public String[] getPrimaryFields() {
            return new String[0];
        }

        @Override
        public PermissionLevel getReadLevel() {
            return PermissionLevel.USER;
        }

        @Override
        public boolean canRead(DatabaseRecord record, int userID, PermissionLevel level) {
            return record.getField(Sessions.USER_ID).getAsInt() == userID || level.getPermIndex() >= PermissionLevel.STAFF_MEMBER.getPermIndex();
        }
    }

    private static final class ComputersDatabase extends Database {

        @Override
        protected String getFilename() {
            return Computers.FILE_NAME;
        }

        @Override
        protected Field[] getDefinition() {
            return new Field[] {
                Field.of(Computers.OS, RecordType.STRING),
                Field.of(Computers.SESSION_ID, RecordType.INTEGER),
                Field.of(Computers.PRICE_PER_HOUR, RecordType.FLOAT),
                Field.of(Computers.INSTALLED_GAMES, RecordType.ARRAY.apply(RecordType.INTEGER))
            };
        }

        @Override
        public PermissionLevel getEditLevel() {
            return PermissionLevel.ADMINISTRATOR;
        }

        @Override
        public PermissionLevel getReadLevel() {
            return PermissionLevel.USER;
        }
    }

    private static final class GamesDatabase extends Database {

        @Override
        protected String getFilename() {
            return Games.FILE_NAME;
        }

        @Override
        protected Field[] getDefinition() {
            return new Field[] {
                Field.of(Games.GAME_NAME, RecordType.STRING),
                Field.of(Games.GAME_RATING, RecordType.STRING),
            };
        }

        @Override
        public PermissionLevel getReadLevel() {
            return PermissionLevel.USER;
        }

        @Override
        public PermissionLevel getEditLevel() {
            return PermissionLevel.ADMINISTRATOR;
        }
    }

    private static final Database[] DATABASES = new Database[]{ GAMES, USERS, SESSIONS, COMPUTERS };

    public static Optional<Database> getFromFile(String fileName) {
        for (Database database : DATABASES) {
            if(fileName.equals(database.getFilename())) {
                return Optional.of(database);
            }
        }
        return Optional.empty();
    }
}
