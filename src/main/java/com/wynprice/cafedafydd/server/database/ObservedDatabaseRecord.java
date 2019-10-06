package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;

import java.util.Arrays;

/**
 * A Database Record that observers the field values, and on any changes,
 * reindexing this entry field with {@link Database#reindexEntryField(DatabaseRecord, String)}
 */
public class ObservedDatabaseRecord extends DatabaseRecord {

    private final Database database;

    public ObservedDatabaseRecord(Database database, int primaryField, RecordEntry[] entries) {
        super(database.getFieldList(), primaryField, entries);
        this.database = database;
    }

    @Override
    public void setField(String field, RecordEntry value) {
        if(Arrays.asList(this.database.getPrimaryFields()).contains(field) && this.database.hasAllEntries(NamedRecord.of(field, value))) {
            throw new IllegalArgumentException("Primary field '" + field + "' with value '" + value + "' already exists in database " + this.database.getFilename());
        }
        super.setField(field, value);
        this.database.writeToFile();
        this.database.reindexEntryField(this, field);
    }
}
