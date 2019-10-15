package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.search.SearchRequirement;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketTryEditDatabase {
    private final String database;
    private final int recordID;
    private final SearchRequirement[] form;

    public PacketTryEditDatabase(String database, int recordID, SearchRequirement... form) {
        this.database = database;
        this.recordID = recordID;
        this.form = form;
    }

    public static void encode(PacketTryEditDatabase packet, ByteBuf buf) {
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.recordID);
        SearchRequirement.write(packet.form, buf);
    }

    public static PacketTryEditDatabase decode(ByteBuf buf) {
        String db;
        return new PacketTryEditDatabase(
            db = ByteBufUtils.readString(buf),
            buf.readInt(),
            SearchRequirement.read(buf, db)
        );
    }
}
