package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.RecordEntry;

import java.util.Arrays;

public class DatabaseDefinition {

    private final RecordType[] entries;

    private DatabaseDefinition(RecordType... entries) {
        this.entries = new RecordType[entries.length + 1];
        this.entries[0] = RecordType.INTEGER;
        System.arraycopy(entries, 0, this.entries, 1, entries.length);
    }

    public static DatabaseDefinition of(RecordType[] entries) {
        return new DatabaseDefinition(entries);
    }

    public RecordEntry[] parseLine(String line) {//fileFields
        FileLineReader reader = new FileLineReader(line);
        return Arrays.stream(this.entries).map(f -> f.getSafeEntry(reader)).toArray(RecordEntry[]::new);
    }

    public RecordType[] getEntries() {
        return entries;
    }
}
