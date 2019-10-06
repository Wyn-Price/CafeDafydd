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
public class PacketRegistry {

    /**
     * The list of entries. This contains all the information about all the packets
     */
    private static List<PacketEntry<?>> entries = new ArrayList<>();

    /**
     * Register the packet
     * @param clazz the packet class to register
     * @param encoder the packet encoder
     * @param decoder the packet decoder
     * @param <T> the packet type
     */
    public static <T> void registerPacket(Class<T> clazz, BiConsumer<T, ByteBuf> encoder, Function<ByteBuf, T> decoder) {
        entries.add(new PacketEntry<>(entries.size(), clazz, encoder, decoder));
    }

    /**
     * Gets the packet entry from the id
     * @param id the id to get the packet from
     * @return the packet associated with the id, or null if it doesn't exist
     */
    public static PacketEntry getEntry(int id) {
        return entries.get(id);
    }

    /**
     * Get the packet entry
     * @param obj the packet object
     * @param <T> the packet type
     * @return the PacketEntry associated with the {@code obj}'s class
     * @throws IllegalArgumentException if the entry cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T> PacketEntry<T> getEntry(T obj) {
        return (PacketEntry<T>) entries.stream().filter(e -> obj.getClass() == e.getClazz()).findAny().orElseThrow(() -> new IllegalArgumentException("Could not find packet registered with class " + obj.getClass()));
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
        registerPacket(PacketLogin.class, PacketLogin::encode, PacketLogin::decode);
        registerPacket(PacketDisplayError.class, PacketDisplayError::encode, PacketDisplayError::decode);
        registerPacket(PacketLogout.class, emptyEncoder(), emptyDecoder(PacketLogout::new));
        registerPacket(PacketDisplayScreen.class, PacketDisplayScreen::encode, PacketDisplayScreen::decode);
        registerPacket(PacketHasDatabaseEntry.class, PacketHasDatabaseEntry::encode, PacketHasDatabaseEntry::decode);
        registerPacket(PacketHasDatabaseEntryResult.class, PacketHasDatabaseEntryResult::encode, PacketHasDatabaseEntryResult::decode);
        registerPacket(PacketCreateUser.class, PacketCreateUser::encode, PacketCreateUser::decode);
        registerPacket(PacketConfirmLogin.class, PacketConfirmLogin::encode, PacketConfirmLogin::decode);
        registerPacket(PacketChangePassword.class, PacketChangePassword::encode, PacketChangePassword::decode);
        registerPacket(PacketGetDatabaseEntries.class, PacketGetDatabaseEntries::encode, PacketGetDatabaseEntries::decode);
        registerPacket(PacketDatabaseEntriesResult.class, PacketDatabaseEntriesResult::encode, PacketDatabaseEntriesResult::decode);
        registerPacket(PacketStartSession.class, PacketStartSession::encode, PacketStartSession::decode);
        registerPacket(PacketCanStartSession.class, emptyEncoder(), emptyDecoder(PacketCanStartSession::new));
        registerPacket(PacketStopSession.class, PacketStopSession::encode, PacketStopSession::decode);
        registerPacket(PacketCauseResync.class, emptyEncoder(), emptyDecoder(PacketCauseResync::new));
        registerPacket(PacketTryEditDatabase.class, PacketTryEditDatabase::encode, PacketTryEditDatabase::decode);
        registerPacket(PacketEditDatabaseField.class, PacketEditDatabaseField::encode, PacketEditDatabaseField::decode);
        registerPacket(PacketEditRecordDirect.class, PacketEditRecordDirect::encode, PacketEditRecordDirect::decode);
    }
}
