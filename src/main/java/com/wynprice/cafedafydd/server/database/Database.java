package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.UtilCollectors;
import com.wynprice.cafedafydd.server.PermissionLevel;
import com.wynprice.cafedafydd.server.utils.Algorithms;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
        this.fields = Arrays.asList(this.getFields());
        if(this.fields.isEmpty()) {
            throw new IllegalArgumentException("Need to specify the at least one field");
        }

        this.path = Paths.get("databases").resolve(this.getFilename() + ".csv");

        //If the file exists try and load from it. Save the file then to ensure the file exists
        if(Files.exists(this.path)) {
            try {
                List<String> lines = Files.readAllLines(this.path);
                List<String> fileFields = this.concat(new ArrayList<>(), lines.remove(0).split(","));
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
            this.indexedRecords.put(field, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> r.getField(field))));
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
        Algorithms.insert(list, record, Comparator.comparing(r -> r.getField(field)));
    }

    /**
     * Parses the csv line.
     * @param line the line to parse
     * @param fileFields the fields to parse to.
     */
    private void parseLine(String line, List<String> fileFields) {
        //TODO:
        // move to character analysis rather than splitting.
        // Possible use -> r/,(?=(?:[^[]*\[[^]]*\])*[^]]*$)/ for splitting,
        // to allow for a,[aa,bb,cc],c -> to be only 3 records, with the second record as an array

        //Get a list of fields with ID on the end, and get the line split by comma
        List<String> idFields = this.concat(this.fields, ID);
        List<String> arr = this.concat(new ArrayList<>(), line.split(","));

        //The entries that have been read
        String[] readEntries = new String[this.fields.size()];

        //Go through the list of values separated by a comma on this line. If that amount of
        //values is larger than the fileFields, then there is too many fields on the entry.
        for (int i = 0; i < arr.size(); i++) {
            if(i >= fileFields.size()) {
                log.error("Too many fields on entry {}. Expected {}, got {}", line, fileFields.size(), arr.size());
                return;
            }

            //Get the field and value. If the fields+ids don't contain the specified field, log an error and return
            String field = fileFields.get(i);
            String value = arr.get(i);
            if(idFields.contains(field)) {

                //If the field is not the ID field, get the index of the field on the idFields and set that to the read array.
                if(!field.equals(ID)) {
                    readEntries[idFields.indexOf(field)] = value;
                }
            } else {
                log.error("Field in {} contains field {} with value {} which doesn't exist in this database fields: {}", this.path, field, value, idFields);
                return;
            }
        }

        //Go through the read array and check that all the fields have been set.
        //TODO: have the databases be able to set non primary fields in databases when values are missing.
        for (int i = 0; i < readEntries.length; i++) {
            if(readEntries[i] == null) {
                log.error("CVS file {} does not contain field {}", this.path, idFields.get(i));
                return;
            }
        }

        //Get the index of the parsed line, check for duplicate entries, and if none are found add a new record to the entries list
        int id = Integer.parseInt(arr.get(fileFields.indexOf(ID)));
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
    private boolean checkDuplicates(String[] readEntries, int id) {
        //Check duplicates using the primary fields.
        for (String field : this.getPrimaryFields()) {
            int index = this.fields.indexOf(field);

            //Search for any entry that has been set with the same value in the same field as the parsed entry.
            //This means that there are conflicting primary fields.
            Optional<String[]> foundMatched = this.entries.stream().map(DatabaseRecord::getEntries).filter(a -> a[index].equals(readEntries[index])).findAny();
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
    public Stream<DatabaseRecord> searchEntries(String... form) {
        return this.streamEntries((o1, o2) -> o2.contains(o1) ? 0 : o1.compareTo(o2), form);
    }

    /**
     * Gets the entries from the form.
     * @param form the form to use.
     * @return the stream of found entries.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public Stream<DatabaseRecord> getEntries(String... form) {
        return this.streamEntries(String::compareTo, form);
    }

    /**
     * Searches the database for entries
     * @param comparator
     * @param form
     * @return
     */
    private Stream<DatabaseRecord> streamEntries(Comparator<String> comparator, String... form) {
        if(this.entries.isEmpty() || form.length == 0) {
            return Stream.empty();
        }
        Iterator<String> iterator = Arrays.stream(form).iterator();
        List<DatabaseRecord> list = new ArrayList<>(this.entries);
        while (iterator.hasNext()) {
            //If the form object equals NOT_PREFIX, then invert the search and move the index along to the next position.
            boolean inverted = false;

            String field = iterator.next();
            if(field.equals(DatabaseStrings.NOT_PREFIX)) {
                field = iterator.next();
                inverted = true;
            }
            //Damn you lambda statements
            String finalField = field;
            boolean finalInverted = inverted;

            String formValue = iterator.next();

            if(formValue.equals(DatabaseStrings.INLINE_REQUEST_PREFIX)) {
                Optional<String> inlineFormValue = this.getInlineFormValue(comparator, iterator);
                if(inlineFormValue.isPresent()) {
                    formValue = inlineFormValue.get();
                } else {
                    return Stream.empty();
                }
            }

            //Get the found entries for this section of the form, and then go through the current list
            //and if the form entry is inverted, removed them if the found entries contains the entry, otherwise
            //remove the element if the found entries doesn't contain it.
            List<DatabaseRecord> foundRecords = Algorithms.splicedBinarySearch(this.indexedRecords.get(finalField), r -> r.getField(finalField), formValue, comparator);
            list.removeIf(r -> finalInverted == foundRecords.contains(r));
        }
        return list.stream();
    }

    private Optional<String> getInlineFormValue(Comparator<String> comparator, Iterator<String> iterator) {
        Optional<Database> file = Databases.getFromFile(iterator.next());
        String field = iterator.next();
        String fieldSizeLength = iterator.next();

        int length;
        try {
            length = Integer.parseInt(fieldSizeLength);
        } catch (NumberFormatException e) {
            log.error("Tried to skip inline request with length: " + fieldSizeLength + " but that is not a number.");
            return Optional.empty();
        }

        if(file.isPresent()) {
            String[] form = IntStream.range(0, length).mapToObj(i -> iterator.next()).toArray(String[]::new);
            return file.flatMap(d -> d.streamEntries(comparator, form).collect(UtilCollectors.toSingleEntry())).map(r -> r.getField(field));
        }
        for (int i = 0; i < length; i++) {
            iterator.next();
        }
        return Optional.empty();
    }

    /**
     * Creates and adds a new database entry.
     * @param form this is in the same format as all forms, but cannot contain {@link DatabaseStrings#NOT_PREFIX}
     * @return the generated record.
     * @see com.wynprice.cafedafydd.common.utils.FormBuilder
     */
    public DatabaseRecord generateAndAddDatabase(String... form) {
        //Check for mismatched inputs
        if(form.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatched input in database generation " + this.path.getName(-1) + ". Fields: " + this.fields.toString() + ", input arguments" + Arrays.toString(form)
                + ". Input data should be in the format: `<KEY1>, <VALUE1>, <KEY2>, <VALUE2>...`");
        }
        //Create the new backing array and get the current maximum id + 1. TODO: maybe get the lowest available id, instead of the max + 1
        String[] entry = new String[this.fields.size()];
        int newId = this.entries.stream().map(DatabaseRecord::getPrimaryField).mapToInt(i -> i).max().orElse(0) + 1;

        //Go through all the form entries and set the fields in the entry to be the field value.
        //The indexOf stuff is to ensure that it all gets put in the right place, as the input form
        //Doesn't necessarily have to be in the same order as the actual fields.
        for (int i = 0; i < form.length; i+=2) {
            entry[this.fields.indexOf(form[i])] = form[i + 1];
        }

        //If any of the entries haven't been set then just set them to an empty string
        for (int i = 0; i < entry.length; i++) {
            if(entry[i] == null) {
                entry[i] = "";
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
    public boolean hasAllEntries(String... form) {
        return this.getEntries(form).count() > 0;
    }

    /**
     * Checks to see if the form is in the database, and if not then add it to the database.
     * @param form the form to check/generate
     * @return true of a form was generated, false otherwise
     */
    public boolean generateIfNotPresent(String... form) {
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
     * @param level the permission level to check with
     * @return true if they can edit it, false if otherwise.
     */
    public boolean canEdit(DatabaseRecord record, PermissionLevel level) {
        return true;
    }

    /**
     * @return the file name for this database
     */
    protected abstract String getFilename();

    /**
     * @return all the fields for this database
     */
    protected abstract String[] getFields();

    /**
     * @return the primary fields for this database. These fields are the ones that MUST have unique values.
     * NOTE ID is already included in this.
     */
    public String[] getPrimaryFields() {
        return new String[0];
    }
}
