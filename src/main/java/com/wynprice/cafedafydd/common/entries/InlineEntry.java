package com.wynprice.cafedafydd.common.entries;

import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class InlineEntry implements RecordEntry {
    private String requestDatabase;
    private String requestDatabaseField;
    private NamedRecord[] form;


    @Override
    public CharSequence getAsFileString() {
        throw new UnsupportedOperationException("Cannot convert an INLINE request to a file string");
    }

    @Override
    public InlineEntry deserialize(ByteBuf buf) {
        this.requestDatabase = ByteBufUtils.readString(buf);
        this.requestDatabaseField = ByteBufUtils.readString(buf);
        this.form = NamedRecord.read(buf, this.requestDatabase);
        return this;
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeString(this.requestDatabase, buf);
        ByteBufUtils.writeString(this.requestDatabaseField, buf);
        NamedRecord.write(this.form, buf);
    }

    @Override
    public byte getId() {
        return 91;
    }
}
