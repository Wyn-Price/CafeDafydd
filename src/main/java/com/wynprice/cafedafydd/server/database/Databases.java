package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.client.utils.UtilCollectors;
import com.wynprice.cafedafydd.server.PermissionLevel;
import com.wynprice.cafedafydd.server.utils.OptionalMap;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.omg.CORBA.IntHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public abstract class Databases {

    public static final Users USERS = new Users();

    private final List<String> fields;
    private final Path path;

    private final List<FieldEntry> entries = new ArrayList<>();

    private Databases() {
        this.fields = Arrays.asList(this.getFields());
        this.path = Paths.get("databases").resolve(this.getFilename() + ".csv");

        if(Files.exists(this.path)) {
            try {
                for (String line : Files.readAllLines(this.path)) {
                    String[] arr = line.split(",");
                    String[] readEntries = new String[arr.length - 1];
                    System.arraycopy(arr, 1, readEntries, 0, readEntries.length);
                    this.entries.add(new FieldEntry(Integer.parseInt(arr[0]), readEntries));
                }
            } catch (IOException e) {
                log.error("Unable to open csv file");
            }
        }

        if(this.fields.isEmpty()) {
            throw new IllegalArgumentException("Need to specify the at least one field");
        }
    }

    public OptionalMap<String, String> getEntryFromId(int id) {
        return this.entries.stream()
            .filter(arr -> arr.getPrimaryField() == id)
            .map(entry -> {
                OptionalMap<String, String> map = new OptionalMap<>(new HashMap<>());
                for (int i = 1; i < this.fields.size(); i++) {
                    map.put(this.fields.get(i), entry.getEntries()[i]);
                }
                return map;
            })
            .collect(UtilCollectors.toSingleEntry())
            .orElse(OptionalMap.emptyMap());
    }

    public Optional<Integer> getSingleIdFromEntry(String... aString) {
        return this.getIdsFromEntries(aString).collect(UtilCollectors.toSingleEntry());
    }

    public Stream<Integer> getIdsFromEntries(String... aString) {
        return this.entries.stream().filter(fieldEntry -> {
            boolean value = true;
            for (int i = 0; i < aString.length; i+=2) {
                value &= fieldEntry.getEntries()[this.fields.indexOf(aString[i])].equals(aString[i + 1]);
            }
            return value;
        }).map(FieldEntry::getPrimaryField);
    }

    public FieldEntry generateAndAddDatabase(String... formatFields) {
        if(formatFields.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatched input in database generation " + this.path.getName(-1) + ". Fields: " + this.fields.toString() + ", input arguments" + Arrays.toString(formatFields)
                + ". Input data should be in the format: `<KEY1>, <VALUE1>, <KEY2>, <VALUE2>...`");
        }
        String[] entry = new String[this.fields.size()];
        int newId = this.entries.stream().map(FieldEntry::getPrimaryField).mapToInt(i -> i).max().orElse(0);

        for (int i = 0; i < formatFields.length; i+=2) {
            entry[this.fields.indexOf(formatFields[i])] = formatFields[i + 1];
        }

        FieldEntry newEntry = new FieldEntry(newId, entry);
        this.entries.add(newEntry);
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
            Files.write(this.path, this.entries.stream().map(FieldEntry::toFileString).collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Unable to write database file " + this.path.getName(this.path.getNameCount() - 1), e);
        }
    }

    protected abstract String getFilename();
    protected abstract String[] getFields();


    public static class Users extends Databases {

        private static final String ADMIN_USERNAME = "admin";
        private static final String ADMIN_PASSWORD_HASH = "0503cde860c5c486b73cfd25e05bb5c54f75d4107225215e52148a6270c0aa3c";

        public Users() {
            if(!this.hasAllEntries(USERNAME, ADMIN_USERNAME, PASSWORD_HASH, ADMIN_PASSWORD_HASH, PERMISSION_LEVEL, PermissionLevel.ADMINISTRATOR.name())) {
                this.generateAndAddDatabase(USERNAME, ADMIN_USERNAME, PASSWORD_HASH, ADMIN_PASSWORD_HASH, PERMISSION_LEVEL, PermissionLevel.ADMINISTRATOR.name());
                this.writeToFile();
            }
        }

        public static final String FILE_NAME = "users";

        public static final String USERNAME = "username";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String PERMISSION_LEVEL = "permission_level";

        @Override
        protected String getFilename() {
            return FILE_NAME;
        }

        @Override
        protected String[] getFields() {
            return new String[] {USERNAME, PASSWORD_HASH, PERMISSION_LEVEL};
        }
    }

    @Value
    public final class FieldEntry {
        private final int primaryField;
        private final String[] entries;

        public String toFileString() {
            return this.primaryField + "," + String.join(",", this.entries);
        }
    }
}
