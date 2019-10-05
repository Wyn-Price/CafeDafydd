package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.IntStream;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class NamedRecord  {
    private final String field;
    private final RecordEntry record;

    public static void write(NamedRecord[] records, ByteBuf buf) {
        buf.writeShort(records.length);
        for (NamedRecord record : records) {
            ByteBufUtils.writeString(record.getField(), buf);
            buf.writeByte(record.record.getId());
            record.getRecord().serialize(buf);
        }
    }

    public static NamedRecord[] read(ByteBuf buf) {
        return IntStream.range(0, buf.readShort()).mapToObj(i -> of(ByteBufUtils.readString(buf), RecordEntry.createNew(buf.readByte()).deserialize(buf))).toArray(NamedRecord[]::new);
    }
}
