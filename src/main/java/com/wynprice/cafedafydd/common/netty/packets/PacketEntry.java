package com.wynprice.cafedafydd.common.netty.packets;

import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Packet Entries are used to hold together the packet index, class, encoder and decoder.
 * @param <T> the packet class
 */
@Value
public class PacketEntry<T> {
    private final int index;
    private final Class<T> clazz;
    private final BiConsumer<T, ByteBuf> encoder;
    private final Function<ByteBuf, T> decoder;
}
