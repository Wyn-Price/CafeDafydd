package com.wynprice.cafedafydd.server.database;

import com.sun.istack.internal.Nullable;
import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.entries.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface RecordType {

    RecordType STRING = reader -> runIfNonnull(new StringEntry(), reader, e -> e.setString(reader.getNextEntry()));
    RecordType INTEGER = reader -> runIfNonnull(new IntEntry(), reader, e -> e.setInt(Integer.valueOf(reader.getNextEntry())));
    RecordType FLOAT = reader -> runIfNonnull(new FloatEntry(), reader, e -> e.setFloat(Float.valueOf(reader.getNextEntry())));
    RecordType BOOLEAN = reader -> runIfNonnull(new BooleanEntry(), reader, e -> e.setBoolean("1".equals(reader.getNextEntry())));
    RecordType DATE = reader -> runIfNonnull(new DateEntry(), reader, e -> e.setDate(reader.getNextEntry()));
    UnaryOperator<RecordType> ARRAY = type -> reader -> runIfNonnull(new ArrayEntry(), reader, e -> {
        List<RecordEntry> entryList = new ArrayList<>();
        for(String entry : new FileLineReader(reader.getNextEntry())) {
            entryList.add(type.getEntry(new FileLineReader(entry)));
        }
        e.setArray(entryList.toArray(new RecordEntry[0]));
    });

    RecordEntry getEntry(@Nullable FileLineReader reader);

    static <T> T runIfNonnull(T entry, @Nullable FileLineReader reader, Consumer<T> r) {
        if(reader != null) {
            r.accept(entry);
        }
        return entry;
    }

}