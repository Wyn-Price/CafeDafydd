package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;

public class ObservedDatabaseRecord extends DatabaseRecord {

    private final Database database;

    public ObservedDatabaseRecord(Database database, int primaryField, String[] entries) {
        super(database.getFieldList(), primaryField, entries);
        this.database = database;
    }

    @Override
    public void setField(String field, String value) {
        super.setField(field, value);
        this.database.writeToFile();
        this.database.reindexEntryField(this, field);
    }
}
