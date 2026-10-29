package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.LocalDateTimeType;

import java.time.LocalDateTime;

public class LocalDateTimeColumn extends TemporalColumn<LocalDateTime, LocalDateTimeColumn> {

    private final LocalDateTimeType valueType = new LocalDateTimeType(getSettings());

    public LocalDateTimeColumn() {
        super(LocalDateTime.class);
    }

    public LocalDateTimeColumn(String name) {
        super(name, LocalDateTime.class);
    }

    public LocalDateTimeColumn(String name, LocalDateTime[] values) {
        super(name, values);
    }

    public LocalDateTimeColumn(String name, LocalDateTime[] values, int size) {
        super(name, values, size);
    }

    @Override
    public LocalDateTimeType getValueType() {
        return valueType;
    }

    @Override
    protected LocalDateTimeColumn getThis() {
        return this;
    }

    @Override
    public LocalDateTimeColumn copy() {
        LocalDateTime[] copyValues = new LocalDateTime[values.length];
        toArray(copyValues);
        LocalDateTimeColumn copy = new LocalDateTimeColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public LocalDateTimeColumn copyEmpty() {
        LocalDateTimeColumn copy = new LocalDateTimeColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
