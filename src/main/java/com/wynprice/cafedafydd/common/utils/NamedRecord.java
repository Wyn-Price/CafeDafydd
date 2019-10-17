package com.wynprice.cafedafydd.common.utils;

import com.wynprice.cafedafydd.common.DatabaseField;
import com.wynprice.cafedafydd.common.FieldDefinition;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import com.wynprice.cafedafydd.common.search.SearchRequirement;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
@ToString
@RequiredArgsConstructor(staticName = "of")
public class NamedRecord<T> implements SearchRequirement {
    private final FieldDefinition<T> definition;
    private final DatabaseField<T> record;

    public static Map<String, FieldDefinition> createMap(FieldDefinition[] definitions) {
        Map<String, FieldDefinition> map = Arrays.stream(definitions).collect(UtilCollectors.toHashMap(FieldDefinition::getFieldName, d -> d));
        map.put(FieldDefinitions.ID.getFieldName(), FieldDefinitions.ID);
        return map;
    }

    public static void write(NamedRecord[] records, ByteBuf buf) {
        buf.writeShort(records.length);
        for (NamedRecord record : records) {
            write(record, buf);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        write(this, buf);
    }

    @Override
    public void deserialize(ByteBuf buf, Map<String, FieldDefinition> definitions) {
        throw new UnsupportedOperationException("Cannot deserialize onto the read only named object");
    }

    @Override
    public byte id() {
        return 0;
    }

    public CharSequence getToString() {
        return this.definition.getRecordType().getAsFileString(this.record.getData());
    }

    public void setInto(DatabaseRecord record) {
        record.set(this.definition, this.record.getData());
    }

    public static NamedRecord[] read(ByteBuf buf, String databaseName) {
        Map<String, FieldDefinition> definitions = createMap(FieldDefinitions.NAME_TO_SCHEMA.get(databaseName));
        return IntStream.range(0, buf.readShort())
            .mapToObj(i -> read(buf, definitions))
            .toArray(NamedRecord[]::new);
    }

    public static void write(NamedRecord record, ByteBuf buf) {
        ByteBufUtils.writeString(record.getDefinition().getFieldName(), buf);
        record.getRecord().serialize(buf);
    }

    public static NamedRecord read(ByteBuf buf, Map<String, FieldDefinition> definitions) {
        String fieldName = ByteBufUtils.readString(buf);
        FieldDefinition<?> definition = definitions.get(fieldName);
        return createFromDefinitionAndBuff(definition, buf);

    }

    private static <T> NamedRecord<T> createFromDefinitionAndBuff(FieldDefinition<T> definition, ByteBuf buf) {
        DatabaseField<T> empty = definition.getRecordType().createEmpty();
        definition.getRecordType().deserialize(buf, empty);
        return of(definition, empty);
    }
}
