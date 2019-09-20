package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.*;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public enum PacketRegistry {
    INSTANCE;

    private List<PacketEntry<?>> entries = new ArrayList<>();

    public <T> void registerPacket(Class<T> clazz, BiConsumer<T, ByteBuf> encoder, Function<ByteBuf, T> decoder) {
        this.entries.add(new PacketEntry<>(this.entries.size(), clazz, encoder, decoder));
    }

    public PacketEntry getEntry(int id) {
        return this.entries.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> PacketEntry<T> getEntry(T obj) {
        return (PacketEntry<T>) this.entries.stream().filter(e -> obj.getClass() == e.getClazz()).findAny().orElseThrow(() -> new IllegalArgumentException("Could not find packet registered with class " + obj.getClass()));
    }

    private static <T> Function<ByteBuf, T> emptyDecoder(Supplier<T> supplier) {
        return buff -> supplier.get();
    }

    private static <T> BiConsumer<T, ByteBuf> emptyEncoder() {
        return (o, buf) -> {};
    }

    static {
        INSTANCE.registerPacket(PacketLogin.class, PacketLogin::encode, PacketLogin::decode);
        INSTANCE.registerPacket(PacketDisplayError.class, PacketDisplayError::encode, PacketDisplayError::decode);
        INSTANCE.registerPacket(PacketLogout.class, emptyEncoder(), emptyDecoder(PacketLogout::new));
        INSTANCE.registerPacket(PacketDisplayScreen.class, PacketDisplayScreen::encode, PacketDisplayScreen::decode);
        INSTANCE.registerPacket(PacketHasDatabaseEntry.class, PacketHasDatabaseEntry::encode, PacketHasDatabaseEntry::decode);
        INSTANCE.registerPacket(PacketHasDatabaseEntryResult.class, PacketHasDatabaseEntryResult::encode, PacketHasDatabaseEntryResult::decode);
        INSTANCE.registerPacket(PacketCreateUser.class, PacketCreateUser::encode, PacketCreateUser::decode);
        INSTANCE.registerPacket(PacketConfirmLogin.class, PacketConfirmLogin::encode, PacketConfirmLogin::decode);
        INSTANCE.registerPacket(PacketChangePassword.class, PacketChangePassword::encode, PacketChangePassword::decode);
        INSTANCE.registerPacket(PacketGetDatabaseEntries.class, PacketGetDatabaseEntries::encode, PacketGetDatabaseEntries::decode);
        INSTANCE.registerPacket(PacketDatabaseEntriesResult.class, PacketDatabaseEntriesResult::encode, PacketDatabaseEntriesResult::decode);
        INSTANCE.registerPacket(PacketStartSession.class, PacketStartSession::encode, PacketStartSession::decode);
        INSTANCE.registerPacket(PacketCanStartSession.class, emptyEncoder(), emptyDecoder(PacketCanStartSession::new));
        INSTANCE.registerPacket(PacketStopSession.class, PacketStopSession::encode, PacketStopSession::decode);
        INSTANCE.registerPacket(PacketCauseResync.class, emptyEncoder(), emptyDecoder(PacketCauseResync::new));
        INSTANCE.registerPacket(PacketTryEditDatabase.class, PacketTryEditDatabase::encode, PacketTryEditDatabase::decode);
    }
}
