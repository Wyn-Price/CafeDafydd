package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.*;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.*;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * The packet registry is where all the packets are registered. This is used to keep the
 * information about how packets are encoded and decoded.
 * @see PacketEntry
 */
//todo: remove enum instance and just go to static stuff?
public enum PacketRegistry {
    INSTANCE;

    /**
     * The list of entries. This contains all the information about all the packets
     */
    private List<PacketEntry<?>> entries = new ArrayList<>();

    /**
     * Register the packet
     * @param clazz the packet class to register
     * @param encoder the packet encoder
     * @param decoder the packet decoder
     * @param <T> the packet type
     */
    public <T> void registerPacket(Class<T> clazz, BiConsumer<T, ByteBuf> encoder, Function<ByteBuf, T> decoder) {
        this.entries.add(new PacketEntry<>(this.entries.size(), clazz, encoder, decoder));
    }

    /**
     * Gets the packet entry from the id
     * @param id the id to get the packet from
     * @return the packet associated with the id, or null if it doesn't exist
     */
    public PacketEntry getEntry(int id) {
        return this.entries.get(id);
    }

    /**
     * Get the packet entry
     * @param obj the packet object
     * @param <T> the packet type
     * @return the PacketEntry associated with the {@code obj}'s class
     * @throws IllegalArgumentException if the entry cannot be found
     */
    @SuppressWarnings("unchecked")
    public <T> PacketEntry<T> getEntry(T obj) {
        return (PacketEntry<T>) this.entries.stream().filter(e -> obj.getClass() == e.getClazz()).findAny().orElseThrow(() -> new IllegalArgumentException("Could not find packet registered with class " + obj.getClass()));
    }

    /**
     * Returns an empty decoder. Used for packets that don't send over any extra data
     * @param supplier the supplier to create the packet
     * @param <T> the packet type
     * @return the function to create the type {@code T}. Essentially just a function that returns {@code supplier}'s {@link Supplier#get()}
     */
    private static <T> Function<ByteBuf, T> emptyDecoder(Supplier<T> supplier) {
        return buff -> supplier.get();
    }

    /**
     * Returns an empty encoder. Used for packets that don't send over any extra data
     * @param <T> the packet type
     * @return and empty encoder. The consumer just doesn't do anything with the parameters.
     */
    private static <T> BiConsumer<T, ByteBuf> emptyEncoder() {
        return (o, buf) -> {};
    }

    static {
        //Register all the packets
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
