package com.wynprice.cafedafydd.common.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class ByteBufUtils {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static void writeString(String string, ByteBuf buf) {
        buf.writeShort(string.length());
        buf.writeCharSequence(string, CHARSET);
    }

    public static String readString(ByteBuf buf) {
        return buf.readCharSequence(buf.readShort(), CHARSET).toString();
    }
}
