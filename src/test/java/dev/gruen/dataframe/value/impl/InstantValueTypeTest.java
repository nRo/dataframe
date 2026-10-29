package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.type.impl.InstantType;

import java.time.Instant;

public class InstantValueTypeTest extends AbstractValueTypeTest<Instant, InstantType> {
    @Override
    public InstantType getValueType() {
        return new InstantType();
    }
}
