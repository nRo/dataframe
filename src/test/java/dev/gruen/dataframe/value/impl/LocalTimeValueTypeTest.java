package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.LocalTimeType;

import java.time.LocalTime;

public class LocalTimeValueTypeTest extends AbstractValueTypeTest<LocalTime, LocalTimeType> {
    @Override
    public LocalTimeType getValueType() {
        return new LocalTimeType();
    }
}
