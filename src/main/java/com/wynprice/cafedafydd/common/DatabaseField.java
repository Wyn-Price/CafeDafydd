package com.wynprice.cafedafydd.common;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class DatabaseField<T> {
    private final RecordType<T> type;
    private T data;

    public void serialize(ByteBuf buf) {
        this.type.serialize(buf, this);
    }

    public DatabaseField deserialize(ByteBuf buf) {
        this.type.deserialize(buf, this);
        return this;
    }

    public CharSequence getAsFileString() {
        return this.type.getAsFileString(this.data);
    }

    public String getAsCompareString() {
        return this.type.getCompareString(this.data);
    }
}
