package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseField;
import com.wynprice.cafedafydd.common.FieldDefinition;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ToString
public class DatabaseRecord {

    @Getter
    private final int primaryField;

    @Getter
    private final NamedRecord[] entries;

    @SuppressWarnings("unchecked")
    public DatabaseRecord(int primaryField, FieldDefinition[] definitions, DatabaseField[] entries) {
        this.primaryField = primaryField;

        //Normally, I wouldn't store the stream as a variable, but for whatever reason if i try otherwise the compiler
        //Shouts at me. No idea why this would be.
        Stream<NamedRecord> stream =
            IntStream.range(0, definitions.length)
            .mapToObj(i -> NamedRecord.of(definitions[i], entries[i]));
        this.entries = stream.toArray(NamedRecord[]::new);
    }

    @SuppressWarnings("unchecked")
    private <T> NamedRecord<T> findEntry(FieldDefinition<T> definition) {
        if(definition == FieldDefinitions.ID) {
            return (NamedRecord<T>) FieldDefinitions.ID.create(this.primaryField);
        }
        return Arrays.stream(this.entries)
            .filter(e -> e.getDefinition() == definition)
            .collect(UtilCollectors.toSingleEntry())
            .orElseThrow(() -> new IllegalArgumentException("Unable to find record entry: " + definition + " in fields " + Arrays.toString(this.entries)));
    }

    public <T> DatabaseField<T> getRawField(FieldDefinition<T> definition) {
        return this.findEntry(definition).getRecord();
    }

    public <T> T get(FieldDefinition<T> definition) {
        return this.getRawField(definition).getData();
    }

    public <T> void set(FieldDefinition<T> definition, T value) {
        this.findEntry(definition).getRecord().setData(value);
    }

    public String toFileString() {
        return this.primaryField + "," + String.join(",", () -> Arrays.stream(this.entries).map(NamedRecord::getToString).iterator());
    }
}
