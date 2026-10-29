package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.LocalTimeType;

import java.time.LocalTime;

public class LocalTimeColumn extends TemporalColumn<LocalTime, LocalTimeColumn> {

    private final LocalTimeType valueType = new LocalTimeType(getSettings());

    public LocalTimeColumn() {
        super(LocalTime.class);
    }

    public LocalTimeColumn(String name) {
        super(name, LocalTime.class);
    }

    public LocalTimeColumn(String name, LocalTime[] values) {
        super(name, values);
    }

    public LocalTimeColumn(String name, LocalTime[] values, int size) {
        super(name, values, size);
    }

    @Override
    public LocalTimeType getValueType() {
        return valueType;
    }

    @Override
    protected LocalTimeColumn getThis() {
        return this;
    }

    @Override
    public LocalTimeColumn copy() {
        LocalTime[] copyValues = new LocalTime[values.length];
        toArray(copyValues);
        LocalTimeColumn copy = new LocalTimeColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public LocalTimeColumn copyEmpty() {
        LocalTimeColumn copy = new LocalTimeColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
