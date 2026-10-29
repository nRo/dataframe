package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.ZonedDateTimeType;

import java.time.ZonedDateTime;

public class ZonedDateTimeColumn extends TemporalColumn<ZonedDateTime, ZonedDateTimeColumn> {

    private final ZonedDateTimeType valueType = new ZonedDateTimeType(getSettings());

    public ZonedDateTimeColumn() {
        super(ZonedDateTime.class);
    }

    public ZonedDateTimeColumn(String name) {
        super(name, ZonedDateTime.class);
    }

    public ZonedDateTimeColumn(String name, ZonedDateTime[] values) {
        super(name, values);
    }

    public ZonedDateTimeColumn(String name, ZonedDateTime[] values, int size) {
        super(name, values, size);
    }

    @Override
    public ZonedDateTimeType getValueType() {
        return valueType;
    }

    @Override
    protected ZonedDateTimeColumn getThis() {
        return this;
    }

    @Override
    public ZonedDateTimeColumn copy() {
        ZonedDateTime[] copyValues = new ZonedDateTime[values.length];
        toArray(copyValues);
        ZonedDateTimeColumn copy = new ZonedDateTimeColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public ZonedDateTimeColumn copyEmpty() {
        ZonedDateTimeColumn copy = new ZonedDateTimeColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
