package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.entries.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public interface RecordType {

    RecordType STRING = reader -> new StringEntry().setString(reader.getNextEntry());
    RecordType INTEGER = reader -> new IntEntry().setInt(Integer.valueOf(reader.getNextEntry()));
    RecordType FLOAT = reader -> new FloatEntry().setFloat(Float.valueOf(reader.getNextEntry()));
    RecordType BOOLEAN = reader -> new BooleanEntry().setBoolean("1".equals(reader.getNextEntry()));
    RecordType DATE = reader -> new DateEntry().setDate(reader.getNextEntry());
    UnaryOperator<RecordType> ARRAY = type -> reader -> {
        List<RecordEntry> entryList = new ArrayList<>();
        for(String entry : new FileLineReader(reader.getNextEntry())) {
            entryList.add(type.getSafeEntry(new FileLineReader(entry)));
        }
        return new ArrayEntry().setArray(entryList.toArray(new RecordEntry[0]));
    };

    RecordEntry getEntry(FileLineReader reader);

    default RecordEntry getSafeEntry(FileLineReader reader) {
        if(reader.peakNext() == ',') {
            return new EmptyRecord();
        }
        return this.getEntry(reader);
    }
}