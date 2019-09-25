package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.utils.DateUtils;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.*;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.*;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NetworkConsumer;
import com.wynprice.cafedafydd.common.utils.NetworkHandle;
import com.wynprice.cafedafydd.common.utils.NetworkHandleScanner;
import com.wynprice.cafedafydd.server.database.Database;
import com.wynprice.cafedafydd.server.database.Databases;
import com.wynprice.cafedafydd.server.utils.PermissionException;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Users;

@Log4j2
public class ServerNetworkHandler extends NetworkHandler {
    private static final NetworkConsumer CONSUMER = NetworkHandleScanner.generateConsumer(ServerNetworkHandler.class);
    private int userID = -1;
    private PermissionLevel permission;

    public ServerNetworkHandler() {
        super(CONSUMER);
    }

    @Override
    protected void handlePacket(Object packet) {
        try {
            super.handlePacket(packet);
        } catch (PermissionException perm) {
            this.sendPacket(new PacketDisplayError("Invalid perms", "Permissions are not sufficient to do task: '" + perm.getOperation() + "'. You have " + (this.permission != null ? this.permission.name() : "[NONE?]") + " and you need " + perm.getAtLeast()));
        }
    }

    @NetworkHandle
    public void handleLogin(PacketLogin login) {
        Optional<DatabaseRecord> entry = Databases.USERS.getSingleEntry(Users.USERNAME, login.getUsername(), Users.PASSWORD_HASH, login.getPasswordHash());
        if(!entry.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Credentials", "Username or Password is incorrect.\nPlease contact a member of staff to change your password"));
            return;
        }
        this.userID = entry.get().getPrimaryField();
        this.permission = PermissionLevel.valueOf(entry.get().getField(Users.PERMISSION_LEVEL));

        switch (this.permission) {
            case USER:
            case STAFF_MEMBER:
//            case ADMINISTRATOR:
                this.sendPacket(new PacketDisplayScreen(Page.USER_PAGE));
                break;
            case ADMINISTRATOR:
                this.sendPacket(new PacketDisplayScreen(Page.ADMINISTRATOR_PAGE));
                break;
        }
        this.sendPacket(new PacketConfirmLogin(entry.get().getField(Users.USERNAME)));
    }

    @NetworkHandle
    public void handleLogout(PacketLogout logout) {
        this.userID = -1;
        this.permission = null;
    }

    @NetworkHandle
    public void handleDatabaseRequest(PacketHasDatabaseEntry hasEntry) {
        Optional<Database> fromFile = Databases.getFromFile(hasEntry.getDatabaseFile());
        if(fromFile.isPresent()) {
            Database database = fromFile.get();
            this.ensurePerms(database.getReadLevel(), "Database Lookup");
            this.sendPacket(new PacketHasDatabaseEntryResult(hasEntry.getRequestID(), database.hasAllEntries(parseForm(hasEntry.getForm()))));
        } else {
            log.error("Requested database file " + hasEntry.getDatabaseFile() + " but it could not be found. ");
        }
    }

    @NetworkHandle
    public void handleCreateUser(PacketCreateUser createUser) {
        this.ensurePerms(PermissionLevel.STAFF_MEMBER, "Create User");
        if (Databases.USERS.hasAllEntries(Users.USERNAME, createUser.getUsername()) || Databases.USERS.hasAllEntries(Users.EMAIL, createUser.getEmail())) {
            this.sendPacket(new PacketDisplayError("Creation Error", "Username or email already exists in database."));
            return;
        }
        Databases.USERS.generateAndAddDatabase(Users.USERNAME, createUser.getUsername(), Users.EMAIL, createUser.getEmail(), Users.PASSWORD_HASH, createUser.getPasswordHash(), Users.PERMISSION_LEVEL, PermissionLevel.USER.name());
    }

    @NetworkHandle
    public void handleChangePassword(PacketChangePassword packet) {
        Optional<DatabaseRecord> entry = Databases.USERS.getEntryFromId(this.userID);
        if(!entry.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Entry-point", "Need to log in before you can change password"));
            return;
        }
        if(!entry.get().getField(Users.PASSWORD_HASH).equals(packet.getCurrentPasswordHash())) {
            this.sendPacket(new PacketDisplayError("Invalid Password", "Current password is incorrect.\nIf you have forgotten your password speak to a member of staff."));
            return;
        }
        entry.get().setField(Users.PASSWORD_HASH, packet.getNewPasswordHash());
        this.sendPacket(new PacketDisplayScreen(Page.USER_PAGE));

    }

    @NetworkHandle
    public void handleDatabaseEntriesRequest(PacketGetDatabaseEntries packet) {
        Optional<Database> database = Databases.getFromFile(packet.getDatabase());
        if(!database.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Database " + packet.getDatabase(), "Database " + packet.getDatabase() + " doesn't exist." ));
            return;
        }
        Database db = database.get();
        this.ensurePerms(db.getReadLevel(), "Database Lookup");
        this.sendPacket(
            new PacketDatabaseEntriesResult(packet.getType(), packet.getRequestID(), db.getFieldList(),
            (packet.getType().isSearch() ? db.searchEntries(parseForm(packet.getRequestForm())) : db.getEntries(parseForm(packet.getRequestForm()))).collect(Collectors.toList()))
        );
    }

    @NetworkHandle
    public void handleStartSession(PacketStartSession packet) {
        Optional<DatabaseRecord> entry = Databases.COMPUTERS.getEntryFromId(packet.getComputerID());
        if (entry.isPresent()) {
            DatabaseRecord record = Databases.SESSIONS.generateAndAddDatabase(
                DatabaseStrings.Sessions.USER_ID, String.valueOf(this.userID),
                DatabaseStrings.Sessions.COMPUTER_ID, String.valueOf(packet.getComputerID()),
                DatabaseStrings.Sessions.ISO8601_START, DateUtils.toISO8691(DateUtils.getCurrentDate()),
                DatabaseStrings.Sessions.ISO8601_END, DatabaseStrings.Computers.HASNT_ENDED
            );
            entry.get().setField(DatabaseStrings.Computers.SESSION_ID, String.valueOf(record.getPrimaryField()));
        } else {
            this.sendPacket(new PacketDisplayError("Invalid Computer ID", "Invalid Computer ID " + packet.getComputerID()));
        }
        this.sendPacket(new PacketDisplayScreen(Page.USER_PAGE));
    }

    @NetworkHandle
    public void handleCanStartSession(PacketCanStartSession packet) {
        //Find if there are any computers with no sessions, if there are, set the client to create session page, otherwise display an error
        if(Databases.COMPUTERS.getEntries().anyMatch(r -> "-1".equals(r.getField(DatabaseStrings.Computers.SESSION_ID)))) {
            this.sendPacket(new PacketDisplayScreen(Page.CREATE_SESSION));
        } else {
            this.sendPacket(new PacketDisplayError("Unable to start session", "There are no computers available :("));
        }
    }

    @NetworkHandle
    public void handleStopSession(PacketStopSession packet) {
        Optional<DatabaseRecord> entry = Databases.SESSIONS.getEntryFromId(packet.getSessionID());
        if(entry.isPresent()) {
            DatabaseRecord record = entry.get();
            record.setField(DatabaseStrings.Sessions.ISO8601_END, DateUtils.toISO8691(DateUtils.getCurrentDate()));
            //Log an error if there is not computer found? It *shouldn't* happen
            Databases.COMPUTERS.getEntryFromId(Integer.parseInt(record.getField(DatabaseStrings.Sessions.COMPUTER_ID))).ifPresent(r -> r.setField(DatabaseStrings.Computers.SESSION_ID, "-1"));

            this.sendPacket(new PacketCauseResync());
        } else {
            this.sendPacket(new PacketDisplayError("Unable to stop session", "Session with session id " + packet.getSessionID() + " does not exist"));
        }
    }

    @NetworkHandle
    public void handleTryEditDatabase(PacketTryEditDatabase packet) {
        Optional<Database> database = Databases.getFromFile(packet.getDatabase());
        if (database.isPresent()) {
            Database db = database.get();
            this.ensurePerms(db.getEditLevel(), "Editing Database " + packet.getDatabase());
            Optional<DatabaseRecord> record = database.flatMap(d -> d.getEntryFromId(packet.getRecordID()));
            if(record.isPresent()) {
                DatabaseRecord dr = record.get();
                if(!db.canEdit(dr, this.permission)) {
                    this.sendPacket(new PacketDisplayError("Unable Editing Database", "Cannot edit that record. Database prevented editing of it with permission " + this.permission));
                    return;
                }
                String[] form = packet.getForm();
                for (int i = 0; i < form.length; i+=2) {
                    boolean primary = Arrays.asList(db.getPrimaryFields()).contains(form[i]);
                    if(primary) {
                        Optional<DatabaseRecord> any = db.getEntries(form[i], form[i + 1]).filter(d -> d != dr).findAny();
                        if(any.isPresent()) {
                            this.sendPacket(new PacketDisplayError("Unable Editing Database", "Field '" + form[i] + "' is a primary field, but the set entry '" + form[i + 1] + "'\nalready exists in record " + any.get()));
                            return;
                        }
                    }
                    dr.setField(form[i], form[i+1]);
                }
            } else {
                this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find record with id " + packet.getRecordID() + " for database " + packet.getDatabase()));
            }
        } else {
            this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find database " + packet.getDatabase()));
        }
    }

    private String[] parseForm(String... form) {
        return Arrays.stream(form).map(s -> s.replaceAll("\\Q$$userid\\E", String.valueOf(this.userID))).toArray(String[]::new);
    }

    private void ensurePerms(PermissionLevel atLeast, String operation) {
        if(this.permission == null || this.permission.getPermIndex() < atLeast.getPermIndex()) {
            throw new PermissionException(atLeast, operation);
        }
    }
}
