package dev.gruen.dataframe.column;

import dev.gruen.dataframe.type.impl.InstantType;

import java.time.Instant;

public class InstantColumn extends TemporalColumn<Instant, InstantColumn> {

    private final InstantType valueType = new InstantType(getSettings());

    public InstantColumn() {
        super(Instant.class);
    }

    public InstantColumn(String name) {
        super(name, Instant.class);
    }

    public InstantColumn(String name, Instant[] values) {
        super(name, values);
    }

    public InstantColumn(String name, Instant[] values, int size) {
        super(name, values, size);
    }

    @Override
    public InstantType getValueType() {
        return valueType;
    }

    @Override
    protected InstantColumn getThis() {
        return this;
    }

    @Override
    public InstantColumn copy() {
        Instant[] copyValues = new Instant[values.length];
        toArray(copyValues);
        InstantColumn copy = new InstantColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public InstantColumn copyEmpty() {
        InstantColumn copy = new InstantColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
