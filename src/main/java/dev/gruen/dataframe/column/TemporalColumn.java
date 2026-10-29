package dev.gruen.dataframe.column;

import dev.gruen.dataframe.DataFrameRuntimeException;
import dev.gruen.dataframe.Values;
import dev.gruen.dataframe.common.Row;

import java.text.ParseException;
import java.time.temporal.TemporalAccessor;

/**
 * Base class of the columns for {@code java.time} values.
 * <p>
 * Text values read from a row are parsed with the value type of this column.
 * The format and the time zone of the column
 * (see {@link dev.gruen.dataframe.settings.TemporalFormatSetting} and
 * {@link dev.gruen.dataframe.settings.TimeZoneSetting}) are therefore used.
 *
 * @param <T> temporal type
 * @param <C> column type
 */
public abstract class TemporalColumn<T extends TemporalAccessor, C extends TemporalColumn<T, C>> extends BasicColumn<T, C> {

    public TemporalColumn(String name, Class<T> cl) {
        super(name, cl);
    }

    public TemporalColumn(Class<T> cl) {
        super(cl);
    }

    public TemporalColumn(String name, T[] values) {
        super(name, values);
    }

    public TemporalColumn(String name, T[] values, int size) {
        super(name, values, size);
    }

    @Override
    public <H> T getValueFromRow(Row<?, H> row, H headerName) {
        return fromRowValue(row.get(headerName));
    }

    @Override
    public T getValueFromRow(Row<?, ?> row, int headerIndex) {
        return fromRowValue(row.get(headerIndex));
    }

    private T fromRowValue(Object value) {
        if (value == null || Values.NA.isNA(value)) {
            return null;
        }
        Class<T> type = getValueType().getType();
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        String text = value.toString();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return getValueType().parse(text);
        } catch (ParseException e) {
            throw new DataFrameRuntimeException(e.getMessage(), e);
        }
    }
}
