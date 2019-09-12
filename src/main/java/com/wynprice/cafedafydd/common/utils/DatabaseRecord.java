package com.wynprice.cafedafydd.common.utils;

import lombok.Value;

import java.util.List;

@Value
public class DatabaseRecord {

    private final List<String> fields;
    private final int primaryField;
    private final String[] entries;

    public String toFileString() {
        return this.primaryField + "," + String.join(",", this.entries);
    }

    public String getField(String field) {
        return this.entries[this.fields.indexOf(field)];
    }

    public void setField(String field, String value) {
        this.entries[this.fields.indexOf(field)] = value;
    }
}
