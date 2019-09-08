package com.wynprice.cafedafydd.common.netty;

import com.wynprice.cafedafydd.common.netty.packets.PacketEntry;
import com.wynprice.cafedafydd.common.netty.packets.packets.clientbound.PacketLoginIncorrect;
import com.wynprice.cafedafydd.common.netty.packets.packets.serverbound.PacketLogin;
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
        INSTANCE.registerPacket(PacketLoginIncorrect.class, PacketLoginIncorrect::encode, PacketLoginIncorrect::decode);
    }
}
