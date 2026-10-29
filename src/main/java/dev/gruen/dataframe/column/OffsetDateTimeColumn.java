package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.OffsetDateTimeType;

import java.time.OffsetDateTime;

public class OffsetDateTimeColumn extends TemporalColumn<OffsetDateTime, OffsetDateTimeColumn> {

    private final OffsetDateTimeType valueType = new OffsetDateTimeType(getSettings());

    public OffsetDateTimeColumn() {
        super(OffsetDateTime.class);
    }

    public OffsetDateTimeColumn(String name) {
        super(name, OffsetDateTime.class);
    }

    public OffsetDateTimeColumn(String name, OffsetDateTime[] values) {
        super(name, values);
    }

    public OffsetDateTimeColumn(String name, OffsetDateTime[] values, int size) {
        super(name, values, size);
    }

    @Override
    public OffsetDateTimeType getValueType() {
        return valueType;
    }

    @Override
    protected OffsetDateTimeColumn getThis() {
        return this;
    }

    @Override
    public OffsetDateTimeColumn copy() {
        OffsetDateTime[] copyValues = new OffsetDateTime[values.length];
        toArray(copyValues);
        OffsetDateTimeColumn copy = new OffsetDateTimeColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public OffsetDateTimeColumn copyEmpty() {
        OffsetDateTimeColumn copy = new OffsetDateTimeColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
