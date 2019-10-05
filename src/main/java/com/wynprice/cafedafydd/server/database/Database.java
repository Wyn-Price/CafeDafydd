package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.RecordEntry;
import com.wynprice.cafedafydd.common.utils.ArrayUtils;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import com.wynprice.cafedafydd.common.utils.UtilCollectors;
import com.wynprice.cafedafydd.server.PermissionLevel;
import com.wynprice.cafedafydd.common.entries.EmptyRecord;
import com.wynprice.cafedafydd.common.entries.InlineEntry;
import com.wynprice.cafedafydd.common.entries.NotEntry;
import com.wynprice.cafedafydd.server.utils.Algorithms;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Database class contains all the records, file location, indexed records ect for a database.
 */
@Log4j2
public abstract class Database {

    private static final String ID = "id";

    private final DatabaseDefinition schema;

    /**
     * This is the list of fields in the database, and is the first line of the csv file
     */
    private final List<String> fields;

    /**
     * The path to this database file
     */
    private final Path path;

    /**
     * The list of database entries
     */
    private final List<DatabaseRecord> entries = new ArrayList<>();

    /**
     * This is the list of {@link #fields} to the list of database records, sorted by the specified field.
     */
    private final Map<String, List<DatabaseRecord>> indexedRecords = new HashMap<>();

    Database() {
        //Set and check the fields and path
        Field[] pairs = this.getDefinition();
        this.fields = Arrays.stream(pairs).map(Field::getFieldName).collect(Collectors.toList());
        this.schema = DatabaseDefinition.of(Arrays.stream(pairs).map(Field::getEntry).toArray(RecordType[]::new));

        if(this.fields.isEmpty()) {
            throw new IllegalArgumentException("Need to specify the at least one field");
        }

        this.path = Paths.get("databases").resolve(this.getFilename() + ".csv");

        //If the file exists try and load from it. Save the file then to ensure the file exists
        if(Files.exists(this.path)) {
            try {
                List<String> lines = Files.readAllLines(this.path);
                List<String> fileFields = ArrayUtils.asList(lines.remove(0).split(","));
                for (String line : lines) {
                    this.parseLine(line, fileFields);
                }
            } catch (IOException e) {
                log.error("Unable to open csv file");
            }
        }
        this.writeToFile();


        //Reindex all the records into the indexedRecords field
        this.reindexAll();
    }

    /**
     * Resorts everything in {@link #indexedRecords}. Should be called sparingly.
     * When possible call {@link #reindexRecord(DatabaseRecord)} or {@link #reindexEntryField(DatabaseRecord, String)}
     * @see #indexedRecords
     */
    private void reindexAll() {
        //TODO: maybe don't use this ever, and instead insert when the file is being loaded.
        for (String field : this.fields) {
            this.indexedRecords.put(field, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> r.getField(field).getCompareString())));
        }
        this.indexedRecords.put(DatabaseStrings.ID, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> String.valueOf(r.getPrimaryField()))));
    }

    /**
     * Resorts all the fields in the specified {@code record}. If possible, {@link #reindexEntryField(DatabaseRecord, String)} should be called.
     * @param record the record to resort.
     * @see #indexedRecords
     */
    private void reindexRecord(DatabaseRecord record) {
        for (String field : this.fields) {
            this.reindexEntryField(record, field);
        }

        List<DatabaseRecord> list = this.indexedRecords.get(DatabaseStrings.ID);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(r -> String.valueOf(r.getPrimaryField())));
    }

    /**
     * Resorts only the specified {@code field} in the record {@code record}.
     * @param record The record of which should be resorted.
     * @param field the field to resort.
     * @see #indexedRecords
     */
    void reindexEntryField(DatabaseRecord record, String field) {
        //Remove the record from the indexed records and insert it
        List<DatabaseRecord> list = this.indexedRecords.get(field);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(r -> r.getField(field).getCompareString()));
    }

    /**
     * Parses the csv line.
     * @param line the line to parse
     * @param fileFields the fields to parse to.
     */
    private void parseLine(String line, List<String> fileFields) {
        //The entries that have been read
        RecordEntry[] rawEntries = this.schema.parseLine(line);
        RecordEntry[] readEntries = new RecordEntry[rawEntries.length - 1];

        for (String field : fileFields) {
            if(!ID.equals(field)) {
                readEntries[this.fields.indexOf(field)] = rawEntries[fileFields.indexOf(field)];
            }
        }

        //Get the index of the parsed line, check for duplicate entries, and if none are found add a new record to the entries list
        int id = rawEntries[fileFields.indexOf(ID)].getAsInt();
        if(this.checkDuplicates(readEntries, id)) {
            return;
        }
        this.entries.add(new ObservedDatabaseRecord(this, id, readEntries));
    }

    /**
     * Check for duplicate entries.
     * @param readEntries the entries read
     * @param id the id of the read entry
     * @return true if there are duplicates found, false otherwise.
     */
    private boolean checkDuplicates(RecordEntry[] readEntries, int id) {
        //Check duplicates using the primary fields.
        for (String field : this.getPrimaryFields()) {
            int index = this.fields.indexOf(field);

            //Search for any entry that has been set with the same value in the same field as the parsed entry.
            //This means that there are conflicting primary fields.
            Optional<RecordEntry[]> foundMatched = this.entries.stream().map(DatabaseRecord::getEntries).filter(a -> a[index].equals(readEntries[index])).findAny();
            if(foundMatched.isPresent()) {
                log.error("Found multiple entries with the same primary field {}: '{}' in file {}. Aborting found entry. Existing entry :{}, found entry {}", field, readEntries[index], this.path, Arrays.toString(foundMatched.get()), Arrays.toString(readEntries));
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
     * Returns a new list with {@code added} and {@code arr} added together
     * @param arr the base list
     * @param added the vararg array.
     * @return a new list of {@code added} + {@code arr}
     */
    private List<String> concat(List<String> arr, String... added) {
        List<String> newList = new ArrayList<>(arr);
        Collections.addAll(newList, added);
        return newList;
    }

    /**
     * Gets the optional entry with the {@link DatabaseRecord#getPrimaryField()} equal to {@code id}
     * @param id the id to look for
     * @return the optional containing the found record, or {@link Optional#empty()} if none could be found.
     */
    public Optional<DatabaseRecord> getEntryFromId(int id) {
        return Algorithms.doMappedSearch(this.indexedRecords.get(DatabaseStrings.ID), DatabaseRecord::getPrimaryField, id, Comparator.comparing(String::valueOf));
    }

    /**
     * Searches the entries from the form. Searches are done with {@code s1.contains(s2)}, whereas s2 is the searched term, and s1 is the field.
     * @param form the form to use.
     * @return the stream of found entries.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public Stream<DatabaseRecord> searchEntries(NamedRecord... form) {
        return this.streamEntries((o1, o2) -> o2.getCompareString().startsWith(o1.getCompareString()) ? 0 : o1.getCompareString().compareTo(o2.getCompareString()), form);
    }

    /**
     * Gets the entries from the form.
     * @param form the form to use.
     * @return the stream of found entries.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public Stream<DatabaseRecord> getEntries(NamedRecord... form) {
        return this.streamEntries(Comparator.comparing(RecordEntry::getCompareString), form);
    }

    /**
     * Searches the database for entries
     * @param comparator
     * @param form
     * @return
     */
    private Stream<DatabaseRecord> streamEntries(Comparator<RecordEntry> comparator, NamedRecord... form) {
        if(this.entries.isEmpty() ) {
            return Stream.empty();
        }
        if(form.length == 0) {
            return this.entries.stream();
        }
        List<DatabaseRecord> list = new ArrayList<>(this.entries);
        for(NamedRecord record : form) {
            //If the form object equals NOT_PREFIX, then invert the search and move the index along to the nextChar position.
            boolean inverted = false;

            String field = record.getField();
            RecordEntry formValue = record.getRecord();

            if(formValue instanceof NotEntry) {
                inverted = true;
                formValue = ((NotEntry)formValue).getEntry();
            }
            //Damn you lambda statements
            boolean finalInverted = inverted;

            if(formValue instanceof InlineEntry) {
                InlineEntry value = (InlineEntry) formValue;
                Optional<RecordEntry> optional = Databases.getFromFile(value.getRequestDatabase())
                    .flatMap(d -> d.streamEntries(comparator, value.getForm()).collect(UtilCollectors.toSingleEntry()))
                    .map(r -> r.getField(value.getRequestDatabaseField()));
                if(optional.isPresent()) {
                    formValue = optional.get();
                } else {
                    return Stream.empty();
                }
            }


            //Get the found entries for this section of the form, and then go through the current list
            //and if the form entry is inverted, removed them if the found entries contains the entry, otherwise
            //remove the element if the found entries doesn't contain it.
            List<DatabaseRecord> foundRecords = Algorithms.splicedBinarySearch(this.indexedRecords.get(field), r -> r.getField(field), formValue, comparator);
            list.removeIf(r -> finalInverted == foundRecords.contains(r));
        }
        return list.stream();
    }

    /**
     * Creates and adds a new database entry.
     * @param form this is in the same format as all forms, but cannot contain {@link DatabaseStrings#NOT_PREFIX}
     * @return the generated record.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public DatabaseRecord generateAndAddDatabase(NamedRecord... form) {
        //Create the new backing array and get the current maximum id + 1. TODO: maybe get the lowest available id, instead of the max + 1
        RecordEntry[] entry = new RecordEntry[this.fields.size()];
        int newId = this.entries.stream().map(DatabaseRecord::getPrimaryField).mapToInt(i -> i).max().orElse(0) + 1;

        //Go through all the form entries and set the fields in the entry to be the field value.
        //The indexOf stuff is to ensure that it all gets put in the right place, as the input form
        //doesn't necessarily have to be in the same order as the actual fields.
        for (NamedRecord record : form) {
            entry[this.fields.indexOf(record.getField())] = record.getRecord();
        }

        //If any of the entries haven't been set then just set them to an empty string
        for (int i = 0; i < entry.length; i++) {
            if(entry[i] == null) {
                entry[i] = new EmptyRecord();
            }
        }


        //Create the new record, add it to the record list, reindex the record, write this database to a file then return the new entry.
        DatabaseRecord newEntry = new ObservedDatabaseRecord(this, newId, entry);
        this.entries.add(newEntry);
        this.reindexRecord(newEntry);
        this.writeToFile();
        return newEntry;
    }

    /**
     * Tests to see if this database contains any entries matching the specified form
     * @param form the form to search for
     * @return true if there is at least one entry that matches the form, false otherwise
     */
    public boolean hasAllEntries(NamedRecord... form) {
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
            List<String> lines = new ArrayList<>();
            lines.add(ID + "," + String.join(",", this.fields));
            this.entries.stream().map(DatabaseRecord::toFileString).forEach(lines::add);
            Files.write(this.path, lines);
        } catch (IOException e) {
            log.error("Unable to write database file " + this.path.getName(this.path.getNameCount() - 1), e);
        }
    }


    public List<String> getFieldList() {
        return this.fields;
    }

    public RecordType[] getSchema() {
        return schema.getEntries();
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
    protected abstract Field[] getDefinition();


    /**
     * @return the primary fields for this database. These fields are the ones that MUST have unique values.
     * NOTE ID is already included in this.
     */
    public String[] getPrimaryFields() {
        return new String[0];
    }

    @Data(staticConstructor = "of")
    public static class Field {
        private final String fieldName;
        private final RecordType entry;
    }
}
