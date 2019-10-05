package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class NotEntry implements RecordEntry {

    private RecordEntry entry;

    @Override
    public CharSequence getAsFileString() {
        throw new UnsupportedOperationException("Cannot convert a NOT request to a file string");
    }

    @Override
    public RecordEntry deserialize(ByteBuf buf) {
        this.entry = RecordEntry.createNew(buf.readByte());
        this.entry.deserialize(buf);
        return this;
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeByte(this.entry.getId());
        this.entry.serialize(buf);
    }

    @Override
    public byte getId() {
        return 90;
    }
}
