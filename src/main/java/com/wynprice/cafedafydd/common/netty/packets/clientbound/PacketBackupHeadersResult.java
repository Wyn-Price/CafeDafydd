package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.BackupHeader;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
public class PacketBackupHeadersResult {
    private final int requestID;
    private final List<BackupHeader> headerList;

    public static void encode(PacketBackupHeadersResult packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);

        buf.writeShort(packet.headerList.size());
        for (BackupHeader header : packet.headerList) {
            buf.writeInt(header.getId());
            buf.writeLong(header.getIndex());
            buf.writeLong(header.getBackupTime().getTime());
            buf.writeInt(header.getSize());
        }
    }

    public static PacketBackupHeadersResult decode(ByteBuf buf) {
        return new PacketBackupHeadersResult(
            buf.readInt(),
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> new BackupHeader(buf.readInt(), buf.readLong(), new Date(buf.readLong()), buf.readInt()))
                .collect(Collectors.toList())
        );
    }
}
