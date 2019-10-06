package com.wynprice.cafedafydd.common;

import com.wynprice.cafedafydd.common.utils.DateUtils;
import com.wynprice.cafedafydd.common.entries.*;
import io.netty.buffer.ByteBuf;

import java.util.Date;

public interface RecordEntry {

    static RecordEntry intRecord(int i) {
        return new IntEntry().setInt(i);
    }
    static RecordEntry floatRecord(float f) {
        return new FloatEntry().setFloat(f);
    }
    static RecordEntry boolRecord(boolean b) {
        return new BooleanEntry().setBoolean(b);
    }
    static RecordEntry dateRecord(Date date) {
        return new DateEntry().setDate(DateUtils.toISO8691(date));
    }
    static RecordEntry dateRecord(String date) {
        return new DateEntry().setDate(date);
    }
    static RecordEntry arrayRecord(RecordEntry[] array) {
        return new ArrayEntry().setArray(array);
    }
    static RecordEntry stringRecord(String str) {
        return new StringEntry().setString(str);
    }

    static RecordEntry createNew(byte id) {
        switch (id) {
            case 0: return new StringEntry();
            case 1: return new IntEntry();
            case 2: return new FloatEntry();
            case 3: return new BooleanEntry();
            case 4: return new DateEntry();
            case 5: return new ArrayEntry();
            case 90: return new NotEntry();
            case 91: return new InlineEntry();
            default: throw new IllegalArgumentException("Don't know how to handle entry type of " + id);
        }
    }

    default String getAsString() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to string");
    }

    default RecordEntry setString(String s) {
        throw new UnsupportedOperationException("Cannot set " + this + " to string");
    }
    default int getAsInt() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to an int");
    }

    default RecordEntry setInt(int i) {
        throw new UnsupportedOperationException("Cannot set " + this + " to an int");
    }
    default float getAsFloat() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to a float");
    }

    default RecordEntry setFloat(float f) {
        throw new UnsupportedOperationException("Cannot set " + this + " to a float");
    }
    default boolean getAsBoolean() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to a bool");
    }

    default RecordEntry setBoolean(boolean b) {
        throw new UnsupportedOperationException("Cannot set " + this + " to a bool");
    }
    default Date getAsDate() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to a date");
    }

    default RecordEntry setDate(String d) {
        throw new UnsupportedOperationException("Cannot set " + this + " to a date");
    }
    default RecordEntry[] getAsArray() {
        throw new UnsupportedOperationException("Cannot convert " + this + " to an array");
    }

    default RecordEntry setArray(RecordEntry[] array) {
        throw new UnsupportedOperationException("Cannot set " + this + " to an array");
    }

    byte getId();
    CharSequence getAsFileString();
    RecordEntry deserialize(ByteBuf buf);
    void serialize(ByteBuf buf);
    default String getCompareString() {
        return this.getAsFileString().toString();
    }
}
