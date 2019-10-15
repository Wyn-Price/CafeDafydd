package com.wynprice.cafedafydd.common;

import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Setter
@Getter
@Accessors(chain = true)
public class RecordType<T> {

    public static final RecordType<Boolean> BOOLEAN_TYPE = new RecordType<>(
        ByteBuf::writeBoolean,
        ByteBuf::readBoolean,
        String::valueOf,
        reader -> "1".equals(reader.getNextEntry()),
        () -> false
    );

    public static final RecordType<Date> DATE_TYPE = new RecordType<>(
        (buf, data) -> buf.writeLong(data.getTime()),
        buf -> new Date(buf.readLong()),
        DateUtils::toISO8691,
        reader -> DateUtils.fromISO8691(reader.getNextEntry(), false),
        () -> DateUtils.EMPTY_DATE
    );

    public static final RecordType<Float> FLOAT_TYPE = new RecordType<>(
        ByteBuf::writeFloat,
        ByteBuf::readFloat,
        String::valueOf,
        reader -> Float.valueOf(reader.getNextEntry()),
        () -> 0F
    );

    public static final RecordType<Integer> INTEGER_TYPE = new RecordType<>(
        ByteBuf::writeInt,
        ByteBuf::readInt,
        String::valueOf,
        reader -> Integer.valueOf(reader.getNextEntry()),
        () -> 0
    );

    public static final RecordType<String> STRING_TYPE = new RecordType<>(
        ByteBufUtils::writeString,
        ByteBufUtils::readString,
        s -> s,
        FileLineReader::getNextEntry,
        () -> ""
    );

    @SuppressWarnings("unchecked")
    public static <T> RecordType<T[]> createArray(RecordType<T> arrayType) {
        return new RecordType<>((buf, ts) -> {
            buf.writeShort(ts.length);
            for (T t : ts) {
                arrayType.serializer.accept(buf, t);
            }
        }, buf -> {
            short len = buf.readShort();
            T[] arr = (T[]) new Object[len];
            for (int i = 0; i < len; i++) {
                arr[i] = arrayType.deserializer.apply(buf);
            }
            return arr;
        }, ts -> {
            StringBuilder builder = new StringBuilder();

            builder.append('[');

            for (int i = 0; i < ts.length; i++) {
                if(i != 0) {
                    builder.append(',');
                }
                builder.append(arrayType.getAsFileString(ts[i]));
            }
            builder.append(']');

            return builder;
        }, reader -> {
            List<T> arr = new ArrayList<>();
            FileLineReader lineReader = new FileLineReader(reader.getNextEntry());
            while (lineReader.hasMore()) {
                arr.add(arrayType.fromFile.apply(lineReader));
            }
            return (T[]) arr.toArray();
        }, () -> (T[]) new Object[0]);
    }

    public RecordType(BiConsumer<ByteBuf, T> serializer, Function<ByteBuf, T> deserializer, Function<T, CharSequence> toFileString, Function<FileLineReader, T> fromFile, Supplier<T> emptyType) {
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.toFileString = toFileString;
        this.fromFile = fromFile;
        this.emptyType = emptyType;
        this.toCompareString = t -> this.toFileString.apply(t).toString();
    }


    private final BiConsumer<ByteBuf, T> serializer;
    private final Function<ByteBuf, T> deserializer;
    private final Function<T, CharSequence> toFileString;
    private final Function<FileLineReader, T> fromFile;
    private final Supplier<T> emptyType;

    private Function<T, String> toCompareString;

    public void serialize(ByteBuf buf, DatabaseField<T> field) {
        this.serializer.accept(buf, field.getData());
    }

    public void deserialize(ByteBuf buf, DatabaseField<T> field) {
        field.setData(this.deserializer.apply(buf));
    }

    public CharSequence getAsFileString(T data) {
        return this.toFileString.apply(data);
    }

    public String getCompareString(T data) {
        return this.toCompareString.apply(data);
    }

    public DatabaseField<T> parseFromFile(FileLineReader reader) {
        return this.createWith(this.fromFile.apply(reader));
    }

    public DatabaseField<T> createEmpty() {
        DatabaseField<T> field = new DatabaseField<>(this);
        field.setData(this.emptyType.get());
        return field;
    }

    public DatabaseField<T> createWith(T data) {
        DatabaseField<T> field = this.createEmpty();
        field.setData(data);
        return field;
    }
}
