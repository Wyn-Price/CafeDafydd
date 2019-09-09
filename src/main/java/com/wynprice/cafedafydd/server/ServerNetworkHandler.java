package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayScreen;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketHasDatabaseEntryResult;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketHasDatabaseEntry;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogout;
import com.wynprice.cafedafydd.common.utils.NetworkConsumer;
import com.wynprice.cafedafydd.common.utils.NetworkHandle;
import com.wynprice.cafedafydd.common.utils.NetworkHandleScanner;
import com.wynprice.cafedafydd.server.database.Database;
import com.wynprice.cafedafydd.server.database.Databases;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Users;

@Log4j2
public class ServerNetworkHandler extends NetworkHandler {
    private static final NetworkConsumer CONSUMER = NetworkHandleScanner.generate(ServerNetworkHandler.class);
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
            this.sendPacket(new PacketDisplayError("Invalid perms", "Permissions are not sufficient. You have " + this.permission.name() + " and you need " + perm.getAtLeast()));
        }
    }

    @NetworkHandle
    public void handleLogin(PacketLogin login) {
        Optional<Integer> entry = Databases.USERS.getSingleIdFromEntry(Users.USERNAME, login.getUsername(), Users.PASSWORD_HASH, login.getPasswordHash());
        if(!entry.isPresent()) {
            this.sendPacket(new PacketDisplayError("Invalid Credentials", "Username or Password is incorrect.\nPlease contact a member of staff to change your password"));
            return;
        }
        this.userID = entry.get();
        this.permission = PermissionLevel.valueOf(Databases.USERS.getEntryFromId(this.userID).orElseThrow(NullPointerException::new).getField(Users.PERMISSION_LEVEL));
        Databases.USERS.writeToFile();

        if (this.permission == PermissionLevel.ADMINISTRATOR) {
            this.sendPacket(new PacketDisplayScreen(Page.ADMINISTRATOR_PAGE));
        }
    }

    @NetworkHandle
    public void handleLogout(PacketLogout logout) {
        this.userID = -1;
        this.permission = null;
    }

    @NetworkHandle
    public void handleDatabaseRequest(PacketHasDatabaseEntry hasEntry) {
        this.ensurePerms(PermissionLevel.STAFF_MEMBER);
        Optional<Database> fromFile = Databases.getFromFile(hasEntry.getDatabaseFile());
        if(fromFile.isPresent()) {
            Database database = fromFile.get();
            this.sendPacket(new PacketHasDatabaseEntryResult(database.hasAllEntries(hasEntry.getField(), hasEntry.getTestData())));
        } else {
            log.error("Requested database file " + hasEntry.getDatabaseFile() + " but it could not be found. ");
        }
    }

    private void ensurePerms(PermissionLevel atLeast) {
        if(this.permission.getPerIndex() < atLeast.getPerIndex()) {
            throw new PermissionException(atLeast);
        }
    }
}
