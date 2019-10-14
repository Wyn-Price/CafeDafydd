package com.wynprice.cafedafydd.common.netty.packets.clientbound;

import com.wynprice.cafedafydd.common.BackupHeader;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Value
public class PacketBackupEntryResult {
    private final int requestID;
    private final List<String> entryList;

    public static void encode(PacketBackupEntryResult packet, ByteBuf buf) {
        buf.writeInt(packet.requestID);

        buf.writeShort(packet.entryList.size());
        for (String entry : packet.entryList) {
            ByteBufUtils.writeString(entry, buf);
        }
    }

    public static PacketBackupEntryResult decode(ByteBuf buf) {
        return new PacketBackupEntryResult(
            buf.readInt(),
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> ByteBufUtils.readString(buf))
                .collect(Collectors.toList())
        );
    }
}
