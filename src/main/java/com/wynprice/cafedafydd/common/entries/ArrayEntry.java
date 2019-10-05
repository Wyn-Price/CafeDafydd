package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

import java.util.stream.IntStream;

@EqualsAndHashCode
public class ArrayEntry implements RecordEntry {
    private RecordEntry[] entry;

    @Override
    public RecordEntry[] getAsArray() {
        return this.entry;
    }

    @Override
    public ArrayEntry setArray(RecordEntry[] array) {
        this.entry = array;
        return this;
    }

    @Override
    public CharSequence getAsFileString() {
        StringBuilder builder = new StringBuilder();

        builder.append('[');

        for (int i = 0; i < this.entry.length; i++) {
            if(i != 0) {
                builder.append(',');
            }
            builder.append(this.entry[i].getAsFileString());
        }
        builder.append(']');

        return builder;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.entry.length);
        for (RecordEntry entry : this.entry) {
            buf.writeByte(entry.getId());
            entry.serialize(buf);
        }
    }

    @Override
    public ArrayEntry deserialize(ByteBuf buf) {
        this.entry = IntStream.range(0, buf.readShort()).mapToObj(i -> RecordEntry.createNew(buf.readByte())).peek(e -> e.deserialize(buf)).toArray(RecordEntry[]::new);
        return this;
    }

    @Override
    public byte getId() {
        return 5;
    }
}
