package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@RequiredArgsConstructor
@ToString
public class DatabaseRecord {

    @ToString.Exclude
    private final List<String> fields;

    @Getter
    private final int primaryField;

    @Getter
    private final String[] entries;

    public String toFileString() {
        return this.primaryField + "," + String.join(",", this.entries);
    }

    public String getField(String field) {
        if(DatabaseStrings.ID.equals(field)) {
            return String.valueOf(this.primaryField);
        }
        return this.entries[this.fields.indexOf(field)];
    }

    public void setField(String field, String value) {
        this.entries[this.fields.indexOf(field)] = value;
    }
}
