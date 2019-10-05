package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class BooleanEntry implements RecordEntry {
    private boolean entry;

    @Override
    public boolean getAsBoolean() {
        return this.entry;
    }

    @Override
    public BooleanEntry setBoolean(boolean b) {
        this.entry = b;
        return this;
    }

    @Override
    public CharSequence getAsFileString() {
        return String.valueOf(this.entry);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.entry);
    }

    @Override
    public BooleanEntry deserialize(ByteBuf buf) {
        this.entry = buf.readBoolean();
        return this;
    }

    @Override
    public byte getId() {
        return 3;
    }
}
