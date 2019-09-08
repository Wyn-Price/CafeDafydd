package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketHasDatabaseEntry;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
public class DatabaseCheck {

    private static Consumer<Boolean> reciever;

    public static void checkDatabase(String databaseFile, String field, String testData, Consumer<Boolean> onRecieved) {
        if(reciever != null) {
            log.error("Requested a database check before the previous check has been done.", new IllegalArgumentException());
            return;
        }
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketHasDatabaseEntry(databaseFile, field, testData));
        reciever = onRecieved;
    }

    public static void receive(boolean value) {
        if(reciever != null) {
            reciever.accept(value);
            reciever = null;
        } else {
            log.error("Tried to receive result without a request?", new IllegalArgumentException());
        }
    }

}
