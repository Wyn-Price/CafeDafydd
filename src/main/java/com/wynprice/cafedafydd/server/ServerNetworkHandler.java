package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.utils.*;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.*;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.*;
import com.wynprice.cafedafydd.server.database.Database;
import com.wynprice.cafedafydd.server.database.Databases;
import com.wynprice.cafedafydd.common.entries.IntEntry;
import com.wynprice.cafedafydd.server.database.FileLineReader;
import com.wynprice.cafedafydd.server.utils.PermissionException;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Users;
import static com.wynprice.cafedafydd.common.RecordEntry.*;

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
        Optional<DatabaseRecord> entry = Databases.USERS.getEntries(NamedRecord.of(Users.USERNAME, stringRecord(login.getUsername())), NamedRecord.of(Users.PASSWORD_HASH, stringRecord(login.getPasswordHash()))).collect(UtilCollectors.toSingleEntry());
        if(!entry.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Credentials", "Username or Password is incorrect.\nPlease contact a member of staff to change your password"));
            return;
        }
        this.userID = entry.get().getPrimaryField();
        this.permission = PermissionLevel.getLevel(entry.get().getField(Users.PERMISSION_LEVEL).getAsString());

        switch (this.permission) {
            case USER:
                this.sendPacket(new PacketDisplayScreen(Page.USER_PAGE));
                break;
            case STAFF_MEMBER:
                this.sendPacket(new PacketDisplayScreen(Page.STAFF_PANEL));
                break;
            case ADMINISTRATOR:
                this.sendPacket(new PacketDisplayScreen(Page.ADMINISTRATOR_PAGE));
                break;
        }
        this.sendPacket(new PacketConfirmLogin(entry.get().getField(Users.USERNAME).getAsString()));
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
            this.ensurePerms(database.getReadLevel(), "Database Lookup " + hasEntry.getDatabaseFile());
            this.sendPacket(new PacketHasDatabaseEntryResult(hasEntry.getRequestID(), database.hasAllEntries(replaceFormUserId(hasEntry.getForm()))));
        } else {
            log.error("Requested database file " + hasEntry.getDatabaseFile() + " but it could not be found. ");
        }
    }

    @NetworkHandle
    public void handleCreateUser(PacketCreateUser packet) {
        this.ensurePerms(PermissionLevel.STAFF_MEMBER, "Create User");
        if (Databases.USERS.hasAllEntries(NamedRecord.of(Users.USERNAME, stringRecord(packet.getUsername()))) || Databases.USERS.hasAllEntries(NamedRecord.of(Users.EMAIL, stringRecord(packet.getEmail())))) {
            this.sendPacket(new PacketDisplayError("Creation Error", "Username or email already exists in database."));
            return;
        }
        Databases.USERS.generateAndAddDatabase(
            NamedRecord.of(Users.USERNAME, stringRecord(packet.getUsername())),
            NamedRecord.of(Users.EMAIL, stringRecord(packet.getEmail())),
            NamedRecord.of(Users.PASSWORD_HASH, stringRecord(packet.getPasswordHash())),
            NamedRecord.of(Users.PERMISSION_LEVEL, stringRecord(PermissionLevel.values()[packet.getPermissionCreatorLevel()].name())));
    }

    @NetworkHandle
    public void handleChangePassword(PacketChangePassword packet) {
        Optional<DatabaseRecord> entry = Databases.USERS.getEntryFromId(this.userID);
        if(!entry.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Entry-point", "Need to log in before you can change password"));
            return;
        }
        if(!entry.get().getField(Users.PASSWORD_HASH).getAsString().equals(packet.getCurrentPasswordHash())) {
            this.sendPacket(new PacketDisplayError("Invalid Password", "Current password is incorrect.\nIf you have forgotten your password speak to a member of staff."));
            return;
        }
        entry.get().setField(Users.PASSWORD_HASH, stringRecord(packet.getNewPasswordHash()));
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
        this.ensurePerms(db.getReadLevel(), "Database Lookup " + packet.getDatabase());
        this.sendPacket(
            new PacketDatabaseEntriesResult(packet.getType(), packet.getRequestID(), db.getFieldList(),
            (packet.getType().isSearch() ? db.searchEntries(replaceFormUserId(packet.getRequestForm())) : db.getEntries(replaceFormUserId(packet.getRequestForm())))
                .filter(r -> db.canRead(r, this.userID, this.permission)).collect(Collectors.toList()))
        );
    }

    @NetworkHandle
    public void handleStartSession(PacketStartSession packet) {
        Optional<DatabaseRecord> entry = Databases.COMPUTERS.getEntryFromId(packet.getComputerID());
        if (entry.isPresent()) {
            DatabaseRecord record = Databases.SESSIONS.generateAndAddDatabase(
                NamedRecord.of(DatabaseStrings.Sessions.USER_ID, intRecord(this.userID)),
                NamedRecord.of(DatabaseStrings.Sessions.COMPUTER_ID, intRecord(packet.getComputerID())),
                NamedRecord.of(DatabaseStrings.Sessions.ISO8601_START, dateRecord(DateUtils.getCurrentDate())),
                NamedRecord.of(DatabaseStrings.Sessions.ISO8601_END, dateRecord(DatabaseStrings.Sessions.HASNT_ENDED))
            );
            entry.get().setField(DatabaseStrings.Computers.SESSION_ID, intRecord(record.getPrimaryField()));
        } else {
            this.sendPacket(new PacketDisplayError("Invalid Computer ID", "Invalid Computer ID " + packet.getComputerID()));
        }
        this.sendPacket(new PacketDisplayScreen(Page.USER_PAGE));
    }

    @NetworkHandle
    public void handleCanStartSession(PacketCanStartSession packet) {
        //Find if there are any computers with no sessions, if there are, set the client to create session page, otherwise display an error
        if(Databases.COMPUTERS.getEntries(NamedRecord.of(DatabaseStrings.Computers.SESSION_ID, intRecord(-1))).count() > 0) {
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
            Date date = DateUtils.getCurrentDate();
            Date startDate = record.getField(DatabaseStrings.Sessions.ISO8601_START).getAsDate();

            //Calculate the price based on the hours spent on the session, and the price per hour.
            //Maybe log an error if there is not computer found? It *shouldn't* happen
            double price = Databases.COMPUTERS.getEntryFromId(record.getField(DatabaseStrings.Sessions.COMPUTER_ID).getAsInt()).map(r -> {
                r.setField(DatabaseStrings.Computers.SESSION_ID, intRecord(-1));
                //3.6e+6 -> How many milliseconds in an hour
                return r.getField(DatabaseStrings.Computers.PRICE_PER_HOUR).getAsFloat() * (date.getTime() - startDate.getTime()) / 3.6e+6D;
            }).orElse(-1D);

            record.setField(DatabaseStrings.Sessions.ISO8601_END, dateRecord(date));
            //the * 100 and / 100 is to get it to 2 decimal places
            record.setField(DatabaseStrings.Sessions.CALCULATED_PRICE, floatRecord(Math.round(price * 100F) / 100F));
            record.setField(DatabaseStrings.Sessions.PAID, boolRecord(false));

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
                if(!db.canEdit(dr, this.userID, this.permission)) {
                    this.sendPacket(new PacketDisplayError("Unable Editing Database", "Cannot edit that record. Database prevented editing of it with permission " + this.permission));
                    return;
                }
                NamedRecord[] form = this.replaceFormUserId(packet.getForm());
                for (NamedRecord namedRecord : form) {
                    boolean primary = ArrayUtils.asList(db.getPrimaryFields()).contains(namedRecord.getField());
                    if(primary) {
                        Optional<DatabaseRecord> any = db.getEntries(namedRecord).filter(d -> d != dr).findAny();
                        if(any.isPresent()) {
                            this.sendPacket(new PacketDisplayError("Unable Editing Database", "Field '" + namedRecord.getField() + "' is a primary field, but the set entry '" + namedRecord.getRecord() + "'\nalready exists in record " + any.get()));
                            return;
                        }
                    }
                    dr.setField(namedRecord.getField(), namedRecord.getRecord());
                }
                this.sendPacket(new PacketCauseResync());
            } else {
                this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find record with id " + packet.getRecordID() + " for database " + packet.getDatabase()));
            }
        } else {
            this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find database " + packet.getDatabase()));
        }
    }

    @NetworkHandle
    public void handleEditDatabaseField(PacketEditDatabaseField packet) {
        Optional<Database> database = Databases.getFromFile(packet.getDatabase());
        if (database.isPresent()) {
            Database db = database.get();
            this.ensurePerms(db.getEditLevel(), "Editing Database " + packet.getDatabase());
            Optional<DatabaseRecord> record = database.flatMap(d -> d.getEntryFromId(packet.getRecordID()));
            if(record.isPresent()) {
                DatabaseRecord dr = record.get();
                if(!db.canEdit(dr, this.userID, this.permission)) {
                    this.sendPacket(new PacketDisplayError("Unable Editing Database", "Cannot edit that record. Database prevented editing of it with permission " + this.permission));
                    return;
                }

                int schemaIndex = db.getFieldList().indexOf(packet.getField()) + 1;

                try {
                    RecordEntry newEntry = db.getSchema().getEntries()[schemaIndex].getEntry(new FileLineReader(packet.getNewValue()));
                    dr.setField(packet.getField(), newEntry);
                } catch (Exception e) {
                    this.sendPacket(new PacketDisplayError("Unable to edit database", "Error while setting field '" + packet.getField() + "' to value '" + packet.getNewValue() + "': \n" + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage() ));
                }


                this.sendPacket(new PacketCauseResync());
            } else {
                this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find record with id " + packet.getRecordID() + " for database " + packet.getDatabase()));
            }
        } else {
            this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find database " + packet.getDatabase()));
        }
    }

    @NetworkHandle
    public void editRecordDirect(PacketEditRecordDirect packet) {
        Optional<Database> database = Databases.getFromFile(packet.getDatabase());
        if (database.isPresent()) {
            Database db = database.get();
            this.ensurePerms(db.getEditLevel(), "Editing Database " + packet.getDatabase());
            if(packet.getRecordID() >= 0) {
                Optional<DatabaseRecord> record = db.getEntryFromId(packet.getRecordID());
                if(record.isPresent()) {
                    db.removeEntry(record.get());
                } else {
                    this.sendPacket(new PacketDisplayError("Unable to remove database record", "Could not find record with id " + packet.getRecordID() + " for database " + packet.getDatabase()));
                }
            } else {
                db.generateAndAddDatabase();
            }
            this.sendPacket(new PacketCauseResync());
        } else {
            this.sendPacket(new PacketDisplayError("Unable to edit database", "Could not find database " + packet.getDatabase()));
        }
    }

    private NamedRecord[] replaceFormUserId(NamedRecord... form) {
        return Arrays.stream(form)
            .map(f -> (f.getRecord() instanceof IntEntry && f.getRecord().getAsInt() == FormBuilder.USER_ID_REFERENCE ? NamedRecord.of(f.getField(), intRecord(this.userID)) : f))
            .toArray(NamedRecord[]::new);
    }

    private void ensurePerms(PermissionLevel atLeast, String operation) {
        if(this.permission == null || this.permission.getPermIndex() < atLeast.getPermIndex()) {
            throw new PermissionException(atLeast, operation);
        }
    }
}
