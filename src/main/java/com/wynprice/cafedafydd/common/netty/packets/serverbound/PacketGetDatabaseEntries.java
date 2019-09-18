package com.wynprice.cafedafydd.common.netty.packets.serverbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.RequestType;
import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class PacketGetDatabaseEntries {
    private final RequestType type;
    private final int requestID;
    private final String database;
    private final String[] requestForm;

    public static void encode(PacketGetDatabaseEntries packet, ByteBuf buf) {
        buf.writeShort(packet.type.ordinal());
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.database, buf);
        buf.writeInt(packet.requestForm.length);
        for (String s : packet.requestForm) {
            ByteBufUtils.writeString(s, buf);
        }
    }

    public static PacketGetDatabaseEntries decode(ByteBuf buf) {
        RequestType type = RequestType.values()[buf.readShort() % RequestType.values().length];
        int id = buf.readInt();
        String database = ByteBufUtils.readString(buf);
        String[] form = new String[buf.readInt()];
        for (int i = 0; i < form.length; i++) {
            form[i] = ByteBufUtils.readString(buf);
        }
        return new PacketGetDatabaseEntries(type, id, database, form);
    }

}
