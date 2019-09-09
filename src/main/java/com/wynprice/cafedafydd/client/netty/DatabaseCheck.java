package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketHasDatabaseEntry;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
public class DatabaseCheck {

    private static int requests = 0;

    private static final Map<Integer, Consumer<Boolean>> requestStorage = new HashMap<>();

    public static void checkDatabase(String databaseFile, String field, String testData, Consumer<Boolean> onRecieved) {
        int id = requests++;
        requestStorage.put(id, onRecieved);
        CafeDafyddMain.getClient().getHandler().sendPacket(new PacketHasDatabaseEntry(id, databaseFile, field, testData));
    }

    public static void receive(int requestID, boolean value) {
        Consumer<Boolean> removed = requestStorage.remove(requestID);
        if(removed != null) {
            removed.accept(value);
        } else {
            log.error(new IllegalArgumentException("Could not find request with id " + requestID));
        }
    }

}
