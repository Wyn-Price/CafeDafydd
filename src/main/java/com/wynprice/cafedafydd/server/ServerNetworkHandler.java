package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.Page;
import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketDisplayScreen;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketHasDatabaseEntryResult;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketHasDatabaseEntry;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogout;
import com.wynprice.cafedafydd.server.database.Database;
import com.wynprice.cafedafydd.server.database.Databases;
import lombok.extern.log4j.Log4j2;

import static com.wynprice.cafedafydd.common.DatabaseStrings.*;


import java.util.Optional;

@Log4j2
public class ServerNetworkHandler extends NetworkHandler {
    private int userID = -1;
    private PermissionLevel permission;

    @Override
    protected void handlePacket(Object packet) {
        if(packet instanceof PacketLogin) {
            PacketLogin login = (PacketLogin) packet;
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
            return;
        }
        if(packet instanceof PacketLogout) {
            this.userID = -1;
            this.permission = null;
            return;
        }
        if(packet instanceof PacketHasDatabaseEntry) {
            PacketHasDatabaseEntry hasEntry = (PacketHasDatabaseEntry) packet;
            Optional<Database> fromFile = Databases.getFromFile(hasEntry.getDatabaseFile());
            if(fromFile.isPresent()) {
                Database database = fromFile.get();
                this.sendPacket(new PacketHasDatabaseEntryResult(database.hasAllEntries(hasEntry.getField(), hasEntry.getTestData())));
            } else {
                log.error("Requested database file " + hasEntry.getDatabaseFile() + " but it could not be found. ");
            }
            return;
        }
    }
}
