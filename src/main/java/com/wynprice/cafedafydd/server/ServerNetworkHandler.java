package com.wynprice.cafedafydd.server;

import com.wynprice.cafedafydd.common.netty.NetworkHandler;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketLoginIncorrect;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
import com.wynprice.cafedafydd.server.database.Databases;

import java.util.Optional;
import java.util.OptionalInt;

public class ServerNetworkHandler extends NetworkHandler {
    private int userID;
    private String username;

    @Override
    protected void handlePacket(Object packet) {
        if(packet instanceof PacketLogin) {
            PacketLogin login = (PacketLogin) packet;
            Optional<Integer> entry = Databases.USERS.getSingleIdFromEntry(Databases.Users.USERNAME, login.getUsername(), Databases.Users.PASSWORD_HASH, login.getPasswordHash());
            if(!entry.isPresent()) {
                this.sendPacket(new PacketLoginIncorrect("Username or Password were incorrect."));
                return;
            }
            this.userID = entry.get();
            this.username = login.getUsername();
            Databases.USERS.writeToFile();
        }
    }
}
