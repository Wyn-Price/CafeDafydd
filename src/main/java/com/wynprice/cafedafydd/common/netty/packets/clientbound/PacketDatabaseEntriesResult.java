package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
public class PacketDatabaseEntriesResult {
    private final int requestID;
    private final List<String> fields;
    private final List<DatabaseRecord> records;

    public static void encode(PacketDatabaseEntriesResult packet, ByteBuf buf) {

        System.out.println(buf);

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


        System.out.println(buf);

    }

    public static PacketDatabaseEntriesResult decode(ByteBuf buf) {
        System.out.println(buf);

        int requestId = buf.readInt();

        short l = buf.readShort();
        List<String> fields = IntStream.range(0, l)
            .mapToObj($ -> {
                String s = ByteBufUtils.readString(buf);
                return s;
            })
            .collect(Collectors.toList());

        l = buf.readShort();
        List<DatabaseRecord> records = IntStream.range(0, l)
            .mapToObj($ -> {
                int i =  buf.readInt();
                return new DatabaseRecord(fields, i,
                    IntStream.range(0, fields.size())
                        .mapToObj($$ -> {
                            String s = ByteBufUtils.readString(buf);
                            return s;
                        })
                        .toArray(String[]::new));

            }).collect(Collectors.toList());
        return new PacketDatabaseEntriesResult(requestId, fields, records);

    }

}

