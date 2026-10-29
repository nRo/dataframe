package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.LocalDateType;

import java.time.LocalDate;

public class LocalDateValueTypeTest extends AbstractValueTypeTest<LocalDate, LocalDateType> {
    @Override
    public LocalDateType getValueType() {
        return new LocalDateType();
    }
}
