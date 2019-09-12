package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.stream.IntStream;

@Value
public class PacketHasDatabaseEntry {
    private final int requestID;
    private final String databaseFile;
    private final String[] form;

    public static void encode(PacketHasDatabaseEntry packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.databaseFile, buf);

        buf.writeShort(packet.form.length);
        for (String s : packet.form) {
            ByteBufUtils.writeString(s, buf);
        }
    }

    public static PacketHasDatabaseEntry decode(ByteBuf buf) {
        return new PacketHasDatabaseEntry(buf.readInt(), ByteBufUtils.readString(buf), IntStream.range(0, buf.readShort()).mapToObj(i -> ByteBufUtils.readString(buf)).toArray(String[]::new));
    }
}

