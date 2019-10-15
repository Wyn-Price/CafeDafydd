package com.wynprice.cafedafydd.common.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

/**
 * A utility class for the {@link ByteBuf}
 */
public class ByteBufUtils {

    //Unicode 8
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Writes the string to the buffer with {@link #UTF_8}
     * @param string the string to write
     * @param buf the buffer to write to
     */
    public static void writeString(String string, ByteBuf buf) {
        buf.writeShort(string.length());
        buf.writeCharSequence(string, UTF_8);
    }

    /**
     * Writes the string to the buffer with {@link #UTF_8}
     * @param buf the buffer to write to
     * @param string the string to write
     */
    public static void writeString(ByteBuf buf, String string) {
        buf.writeShort(string.length());
        buf.writeCharSequence(string, UTF_8);
    }


    /**
     * Reads the string from the buffer, using {@link #UTF_8}
     * @param buf the buffer to read from
     * @return the string contained in the buffer
     */
    public static String readString(ByteBuf buf) {
        return buf.readCharSequence(buf.readShort(), UTF_8).toString();
    }
}
