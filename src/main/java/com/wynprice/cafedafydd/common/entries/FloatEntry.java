package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class FloatEntry implements RecordEntry {
    private float entry;

    @Override
    public float getAsFloat() {
        return this.entry;
    }

    @Override
    public FloatEntry setFloat(float f) {
        this.entry = f;
        return this;
    }

    @Override
    public CharSequence getAsFileString() {
        return String.valueOf(this.entry);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeFloat(this.entry);
    }

    @Override
    public FloatEntry deserialize(ByteBuf buf) {
        this.entry = buf.readFloat();
        return this;
    }

    @Override
    public byte getId() {
        return 2;
    }
}
