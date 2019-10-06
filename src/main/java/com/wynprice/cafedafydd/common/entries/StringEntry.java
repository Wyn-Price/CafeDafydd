package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class StringEntry implements RecordEntry {
    private String entry = "";

    @Override
    public String getAsString() {
        return this.entry;
    }

    @Override
    public StringEntry setString(String s) {
        this.entry = s;
        return this;
    }

    @Override
    public CharSequence getAsFileString() {
        return this.entry;
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeString(this.entry, buf);
    }

    @Override
    public StringEntry deserialize(ByteBuf buf) {
        this.entry = ByteBufUtils.readString(buf);
        return this;
    }

    @Override
    public byte getId() {
        return 0;
    }
}