package com.wynprice.cafedafydd.server.utils;

import lombok.Cleanup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtils {

    public static byte[] compress(byte[] arr) throws IOException {
        @Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream(arr.length);
        @Cleanup GZIPOutputStream gzos = new GZIPOutputStream(bos);
        gzos.write(arr);
        gzos.close();
        return bos.toByteArray();
    }

    public static byte[] uncompress(byte[] arr) throws IOException {
        @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);
        @Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while((len = gzis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }

        return bos.toByteArray();
    }
}
