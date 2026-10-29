package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;
import dev.gruen.dataframe.type.ValueType;

import java.util.Comparator;

public abstract class ComparableType<T extends Comparable<T>> extends ValueType<T> {
    protected ComparableType() {
        super();
    }

    protected ComparableType(ColumnSettings settings) {
        super(settings);
    }

    private final Comparator<T> defaultComparator = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.compareTo(o2);
    };

    @Override
    public Comparator<T> getComparator() {
        return defaultComparator;
    }
}
