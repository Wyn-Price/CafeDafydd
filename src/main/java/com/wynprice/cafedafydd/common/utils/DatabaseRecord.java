package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.entries.IntEntry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a record in a database.
 */
@ToString
@RequiredArgsConstructor
public class DatabaseRecord {

    /**
     * The list of fields from the database. These fields should be in the same order as the {@link #entries}
     */
    @Getter
    @ToString.Exclude
    private final List<String> fields;

    /**
     * This records primary field. This is unique per record
     */
    @Getter
    private final int primaryField;

    /**
     * This is the record entries for this record. This should be in the same order as {@link #fields}
     */
    @Getter
    private final RecordEntry[] entries;

    /**
     * Converts this record to a string to be put in the csv file.
     * @return The csv line string.
     */
    public String toFileString() {
        return this.primaryField + "," + String.join(",", () -> Arrays.stream(this.entries).map(RecordEntry::getAsFileString).iterator());
    }

    /**
     * Gets the field
     * @param field the field to get the entry from
     * @return the value for the specified field
     */
    public RecordEntry getField(String field) {
        if(DatabaseStrings.ID.equals(field)) {
            return new IntEntry().setInt(this.primaryField);
        }
        return this.entries[this.fields.indexOf(field)];
    }

    /**
     * Sets the entries to this field
     * @param field the field to set to
     * @param value the value to set with the field
     */
    public void setField(String field, RecordEntry value) {
        this.entries[this.fields.indexOf(field)] = value;
    }
}

