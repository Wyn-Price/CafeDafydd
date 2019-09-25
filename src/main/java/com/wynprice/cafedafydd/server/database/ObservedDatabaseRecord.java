package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;

/**
 * A Database Record that observers the field values, and on any changes,
 * reindexing this entry field with {@link Database#reindexEntryField(DatabaseRecord, String)}
 */
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
