package com.wynprice.cafedafydd.common;

import lombok.Value;

import java.util.Date;

@Value
public class BackupHeader {
    private final int id;
    private final long index;
    private final Date backupTime;
    private final int size;
}
