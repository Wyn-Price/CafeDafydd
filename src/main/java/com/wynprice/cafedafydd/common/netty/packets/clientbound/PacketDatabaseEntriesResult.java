package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.DatabaseField;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import com.wynprice.cafedafydd.common.utils.RequestType;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
public class PacketDatabaseEntriesResult {
    private final RequestType type;
    private final int requestID;
    private final String database;
    private final List<DatabaseRecord> entries;

    public static void encode(PacketDatabaseEntriesResult packet, ByteBuf buf) {
        buf.writeShort(packet.type.ordinal());
        buf.writeInt(packet.requestID);
        ByteBufUtils.writeString(packet.database, buf);

        buf.writeShort(packet.entries.size());

        for (DatabaseRecord entry : packet.entries) {
            buf.writeInt(entry.getPrimaryField());
            for (NamedRecord entryEntry : entry.getEntries()) {
                entryEntry.getRecord().serialize(buf);
            }
        }
    }

    public static PacketDatabaseEntriesResult decode(ByteBuf buf) {
        String database;
        return new PacketDatabaseEntriesResult(
            RequestType.values()[buf.readShort() % RequestType.values().length],
            buf.readInt(),
            database = ByteBufUtils.readString(buf),
            IntStream.range(0, buf.readShort())
                .mapToObj(value ->
                    new DatabaseRecord(
                        buf.readInt(),
                        FieldDefinitions.NAME_TO_SCHEMA.get(database),
                        Arrays.stream(FieldDefinitions.NAME_TO_SCHEMA.get(database)).map(definition -> definition.getRecordType().createEmpty().deserialize(buf)).toArray(DatabaseField[]::new)
                    )
                ).collect(Collectors.toList())
        );

    }


}

