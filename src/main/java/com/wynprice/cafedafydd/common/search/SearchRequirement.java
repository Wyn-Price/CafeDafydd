package com.wynprice.cafedafydd.common.search;

import com.wynprice.cafedafydd.common.FieldDefinition;
import com.wynprice.cafedafydd.common.FieldDefinitions;
import com.wynprice.cafedafydd.common.utils.ByteBufUtils;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.stream.IntStream;

public interface SearchRequirement {


    void serialize(ByteBuf buf);
    void deserialize(ByteBuf buf, Map<String, FieldDefinition> definitions);
    FieldDefinition getDefinition();
    byte id();

    @Data
    @EqualsAndHashCode
    @Accessors(chain = true)
    class DirectSearch implements SearchRequirement {

        private NamedRecord record;

        @Override
        public void serialize(ByteBuf buf) {
            NamedRecord.write(this.record, buf);
        }

        @Override
        public void deserialize(ByteBuf buf, Map<String, FieldDefinition> definitions) {
            this.record = NamedRecord.read(buf, definitions);
        }

        @Override
        public FieldDefinition getDefinition() {
            return this.record.getDefinition();
        }

        @Override
        public byte id() {
            return 0;
        }
    }

    class NotSearch extends DirectSearch {

        @Override
        public byte id() {
            return 1;
        }
    }

    @Data
    @EqualsAndHashCode
    @Accessors(chain = true)
    class InlineSearch implements SearchRequirement {

        private FieldDefinition definition;
        private String requestDatabase;
        private FieldDefinition<?> requestDatabaseField;
        private SearchRequirement[] form;

        @Override
        public void serialize(ByteBuf buf) {
            ByteBufUtils.writeString(this.definition.getFieldName(), buf);
            ByteBufUtils.writeString(this.requestDatabase, buf);
            ByteBufUtils.writeString(this.requestDatabaseField.getFieldName(), buf);
            SearchRequirement.write(this.form, buf);
        }

        @Override
        public void deserialize(ByteBuf buf, Map<String, FieldDefinition> definitions) {
            this.definition = definitions.get(ByteBufUtils.readString(buf));
            this.requestDatabase = ByteBufUtils.readString(buf);
            this.requestDatabaseField = definitions.get(ByteBufUtils.readString(buf));
            this.form = SearchRequirement.read(buf, this.requestDatabase);
    }

        @Override
        public byte id() {
            return 2;
        }
    }

    @Data
    @EqualsAndHashCode
    @Accessors(chain = true)
    class UserIdReference implements SearchRequirement {

        private FieldDefinition definition;

        @Override
        public void serialize(ByteBuf buf) {
            ByteBufUtils.writeString(this.definition.getFieldName(), buf);
        }

        @Override
        public void deserialize(ByteBuf buf, Map<String, FieldDefinition> definitions) {
            this.definition = definitions.get(ByteBufUtils.readString(buf));

        }

        @Override
        public byte id() {
            return 3;
        }
    }

    static SearchRequirement createNew(byte id) {
        switch (id) {
            case 0:
                return new DirectSearch();
            case 1:
                return new NotSearch();
            case 2:
                return new InlineSearch();
            case 3:
                return new UserIdReference();
            default: throw new IllegalArgumentException("Don't know how to handle search requirement with index " + id);
        }
    }

    static void write(SearchRequirement[] requirements, ByteBuf buf) {
        buf.writeShort(requirements.length);
        for (SearchRequirement requirement : requirements) {
            buf.writeByte(requirement.id());
            requirement.serialize(buf);
        }
    }

    static SearchRequirement[] read(ByteBuf buf, String database) {
        return read(buf, NamedRecord.createMap(FieldDefinitions.NAME_TO_SCHEMA.get(database)));
    }

    static SearchRequirement[] read(ByteBuf buf, Map<String, FieldDefinition> definitions) {
        return IntStream.range(0, buf.readShort()).mapToObj(i -> {
            SearchRequirement requirement = createNew(buf.readByte());
            requirement.deserialize(buf, definitions);
            return requirement;
        }).toArray(SearchRequirement[]::new);
    }


}
