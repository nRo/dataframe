package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.LocalDateType;

import java.time.LocalDate;

public class LocalDateColumn extends TemporalColumn<LocalDate, LocalDateColumn> {

    private final LocalDateType valueType = new LocalDateType(getSettings());

    public LocalDateColumn() {
        super(LocalDate.class);
    }

    public LocalDateColumn(String name) {
        super(name, LocalDate.class);
    }

    public LocalDateColumn(String name, LocalDate[] values) {
        super(name, values);
    }

    public LocalDateColumn(String name, LocalDate[] values, int size) {
        super(name, values, size);
    }

    @Override
    public LocalDateType getValueType() {
        return valueType;
    }

    @Override
    protected LocalDateColumn getThis() {
        return this;
    }

    @Override
    public LocalDateColumn copy() {
        LocalDate[] copyValues = new LocalDate[values.length];
        toArray(copyValues);
        LocalDateColumn copy = new LocalDateColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public LocalDateColumn copyEmpty() {
        LocalDateColumn copy = new LocalDateColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
