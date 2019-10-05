package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class IntEntry implements RecordEntry {
    private int entry;

    @Override
    public int getAsInt() {
        return this.entry;
    }

    @Override
    public IntEntry setInt(int i) {
        this.entry = i;
        return this;
    }

    @Override
    public CharSequence getAsFileString() {
        return String.valueOf(this.entry);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(this.entry);
    }

    @Override
    public IntEntry deserialize(ByteBuf buf) {
        this.entry = buf.readInt();
        return this;
    }

    @Override
    public byte getId() {
        return 1;
    }

}
