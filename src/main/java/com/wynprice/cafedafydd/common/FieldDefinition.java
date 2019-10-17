package com.wynprice.cafedafydd.common;

import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.NamedRecord;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.BiFunction;

@Data
@ToString
@RequiredArgsConstructor(staticName = "of")
public class FieldDefinition<T> {
    private final String fieldName;
    private final RecordType<T> recordType;

    public NamedRecord<T> create(T data) {
        return NamedRecord.of(this, this.recordType.createWith(data));
    }

    public <R> R getResult(DatabaseRecord record, BiFunction<RecordType<T>, T, R> func) {
        return func.apply(this.recordType, record.get(this));
    }
}
