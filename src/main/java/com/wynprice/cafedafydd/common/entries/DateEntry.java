package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode
public class DateEntry implements RecordEntry {
    private String entry;

    @Override
    public Date getAsDate() {
        return DateUtils.fromISO8691(this.entry, false);
    }

    @Override
    public DateEntry setDate(String d) {
        this.entry = d;
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
    public DateEntry deserialize(ByteBuf buf) {
        this.entry = ByteBufUtils.readString(buf);
        return this;
    }

    @Override
    public byte getId() {
        return 4;
    }
}
