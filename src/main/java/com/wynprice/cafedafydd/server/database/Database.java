package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.*;
import com.wynprice.cafedafydd.common.search.SearchRequirement;
import com.wynprice.cafedafydd.common.utils.ArrayUtils;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import com.wynprice.cafedafydd.common.utils.UtilCollectors;
import com.wynprice.cafedafydd.server.PermissionLevel;
import com.wynprice.cafedafydd.server.utils.Algorithms;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The Database class contains all the records, file location, indexed records ect for a database.
 */
@Log4j2
public abstract class Database {

    private static final String ID = "id";

    @Getter
    private final FieldDefinition[] definitions;

    /**
     * The path to this database file
     */
    private final Path path;

    /**
     * The list of database entries
     */
    private final List<DatabaseRecord> entries = new ArrayList<>();

    /**
     * This is the list of {@link #entries} to the list of database records, sorted by the specified field.
     */
    private final Map<FieldDefinition, List<DatabaseRecord>> indexedRecords = new HashMap<>();

    /**
     * Used for handling database backups
     */
    @Getter
    private final DatabaseBackup backupHandler;

    Database() {
        this.backupHandler = new DatabaseBackup(this);
        //Set and check the fields and path
        this.definitions = this.createDefinition();
        if(this.definitions.length == 0) {
            throw new IllegalArgumentException("Need to specify the at least one field");
        }

        this.path = Paths.get("databases").resolve(this.getFilename() + ".csv");
        //If the file exists try and load from it. Save the file then to ensure the file exists
        if(this.path.toFile().exists()) {
            try {
                this.readAllLines(Files.readAllLines(this.path));
            } catch (IOException e) {
                log.error("Unable to open csv file");
            }
        } else {
            this.writeToFile();
        }
        //Reindex all the records into the indexedRecords field
        this.reindexAll();
    }

    /**
     * Reads all the lines to the database. Clears the current entries.
     * @param lines the lines to read.
     */
    public void readAllLines(List<String> lines) {
        this.entries.clear();
        List<String> fileFields = ArrayUtils.asList(lines.remove(0).split(","));
        for (String line : lines) {
            this.parseLine(line, fileFields);
        }
        this.writeToFile();
    }

    /**
     * Resorts everything in {@link #indexedRecords}. Should be called sparingly.
     * When possible call {@link #reindexRecord(DatabaseRecord)} or {@link #reindexEntryField(DatabaseRecord, FieldDefinition)}}
     * @see #indexedRecords
     */
    private void reindexAll() {
        //TODO: maybe don't use this ever, and instead insert when the file is being loaded.
        for (FieldDefinition<?> definition : this.definitions) {
            this.indexedRecords.put(definition, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> definition.getResult(r, RecordType::getCompareString))));
        }
        this.indexedRecords.put(FieldDefinitions.ID, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> String.valueOf(r.getPrimaryField()))));
    }

    /**
     * Resorts all the fields in the specified {@code record}. If possible, {@link #reindexEntryField(DatabaseRecord, FieldDefinition)} should be called.
     * @param record the record to resort.
     * @see #indexedRecords
     */
    private void reindexRecord(DatabaseRecord record) {
        for (FieldDefinition<?> definition : this.definitions) {
            this.reindexEntryField(record, definition);

        }

        List<DatabaseRecord> list = this.indexedRecords.get(FieldDefinitions.ID);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(r -> String.valueOf(r.getPrimaryField())));
    }

    /**
     * Resorts only the specified {@code field} in the record {@code record}.
     * @param record The record of which should be resorted.
     * @param definition the definition to re-sort.
     * @see #indexedRecords
     */
    <T> void reindexEntryField(DatabaseRecord record, FieldDefinition<T> definition) {
        //Remove the record from the indexed records and insert it
        List<DatabaseRecord> list = this.indexedRecords.get(definition);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(r -> definition.getResult(r, RecordType::getCompareString)));
    }

    /**
     * Parses the csv line.
     * @param line the line to parse
     * @param fileFields the fields to parse to.
     */
    private void parseLine(String line, List<String> fileFields) {
        //The entries that have been read
        FileLineReader reader = new FileLineReader(line);

        int id = RecordType.INTEGER_TYPE.parseFromFile(reader).getData();

        DatabaseField[] rawEntries = Arrays.stream(this.definitions).map(d -> d.getRecordType().parseFromFile(reader)).toArray(DatabaseField[]::new);
        DatabaseField[] readEntries = new DatabaseField[this.definitions.length];

        for (int i = 0; i < this.definitions.length; i++) {
            FieldDefinition definition = this.definitions[i];
            if(fileFields.contains(definition.getFieldName())) {
                readEntries[i] = rawEntries[fileFields.indexOf(definition.getFieldName()) - 1];
            } else {
                readEntries[i] = definition.getRecordType().createEmpty();
            }
        }

        //Get the index of the parsed line, check for duplicate entries, and if none are found add a new record to the entries list
        if(this.checkDuplicates(readEntries, id)) {
            return;
        }
        this.entries.add(new ObservedDatabaseRecord(this, id, readEntries));
    }

    public int indexOf(FieldDefinition<?> definition) {
        return IntStream.range(0, this.definitions.length).filter(i -> this.definitions[i] == definition).findAny().orElseThrow(() -> new IllegalArgumentException("Unable to find primary field " + definition + " in definition " + Arrays.toString(this.definitions)));
    }

    public FieldDefinition getForName(String fieldName) {
        return Arrays.stream(this.definitions).filter(d -> d.getFieldName().equals(fieldName)).findAny().orElseThrow(() -> new IllegalArgumentException("Unable to find definition " + fieldName + " in database with definitions: " + Arrays.toString(this.definitions)));
    }

    private int indexOf(NamedRecord[] form, FieldDefinition<?> definition) {
        return IntStream.range(0, form.length).filter(i -> form[i].getDefinition() == definition).findAny().orElse(-1);
    }

    /**CALCULATED_PRICE
     * Check for duplicate entries.
     * @param readEntries the entries read
     * @param id the id of the read entry
     * @return true if there are duplicates found, false otherwise.
     */
    private boolean checkDuplicates(DatabaseField[] readEntries, int id) {
        //Check duplicates using the primary fields.
        for (FieldDefinition<?> definition : this.getPrimaryFields()) {
            int index = this.indexOf(definition);
            //Search for any entry that has been set with the same value in the same field as the parsed entry.
            //This means that there are conflicting primary fields.
            Optional<NamedRecord[]> foundMatched = this.entries.stream().map(DatabaseRecord::getEntries).filter(a -> a[index].getRecord().getData().equals(readEntries[index].getData())).findAny();
            if(foundMatched.isPresent()) {
                log.error("Found multiple entries with the same primary field {}: '{}' in file {}. Aborting found entry. Existing entry :{}, found entry {}", definition, readEntries[index], this.path, Arrays.toString(foundMatched.get()), Arrays.toString(readEntries));
                return true;
            }
        }

        //Check for duplicates with the primary field.
        Optional<DatabaseRecord> foundMatched = this.entries.stream().filter(f -> f.getPrimaryField() == id).findAny();
        if(foundMatched.isPresent()) {
            log.error("Found multiple entries with the same id '{}' in file {}. Aborting found entry. Existing entry :{}, found entry {}", id, this.path, Arrays.toString(foundMatched.get().getEntries()), Arrays.toString(readEntries));
            return true;
        }
        return false;
    }

    /**
     * Gets the optional entry with the {@link DatabaseRecord#getPrimaryField()} equal to {@code id}
     * @param id the id to look for
     * @return the optional containing the found record, or {@link Optional#empty()} if none could be found.
     */
    public Optional<DatabaseRecord> getEntryFromId(int id) {
        return Algorithms.doMappedSearch(this.indexedRecords.get(FieldDefinitions.ID), DatabaseRecord::getPrimaryField, id, Comparator.comparing(String::valueOf));
    }

    /**
     * Searches the entries from the form. Searches are done with {@code s1.contains(s2)}, whereas s2 is the searched term, and s1 is the field.
     * @param form the form to use.
     * @return the stream of found entries.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public Stream<DatabaseRecord> searchEntries(SearchRequirement... form) {
        return this.streamEntries((o1, o2) -> o2.startsWith(o1) ? 0 : o1.compareTo(o2), form);
    }

    /**
     * Gets the entries from the form.
     * @param form the form to use.
     * @return the stream of found entries.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public Stream<DatabaseRecord> getEntries(SearchRequirement... form) {
        return this.streamEntries(String::compareTo, form);
    }

    /**
     * Searches the database for entries
     * @param comparator the comparator to compare the records.
     * @param form the form to search with.
     * @return the resulting stream from the found entries.
     */
    private Stream<DatabaseRecord> streamEntries(Comparator<String> comparator, SearchRequirement... form) {
        if(this.entries.isEmpty() ) {
            return Stream.empty();
        }
        if(form.length == 0) {
            return this.entries.stream();
        }
        List<DatabaseRecord> list = new ArrayList<>(this.entries);
        for(SearchRequirement requirement : form) {
            //If the form object equals NOT_PREFIX, then invert the search and move the index along to the nextChar position.
            boolean inverted = false;

            FieldDefinition<?> field;
            DatabaseField<?> value;


            switch (requirement.id()) {
                case 1:
                    inverted = true;
                case 0:
                    NamedRecord record = requirement instanceof NamedRecord ? (NamedRecord) requirement : ((SearchRequirement.DirectSearch) requirement).getRecord();
                    field = record.getDefinition();
                    value = record.getRecord();
                    break;

                case 2:
                    SearchRequirement.InlineSearch inline = (SearchRequirement.InlineSearch) requirement;
                    field = inline.getDefinition();
                    Optional<DatabaseField> databaseField = Databases.getFromFile(inline.getRequestDatabase())
                        .flatMap(d -> d.streamEntries(comparator, inline.getForm()).collect(UtilCollectors.toSingleEntry()))
                        .map(r -> r.getRawField(inline.getRequestDatabaseField()));
                    if(databaseField.isPresent()) {
                        value = databaseField.get();
                    } else {
                        return Stream.empty();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Don't know how to handle search requirement of id " + requirement.id());
            }

            //Damn you lambda statements
            boolean finalInverted = inverted;

            //Get the found entries for this section of the form, and then go through the current list
            //and if the form entry is inverted, removed them if the found entries contains the entry, otherwise
            //remove the element if the found entries doesn't contain it.

            List<DatabaseRecord> foundRecords = Algorithms.splicedBinarySearch(this.indexedRecords.get(field), r -> r.getRawField(field), value, (o1, o2) -> comparator.compare(o1.getAsCompareString(), o2.getAsCompareString()));
            list.removeIf(r -> finalInverted == foundRecords.contains(r));
        }
        return list.stream();
    }

    /**
     * Creates and adds a new database entry.
     * @param form this is in the same format as all forms
     * @return the generated record.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public DatabaseRecord generateAndAddDatabase(NamedRecord... form) {
        //Create the new backing array and get the current maximum id + 1. TODO: maybe get the lowest available id, instead of the max + 1
        DatabaseField[] entry = new DatabaseField[this.definitions.length];
        int newId = this.entries.stream().map(DatabaseRecord::getPrimaryField).mapToInt(i -> i).max().orElse(0) + 1;


        //Go through all the form entries and set the fields in the entry to be the field value.
        //The indexOf stuff is to ensure that it all gets put in the right place, as the input form
        //doesn't necessarily have to be in the same order as the actual fields.
        for (int i = 0; i < this.definitions.length; i++) {
            int index = this.indexOf(form, this.definitions[i]);
            if(index >= 0) {
                entry[i] = form[index].getRecord();
            } else {
                //If any of the entries haven't been set then just set them to an empty string
                entry[i] = this.definitions[i].getRecordType().createEmpty();
            }

        }


        //Create the new record, add it to the record list, reindex the record, write this database to a file then return the new entry.
        DatabaseRecord newEntry = new ObservedDatabaseRecord(this, newId, entry);
        this.entries.add(newEntry);
        this.reindexRecord(newEntry);
        this.backupHandler.onChanged();
        this.writeToFile();
        return newEntry;
    }

    /**
     * Removes the record from this database
     * @param record the record to remove
     * @return true if a record was removed, or false otherwise
     */
    public boolean removeEntry(DatabaseRecord record) {
        boolean ret = this.entries.remove(record);
        for (FieldDefinition definition : this.definitions) {
            this.indexedRecords.get(definition).remove(record);
        }
        this.indexedRecords.get(FieldDefinitions.ID).remove(record);
        this.backupHandler.onChanged();
        this.writeToFile();
        return ret;
    }

    /**
     * Tests to see if this database contains any entries matching the specified form
     * @param form the form to search for
     * @return true if there is at least one entry that matches the form, false otherwise
     */
    public boolean hasAllEntries(SearchRequirement... form) {
        return this.getEntries(form).count() > 0;
    }

    /**
     * Checks to see if the form is in the database, and if not then add it to the database.
     * @param form the form to check/generate
     * @return true of a form was generated, false otherwise
     */
    public boolean generateIfNotPresent(NamedRecord... form) {
        if(!this.hasAllEntries(form)) {
            this.generateAndAddDatabase(form);
            return true;
        }
        return false;
    }

    /**
     * Writes to the file at {@link #path}
     * @see DatabaseRecord#toFileString()
     */
    public void writeToFile() {
        this.backupHandler.onWritten();
        //If the file parent doesn't exist, try and generate the parent folders and log if an error occurs.
        if(Files.notExists(this.path.getParent())) {
            try {
                Files.createDirectories(this.path.getParent());
            } catch (IOException e) {
                log.error("Unable to create the database file", e);
                return;
            }
        }

        //Try and write the databse to the file. This occurs by first writing the fields, then the records #toFileString
        try {
            Files.write(this.path, this.getFileGeneratedList());
        } catch (IOException e) {
            log.error("Unable to write database file " + this.path.getName(this.path.getNameCount() - 1), e);
        }
    }

    /**
     * Gets the lines that are written into the file or backup file
     * @return the list to put in a file
     */
    public List<String> getFileGeneratedList() {
        List<String> lines = new ArrayList<>();
        lines.add(ID + "," + String.join(",", () -> Arrays.stream(this.definitions).map(d -> (CharSequence) d.getFieldName()).iterator()));
        this.entries.stream().map(DatabaseRecord::toFileString).forEach(lines::add);
        return lines;
    }


    /**
     * Gets the read level for this database
     * @return the minimum read level for this database
     */
    public PermissionLevel getReadLevel() {
        return PermissionLevel.STAFF_MEMBER;
    }

    /**
     * Gets the write level for this database
     * @return the minimum write level for this database
     */
    public PermissionLevel getEditLevel() {
        return PermissionLevel.STAFF_MEMBER;
    }

    /**
     * Used for permission levels based on individual records
     * @param record the record to check
     * @param userID the user id to check with
     * @param level the permission level to check with
     * @return true if they can read it, false if otherwise.
     */
    public boolean canRead(DatabaseRecord record, int userID, PermissionLevel level) {
        return true;
    }

    /**
     * Used for permission levels based on individual records
     * @param record the record to check
     * @param userID the user id to check with
     * @param level the permission level to check with
     * @return true if they can edit it, false if otherwise.
     */
    public boolean canEdit(DatabaseRecord record, int userID, PermissionLevel level) {
        return true;
    }

    /**
     * @return the file name for this database
     */
    protected abstract String getFilename();

    /**
     * @return the definition of field names to schema types for this database.
     */
    protected abstract FieldDefinition[] createDefinition();

    /**
     * @return the primary fields for this database. These fields are the ones that MUST have unique values.
     * NOTE ID is already included in this.
     */
    public FieldDefinition[] getPrimaryFields() {
        return new FieldDefinition[0];
    }

}
