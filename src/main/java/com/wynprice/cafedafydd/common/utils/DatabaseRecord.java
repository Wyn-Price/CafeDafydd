package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

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
    private final String[] entries;

    /**
     * Converts this record to a string to be put in the csv file.
     * @return The csv line string.
     */
    public String toFileString() {
        return this.primaryField + "," + String.join(",", this.entries);
    }

    /**
     * Gets the field
     * @param field the field to get the entry from
     * @return the value for the specified field
     */
    public String getField(String field) {
        if(DatabaseStrings.ID.equals(field)) {
            return String.valueOf(this.primaryField);
        }
        return this.entries[this.fields.indexOf(field)];
    }

    /**
     * Sets the entries to this field
     * @param field the field to set to
     * @param value the value to set with the field
     */
    public void setField(String field, String value) {
        this.entries[this.fields.indexOf(field)] = value;
    }
}

