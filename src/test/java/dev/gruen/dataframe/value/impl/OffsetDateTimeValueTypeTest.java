package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.OffsetDateTimeType;

import java.time.OffsetDateTime;

public class OffsetDateTimeValueTypeTest extends AbstractValueTypeTest<OffsetDateTime, OffsetDateTimeType> {
    @Override
    public OffsetDateTimeType getValueType() {
        return new OffsetDateTimeType();
    }
}
