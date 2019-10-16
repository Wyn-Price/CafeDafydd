package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.DatabaseField;
import com.wynprice.cafedafydd.common.FieldDefinition;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;

import java.util.Arrays;

/**
 * A Database Record that observers the field values, and on any changes,
 * reindexing this entry field with {@link Database#reindexEntryField(DatabaseRecord, FieldDefinition)}
 */
public class ObservedDatabaseRecord extends DatabaseRecord {

    private final Database database;

    public ObservedDatabaseRecord(Database database, int primaryField, DatabaseField[] entries) {
        super(primaryField, database.getDefinitions(), entries);
        this.database = database;
    }

    @Override
    public <T> void set(FieldDefinition<T> definition, T value) {
        if(Arrays.asList(this.database.getPrimaryFields()).contains(definition) && this.database.hasAllEntries(definition.create(value))) {
            throw new IllegalArgumentException("Primary field '" + definition + "' with value '" + value + "' already exists in database " + this.database.getFilename());
        }
        super.set(definition, value);
        this.database.getBackupHandler().onChanged();
        this.database.writeToFile();
        this.database.reindexEntryField(this, definition);
    }
}
