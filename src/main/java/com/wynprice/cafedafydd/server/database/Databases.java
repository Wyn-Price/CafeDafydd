package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.server.PermissionLevel;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class Databases {

    public static final Users USERS = new Users();

    public static class Users extends Database {

        private static final String ADMIN_USERNAME = "admin";
        private static final String ADMIN_PASSWORD_HASH = "0503cde860c5c486b73cfd25e05bb5c54f75d4107225215e52148a6270c0aa3c";

        public Users() {
            if(!this.hasAllEntries(USERNAME, ADMIN_USERNAME, PASSWORD_HASH, ADMIN_PASSWORD_HASH, PERMISSION_LEVEL, PermissionLevel.ADMINISTRATOR.name())) {
                this.generateAndAddDatabase(USERNAME, ADMIN_USERNAME, PASSWORD_HASH, ADMIN_PASSWORD_HASH, PERMISSION_LEVEL, PermissionLevel.ADMINISTRATOR.name());
                this.writeToFile();
            }
        }

        public static final String FILE_NAME = "users";

        public static final String USERNAME = "username";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String PERMISSION_LEVEL = "permission_level";

        @Override
        protected String getFilename() {
            return FILE_NAME;
        }

        @Override
        protected String[] getFields() {
            return new String[] {USERNAME, PASSWORD_HASH, PERMISSION_LEVEL};
        }
    }
}
