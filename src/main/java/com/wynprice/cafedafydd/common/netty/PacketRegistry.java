package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.PacketConfirmLogin;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.PacketDisplayError;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.PacketDisplayScreen;
import com.wynprice.cafedafydd.common.netty.packets.clientbound.PacketHasDatabaseEntryResult;
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
        return (PacketEntry<T>) this.entries.stream().filter(e -> obj.getClass() == e.getClazz()).findAny().orElseThrow(() -> new IllegalArgumentException("Could not find packet registered with class " + obj.getClass() + " for state " + this.name()));
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
    }
}
