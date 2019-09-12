package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
public class PacketDatabaseEntriesResult {
    private final int requestID;
    private final List<String> fields;
    private final List<DatabaseRecord> records;

    public static void encode(PacketDatabaseEntriesResult packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);
        buf.writeShort(packet.fields.size());
        for (String field : packet.fields) {
            ByteBufUtils.writeString(field, buf);
        }

        buf.writeShort(packet.records.size());
        for (DatabaseRecord record : packet.records) {
            buf.writeInt(record.getPrimaryField());
            for (String entry : record.getEntries()) {
                ByteBufUtils.writeString(entry, buf);
            }
        }
    }

    public static PacketDatabaseEntriesResult decode(ByteBuf buf) {
        int requestId = buf.readInt();

        List<String> fields = IntStream.range(0, buf.readShort())
            .mapToObj($ -> ByteBufUtils.readString(buf))
            .collect(Collectors.toList());

        List<DatabaseRecord> records = IntStream.range(0, buf.readShort())
            .mapToObj($ -> new DatabaseRecord(fields, buf.readInt(),
                IntStream.range(0, fields.size())
                    .mapToObj($$ -> ByteBufUtils.readString(buf))
                    .toArray(String[]::new))
            ).collect(Collectors.toList());

        return new PacketDatabaseEntriesResult(requestId, fields, records);

    }

}

