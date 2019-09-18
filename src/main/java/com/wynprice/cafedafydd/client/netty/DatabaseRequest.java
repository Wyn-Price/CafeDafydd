package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketGetDatabaseEntries;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketHasDatabaseEntry;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.RequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
public class DatabaseRequest {

    public static final RequestForm<Boolean> HAS_ENTRY = new RequestForm<>(PacketHasDatabaseEntry::new);
    public static final RequestForm<List<DatabaseRecord>> GET_ENTRIES = new RequestForm<>((r, d, f) -> new PacketGetDatabaseEntries(RequestType.GET, r, d, f));
    public static final RequestForm<List<DatabaseRecord>> SEARCH_ENTRIES = new RequestForm<>((r, d, f) -> new PacketGetDatabaseEntries(RequestType.SEARCH, r, d, f));


    @RequiredArgsConstructor
    public static class RequestForm<D> {

        private final PacketCreation creation;

        private int requests = 0;
        private final Map<Integer, Consumer<D>> storage = new HashMap<>();

        public void sendRequest(String databaseFile, Consumer<D> reciever, String... form) {
            int id = this.requests++;
            this.storage.put(id, reciever);
            CafeDafyddMain.getClient().getHandler().sendPacket(this.creation.createPacket(id, databaseFile, form));
        }

        public void receive(int requestID, D value) {
            Consumer<D> removed = this.storage.remove(requestID);
            if(removed != null) {
                removed.accept(value);
            } else {
                log.error(new IllegalArgumentException("Could not find request with id " + requestID));
            }
        }

    }

    private interface PacketCreation<P> {
        P createPacket(int requestID, String databaseFile, String... form);
    }
}
