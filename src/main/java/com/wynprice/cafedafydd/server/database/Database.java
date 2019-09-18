package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.client.utils.UtilCollectors;
import com.wynprice.cafedafydd.common.DatabaseStrings;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import lombok.Value;
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
        return this.entries.stream()
            .filter(arr -> arr.getPrimaryField() == id)
            .collect(UtilCollectors.toSingleEntry());
    }

    public Optional<DatabaseRecord> getSingleEntry(String... aString) {
        return this.getEntries(aString).collect(UtilCollectors.toSingleEntry());
    }

    public boolean remove(int id) {
        boolean ret = this.entries.removeIf(r -> r.getPrimaryField() == id);
        this.writeToFile();
        return ret;
    }

    public Stream<DatabaseRecord> getEntries(String... aString) {
        return this.entries.stream().filter(fieldEntry -> {
            boolean value = true;
            for (int i = 0; i < aString.length; i+=2) {
                if(DatabaseStrings.ID.equals(aString[i])) {
                    value &= String.valueOf(fieldEntry.getPrimaryField()).equals(aString[i + 1]);
                } else {
                    value &= fieldEntry.getEntries()[this.fields.indexOf(aString[i])].equals(aString[i + 1]);
                }
            }
            return value;
        });
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
        this.writeToFile();
        return newEntry;
    }

    public boolean hasAllEntries(String... aString) {
        return this.entries.stream().anyMatch(fieldEntry -> {
            boolean value = true;
            for (int i = 0; i < aString.length; i+=2) {
                value &= fieldEntry.getEntries()[this.fields.indexOf(aString[i])].equals(aString[i + 1]);
            }
            return value;
        });
    }

    public boolean generateIfNotPresent(String... aString) {
        if(!this.hasAllEntries(aString)) {
            this.generateAndAddDatabase(aString);
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

    protected abstract String getFilename();
    protected abstract String[] getFields();

    protected String[] getPrimaryFields() {
        return new String[0];
    }


}
