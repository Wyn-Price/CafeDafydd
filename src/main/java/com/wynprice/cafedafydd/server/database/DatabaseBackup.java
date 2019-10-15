package com.wynprice.cafedafydd.server.database;

import com.wynprice.cafedafydd.common.BackupHeader;
import com.wynprice.cafedafydd.server.utils.CompressionUtils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public class DatabaseBackup implements AutoCloseable {
    private static final long MS_TIME_BETWEEN_BACKUPS = TimeUnit.MINUTES.toMillis(5);
    private static final int HEADER_SIZE_BYTES = Long.BYTES + Integer.BYTES; //time, size

    private final Database database;

    private final File baseFile;
    private final RandomAccessFile file;

    private long prevBackupTime;

    @Getter
    private final List<BackupHeader> headers = new LinkedList<>();

    public DatabaseBackup(Database database) {
        this.database = database;

        this.baseFile = Paths.get("databases").resolve("backups").resolve(database.getFilename() + ".dbb").toFile();
        if(!this.baseFile.exists()) {
            if (!this.baseFile.getParentFile().exists() && !this.baseFile.getParentFile().mkdir()) {
                throw new IllegalStateException("Should be able to create file at :" + this.baseFile.getParentFile().getAbsolutePath());
            }
            try {
                if(!this.baseFile.createNewFile()) {
                    throw new RuntimeException("Unable to create backup file " + this.baseFile.getAbsolutePath());
                }
            } catch (IOException e) {
                throw  new RuntimeException("Unable to create backup file " + this.baseFile.getAbsolutePath(), e);
            }
        }
        try {
            this.file = new RandomAccessFile(this.baseFile, "rw");
        } catch (IOException e) {
            throw new RuntimeException("Unable to load backup file " + this.baseFile.getAbsolutePath(), e);
        }

        this.readHeaders();
    }

    public void onChanged() {
        try {
            List<String> collected = this.database.getFileGeneratedList();

            @Cleanup ByteArrayOutputStream baos = new ByteArrayOutputStream();

            @Cleanup DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(collected.size());
            for (String s : collected) {
                dos.writeUTF(s);
            }

            byte[] compressed = CompressionUtils.compress(baos.toByteArray());


            BackupHeader header = new BackupHeader(this.headers.size(), this.file.length(), new Date(System.currentTimeMillis()), compressed.length);
            this.headers.add(header);

            //Append a new entry
            this.file.seek(header.getIndex());
            this.file.writeLong(header.getBackupTime().getTime());
            this.file.writeInt(header.getSize());
            this.file.write(compressed);
        } catch (IOException e) {
            log.error("Unable to write to database file: " + this.baseFile.getAbsolutePath(), e);
        }
    }

    private void readHeaders() {
        this.headers.clear();
        try {
            this.file.seek(0);
            while (this.file.getFilePointer() < this.file.length()) {
                BackupHeader header = new BackupHeader(this.headers.size(), this.file.getFilePointer(), new Date(this.file.readLong()), this.file.readInt());
                this.headers.add(header);
                //Don't overshoot it and accidentally generate more file
                if(this.file.getFilePointer() + header.getSize() >= this.file.length()) {
                    break;
                }
                this.file.seek(this.file.getFilePointer() + header.getSize());
            }
        } catch (IOException e) {
            log.error("Unable to parse database backup: " + this.baseFile.getAbsolutePath(), e);
        }
    }

    public void revertToBackup(int id) {
        this.database.readAllLines(this.getFileListForId(id));
    }

    public List<String> getFileListForId(int id) {
        List<String> fileLines = new ArrayList<>();
        BackupHeader header = this.headers.get(id);

        try {
            this.file.seek(header.getIndex() + HEADER_SIZE_BYTES);
            byte[] read = new byte[header.getSize()];
            this.file.readFully(read);
            byte[] uncompressed = CompressionUtils.uncompress(read);

            @Cleanup DataInputStream dis = new DataInputStream(new ByteArrayInputStream(uncompressed));

            int len = dis.readInt();
            for (int i = 0; i < len; i++) {
                fileLines.add(dis.readUTF());
            }

        } catch (IOException e) {
            log.error("Unable to get file for header: " + header, e);
        }
        return fileLines;
    }


    @Override
    public void close() throws Exception {
        this.file.close();
    }
}
