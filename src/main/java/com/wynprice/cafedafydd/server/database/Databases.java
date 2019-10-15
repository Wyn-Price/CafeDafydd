package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.FieldDefinition;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static com.wynprice.cafedafydd.common.FieldDefinitions.*;

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
                Users.USERNAME.create(ADMIN_USERNAME),
               Users.PASSWORD_HASH.create(ADMIN_PASSWORD_HASH),
               Users.PERMISSION_LEVEL.create(PermissionLevel.ADMINISTRATOR.name()),
               Users.EMAIL.create(ADMIN_EMAIL))) {
                this.writeToFile();
            }
        }

        @Override
        protected String getFilename() {
            return Users.FILE_NAME;
        }

        @Override
        public FieldDefinition[] createDefinition() {
            return new FieldDefinition[] {
                Users.USERNAME,
                Users.PASSWORD_HASH,
                Users.PERMISSION_LEVEL,
                Users.EMAIL
            };
        }

        @Override
        public FieldDefinition[] getPrimaryFields() {
            return new FieldDefinition[] { Users.USERNAME, Users.EMAIL };
        }

        @Override
        public boolean canEdit(DatabaseRecord record, int userID, PermissionLevel level) {
            return PermissionLevel.getLevel(record.get(Users.PERMISSION_LEVEL)).getPermIndex() < level.getPermIndex() || level == PermissionLevel.ADMINISTRATOR;
        }
    }

    private static final class SessionsDatabase extends Database {

        @Override
        protected String getFilename() {
            return Sessions.FILE_NAME;
        }

        @Override
        protected FieldDefinition[] createDefinition() {
            return new FieldDefinition[] {
                Sessions.USER_ID,
                Sessions.COMPUTER_ID,
                Sessions.ISO8601_START,
                Sessions.ISO8601_END,
                Sessions.CALCULATED_PRICE,
                Sessions.PAID
            };
        }

        @Override
        public PermissionLevel getReadLevel() {
            return PermissionLevel.USER;
        }

        @Override
        public boolean canRead(DatabaseRecord record, int userID, PermissionLevel level) {
            return record.get(Sessions.USER_ID) == userID || level.getPermIndex() >= PermissionLevel.STAFF_MEMBER.getPermIndex();
        }
    }

    private static final class ComputersDatabase extends Database {

        @Override
        protected String getFilename() {
            return Computers.FILE_NAME;
        }

        @Override
        protected FieldDefinition[] createDefinition() {
            return new FieldDefinition[] {
                Computers.OS,
                Computers.SESSION_ID,
                Computers.PRICE_PER_HOUR,
                Computers.INSTALLED_GAMES
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
        protected FieldDefinition[] createDefinition() {
            return new FieldDefinition[] {
                Games.GAME_NAME,
                Games.GAME_RATING,
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
