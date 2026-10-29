package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.LocalDateTimeType;

import java.time.LocalDateTime;

public class LocalDateTimeValueTypeTest extends AbstractValueTypeTest<LocalDateTime, LocalDateTimeType> {
    @Override
    public LocalDateTimeType getValueType() {
        return new LocalDateTimeType();
    }
}
