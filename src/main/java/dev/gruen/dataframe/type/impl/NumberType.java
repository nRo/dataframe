package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.common.NumberUtil;
import dev.gruen.dataframe.settings.ColumnSettings;

public abstract class NumberType<T extends Number & Comparable<T>> extends ComparableType<T> {
    protected NumberType() {
        super();
    }

    protected NumberType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public T convertRaw(Object o) {
        if (o instanceof Number) {
            return NumberUtil.convert((Number) o, getType());
        }
        return super.convertRaw(o);
    }

    @Override
    public int compareRaw(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return NumberUtil.compare((Number) a, (Number) b);
        }
        return super.compareRaw(a, b);
    }
}
