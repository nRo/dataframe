package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.ZonedDateTimeType;

import java.time.ZonedDateTime;

public class ZonedDateTimeValueTypeTest extends AbstractValueTypeTest<ZonedDateTime, ZonedDateTimeType> {
    @Override
    public ZonedDateTimeType getValueType() {
        return new ZonedDateTimeType();
    }
}
