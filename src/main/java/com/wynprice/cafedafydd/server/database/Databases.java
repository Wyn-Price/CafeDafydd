package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static com.wynprice.cafedafydd.common.DatabaseStrings.*;

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

    private static final class UserDatabase extends Database {

        //The default values for the admin account. Should be generated if they don't exist
        private static final String ADMIN_USERNAME = "admin";
        private static final String ADMIN_PASSWORD_HASH = "0503cde860c5c486b73cfd25e05bb5c54f75d4107225215e52148a6270c0aa3c";
        private static final String ADMIN_EMAIL = "admin@cafedayfdd.com";

        private UserDatabase() {
            //If the admin credentials don't exist, generate them. (MAYBE THROW AN ERROR OR SOMETHING?)
            if(!this.generateIfNotPresent(Users.USERNAME, ADMIN_USERNAME, Users.PASSWORD_HASH, ADMIN_PASSWORD_HASH, Users.PERMISSION_LEVEL, PermissionLevel.ADMINISTRATOR.name(), Users.EMAIL, ADMIN_EMAIL)) {
                this.writeToFile();
            }
        }

        @Override
        protected String getFilename() {
            return Users.FILE_NAME;
        }

        @Override
        protected String[] getFields() {
            return new String[] {Users.USERNAME, Users.PASSWORD_HASH, Users.PERMISSION_LEVEL, Users.EMAIL};
        }

        @Override
        public String[] getPrimaryFields() {
            return new String[] { Users.USERNAME, Users.EMAIL };
        }

        @Override
        public boolean canEdit(DatabaseRecord record, PermissionLevel level) {
            return PermissionLevel.valueOf(record.getField(Users.PERMISSION_LEVEL)).getPermIndex() < level.getPermIndex();
        }
    }

    private static final class SessionsDatabase extends Database {

        @Override
        protected String getFilename() {
            return Sessions.FILE_NAME;
        }

        @Override
        protected String[] getFields() {
            return new String[]{ Sessions.USER_ID, Sessions.COMPUTER_ID, Sessions.ISO8601_START, Sessions.ISO8601_END };
        }

        @Override
        public String[] getPrimaryFields() {
            return new String[0];
        }
    }

    private static final class ComputersDatabase extends Database {

        @Override
        protected String getFilename() {
            return Computers.FILE_NAME;
        }

        @Override
        protected String[] getFields() {
            return new String[]{ Computers.OS, Computers.SESSION_ID };
        }

        @Override
        public PermissionLevel getEditLevel() {
            return PermissionLevel.ADMINISTRATOR;
        }
    }

    private static final Database[] DATABASES = new Database[]{ USERS, SESSIONS, COMPUTERS };

    public static Optional<Database> getFromFile(String fileName) {
        for (Database database : DATABASES) {
            if(fileName.equals(database.getFilename())) {
                return Optional.of(database);
            }
        }
        return Optional.empty();
    }
}
