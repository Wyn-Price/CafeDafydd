package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.search.SearchRequirement;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketHasDatabaseEntry {
    private final int requestID;
    private final String databaseFile;
    private final SearchRequirement[] form;

    public static void encode(PacketHasDatabaseEntry packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.databaseFile, buf);
        SearchRequirement.write(packet.form, buf);
    }

    public static PacketHasDatabaseEntry decode(ByteBuf buf) {
        String db;
        return new PacketHasDatabaseEntry(buf.readInt(), db = ByteBufUtils.readString(buf), SearchRequirement.read(buf, db));
    }
}

