package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.utils.UtilCollectors;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.server.PermissionLevel;
import com.wynprice.cafedafydd.server.utils.Algorithms;
import com.wynprice.cafedafydd.server.utils.AndList;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Log4j2
public abstract class Database {

    private static final String ID = "id";

    private final List<String> fields;
    private final Path path;

    private final List<DatabaseRecord> entries = new ArrayList<>();

    private final Map<String, List<DatabaseRecord>> indexedRecords = new HashMap<>();

    protected Database() {
        this.fields = Arrays.asList(this.getFields());
        this.path = Paths.get("databases").resolve(this.getFilename() + ".csv");

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

        if(this.fields.isEmpty()) {
            throw new IllegalArgumentException("Need to specify the at least one field");
        }

        this.writeToFile();

        this.reindexAll();
    }

    private void reindexAll() {
        for (String field : this.fields) {
            this.indexedRecords.put(field, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> r.getField(field))));
        }
        this.indexedRecords.put(DatabaseStrings.ID, Algorithms.quickSort(new ArrayList<>(this.entries), Comparator.comparing(r -> String.valueOf(r.getPrimaryField()))));
    }

    private void reindexRecord(DatabaseRecord record) {
        for (String field : this.fields) {
            List<DatabaseRecord> list = this.indexedRecords.get(field);
            list.remove(record);
            Algorithms.insert(list, record, Comparator.comparing(r -> r.getField(field)));
        }

        List<DatabaseRecord> list = this.indexedRecords.get(DatabaseStrings.ID);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(DatabaseRecord::getPrimaryField));
    }

    //Remove the record from the indexed records and insert it
    void reindexEntryField(DatabaseRecord record, String field) {
        List<DatabaseRecord> list = this.indexedRecords.get(field);
        list.remove(record);
        Algorithms.insert(list, record, Comparator.comparing(r -> r.getField(field)));
    }


    private void parseLine(String line, List<String> fileFields) {
        List<String> idFields = this.concat(this.fields, ID);
        List<String> arr = this.concat(new ArrayList<>(), line.split(","));
        String[] readEntries = new String[this.fields.size()];
        for (int i = 0; i < arr.size(); i++) {
            if(i >= fileFields.size()) {
                log.error("Too many fields on entry {}. Expected {}, got {}", line, fileFields.size(), arr.size());
                return;
            }
            String field = fileFields.get(i);
            String value = arr.get(i);
            if(idFields.contains(field)) {
                if(!field.equals(ID)) {
                    readEntries[idFields.indexOf(field)] = value;
                }
            } else {
                log.error("Field in {} contains field {} with value {} which doesn't exist in this database fields: {}", this.path, field, value, idFields);
                return;
            }
        }
        for (int i = 0; i < readEntries.length; i++) {
            if(readEntries[i] == null) {
                log.error("CVS file {} does not contain field {}", this.path, idFields.get(i));
                return;
            }
        }

        int id = Integer.parseInt(arr.get(fileFields.indexOf(ID)));

        if(this.checkDuplicates(readEntries, id)) {
            return;
        }

        this.entries.add(new ObservedDatabaseRecord(this, id, readEntries));
    }

    private boolean checkDuplicates(String[] readEntries, int id) {
        //Check duplicates primary fields
        for (String field : this.getPrimaryFields()) {
            int index = this.fields.indexOf(field);
            Optional<String[]> foundMatched = this.entries.stream().map(DatabaseRecord::getEntries).filter(a -> a[index].equals(readEntries[index])).findAny();

            if(foundMatched.isPresent()) {
                log.error("Found multiple entries with the same primary field {}: '{}' in file {}. Aborting found entry. Existing entry :{}, found entry {}", field, readEntries[index], this.path, Arrays.toString(foundMatched.get()), Arrays.toString(readEntries));
                return true;
            }
        }


        Optional<DatabaseRecord> foundMatched = this.entries.stream().filter(f -> f.getPrimaryField() == id).findAny();
        if(foundMatched.isPresent()) {
            log.error("Found multiple entries with the same id '{}' in file {}. Aborting found entry. Existing entry :{}, found entry {}", id, this.path, Arrays.toString(foundMatched.get().getEntries()), Arrays.toString(readEntries));
            return true;
        }
        return false;
    }

    private List<String> concat(List<String> arr, String... added) {
        List<String> newList = new ArrayList<>(arr);
        Collections.addAll(newList, added);
        return newList;
    }

    public Optional<DatabaseRecord> getEntryFromId(int id) {
        return Algorithms.doRecordSearch(this.indexedRecords.get(DatabaseStrings.ID), DatabaseRecord::getPrimaryField, id, Integer::compareTo);
    }

    public Optional<DatabaseRecord> getSingleEntry(String... aString) {
        return this.getEntries(aString).collect(UtilCollectors.toSingleEntry());
    }

    public Stream<DatabaseRecord> searchEntries(String... form) {
        return this.streamEntries((o1, o2) -> o2.contains(o1) ? 0 : o1.compareTo(o2), form);
    }

    public Stream<DatabaseRecord> getEntries(String... form) {
        return this.streamEntries(String::compareTo, form);
    }

    private Stream<DatabaseRecord> streamEntries(Comparator<String> comparator, String... form) {
        AndList<DatabaseRecord> list = new AndList<>(new ArrayList<>());
        for (int i = 0; i < form.length; i+=2) {
            boolean inverted = false;
            if(form[i].equals(DatabaseStrings.NOT_PREFIX)) {
                i++;
                inverted = true;
            }
            String finalField = form[i]; //Damn you lambda statements
            list.and(Algorithms.splicedBinarySearch(this.indexedRecords.get(finalField), r -> r.getField(finalField), form[i+1], comparator), inverted, i==0);
        }
        return list.stream();
    }

    public DatabaseRecord generateAndAddDatabase(String... formatFields) {
        if(formatFields.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatched input in database generation " + this.path.getName(-1) + ". Fields: " + this.fields.toString() + ", input arguments" + Arrays.toString(formatFields)
                + ". Input data should be in the format: `<KEY1>, <VALUE1>, <KEY2>, <VALUE2>...`");
        }
        String[] entry = new String[this.fields.size()];
        int newId = this.entries.stream().map(DatabaseRecord::getPrimaryField).mapToInt(i -> i).max().orElse(0) + 1;

        for (int i = 0; i < formatFields.length; i+=2) {
            entry[this.fields.indexOf(formatFields[i])] = formatFields[i + 1];
        }

        for (int i = 0; i < entry.length; i++) {
            if(entry[i] == null) {
                entry[i] = "";
            }
        }

        DatabaseRecord newEntry = new ObservedDatabaseRecord(this, newId, entry);
        this.entries.add(newEntry);
        this.reindexRecord(newEntry);
        this.writeToFile();
        return newEntry;
    }

    public boolean hasAllEntries(String... form) {
        return this.getEntries(form).findAny().isPresent();
    }

    public boolean generateIfNotPresent(String... form) {
        if(!this.hasAllEntries(form)) {
            this.generateAndAddDatabase(form);
            return true;
        }
        return false;
    }

    public void writeToFile() {
        if(Files.notExists(this.path.getParent())) {
            try {
                Files.createDirectories(this.path.getParent());
            } catch (IOException e) {
                log.error("Unable to create the database file", e);
                return;
            }
        }
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

    public PermissionLevel getReadLevel() {
        return PermissionLevel.STAFF_MEMBER;
    }

    public PermissionLevel getEditLevel() {
        return PermissionLevel.STAFF_MEMBER;
    }

    public boolean canEdit(DatabaseRecord record, PermissionLevel level) {
        return true;
    }

    protected abstract String getFilename();
    protected abstract String[] getFields();

    public String[] getPrimaryFields() {
        return new String[0];
    }


}
