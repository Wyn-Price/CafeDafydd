package com.wynprice.cafedafydd.common.netty.packets;

import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Value
public class PacketEntry<T> {
    private final int index;
    private final Class<T> clazz;
    private final BiConsumer<T, ByteBuf> encoder;
    private final Function<ByteBuf, T> decoder;
}
