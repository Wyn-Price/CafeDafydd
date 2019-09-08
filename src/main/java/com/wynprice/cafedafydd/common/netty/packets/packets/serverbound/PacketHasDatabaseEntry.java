package com.wynprice.cafedafydd.common.netty.packets.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketHasDatabaseEntry {
    private final String databaseFile;
    private final String field;
    private final String testData;

    public static void encode(PacketHasDatabaseEntry packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.databaseFile, buf);
        ByteBufUtils.writeString(packet.field, buf);
        ByteBufUtils.writeString(packet.testData, buf);
    }

    public static PacketHasDatabaseEntry decode(ByteBuf buf) {
        return new PacketHasDatabaseEntry(ByteBufUtils.readString(buf), ByteBufUtils.readString(buf), ByteBufUtils.readString(buf));
    }
}

