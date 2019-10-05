package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.utils.DateUtils;
import com.wynprice.cafedafydd.common.RecordEntry;
import io.netty.buffer.ByteBuf;

import java.util.Date;

public class EmptyRecord implements RecordEntry {

    @Override
    public CharSequence getAsFileString() {
        return "";
    }

    @Override
    public byte getId() {
        return -1;
    }

    @Override
    public EmptyRecord deserialize(ByteBuf buf) {
        return new EmptyRecord();
    }

    @Override
    public void serialize(ByteBuf buf) {

    }

    @Override
    public RecordEntry[] getAsArray() {
        return new RecordEntry[0];
    }

    @Override
    public Date getAsDate() {
        return DateUtils.EMPTY_DATE;
    }

    @Override
    public float getAsFloat() {
        return 0;
    }

    @Override
    public int getAsInt() {
        return 0;
    }

    @Override
    public String getAsString() {
        return "";
    }

    @Override
    public boolean getAsBoolean() {
        return false;
    }
}
