package com.wynprice.cafedafydd.server.utils;

import lombok.Cleanup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The class holding all the algorithms used to compress and uncompress data.
 */
public class CompressionUtils {

    /**
     * Compresses the given data using the gzip algorithm.
     * @param arr the data to compress
     * @return the compressed version of {@code arr}
     * @see #uncompress(byte[])
     * @see GZIPOutputStream
     */
    public static byte[] compress(byte[] arr) throws IOException {
        //Create the output stream to get the byte array from. 
        //Create the gzip stream to compress the data, then write all the data to the gzip stream, and get the resulting byte array.
        @Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream(arr.length);
        @Cleanup GZIPOutputStream gzos = new GZIPOutputStream(bos);
        gzos.write(arr);
        gzos.close();
        return bos.toByteArray();
    }

    /**
     * Uncompresses the given data using the gzip algorithm.
     * @param arr the data to uncompress
     * @return the uncompressed version of {@code arr}
     * @see GZIPInputStream
     * @see #compress(byte[])
     */
    public static byte[] uncompress(byte[] arr) throws IOException {
        //Create the output stream to write too.  
        @Cleanup ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        //Put the input array into an input stream, and setup a gzip stream to uncompress the data.
        @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        @Cleanup GZIPInputStream gzis = new GZIPInputStream(bais);

        //The buffer is used to write from the gzip stream to the output array stream.
        //We can't write all the data at once, so using the buffer allows me to write the data
        //from one stream to another in chunks of 1024
        byte[] buffer = new byte[1024];
        int len;
        while((len = gzis.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }

        return bos.toByteArray();
    }
}
