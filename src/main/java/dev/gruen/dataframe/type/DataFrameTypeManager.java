/*
 *
 *  * Copyright (c) 2019-2026 Alexander Grün
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package dev.gruen.dataframe.type;

import dev.gruen.dataframe.DataFrameColumn;
import dev.gruen.dataframe.DataFrameRuntimeException;
import dev.gruen.dataframe.column.*;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Alex on 22.06.2016.
 */
public class DataFrameTypeManager {

    private static final List<DataFrameColumn<?, ?>> DEFAULT_COLUMN = List.of(
            new StringColumn(),
            new CharacterColumn(),
            new DoubleColumn(),
            new IntegerColumn(),
            new FloatColumn(),
            new LongColumn(),
            new BooleanColumn(),
            new ShortColumn(),
            new ByteColumn(),
            new LocalDateColumn(),
            new LocalTimeColumn(),
            new LocalDateTimeColumn(),
            new InstantColumn(),
            new OffsetDateTimeColumn(),
            new ZonedDateTimeColumn()
    );

    private static final DataFrameTypeManager defaultInstance = createNew();

    private final Map<Class<?>, DataFrameColumn<?, ?>> columnValueTypeMap = new HashMap<>();
    private final Map<Class<? extends DataFrameColumn>, DataFrameColumn<?, ?>> columnTypesMap = new HashMap<>();
    private final List<Class<?>> customTypes = new ArrayList<>();

    /**
     * Do not instantiate ColumnConverter.
     */
    private DataFrameTypeManager() {
        DEFAULT_COLUMN.forEach(this::add);
    }

    private void add(DataFrameColumn<?, ?> col) {
        this.columnTypesMap.put(col.getClass(), col);
        this.columnValueTypeMap.put(col.getValueType().getType(), col);
    }


    /**
     * Returns a data frame column type for a provided column value type
     *
     * @param type column value class
     * @param <T>  column type
     * @param <C>  value type
     * @return column matching the value type
     */
    @SuppressWarnings("unchecked")
    public <T, C extends DataFrameColumn<T, ?>> Class<C> getColumnType(Class<T> type) {
        DataFrameColumn<?, ?> column = columnValueTypeMap.get(type);
        if (column == null) {
            throw new DataFrameRuntimeException(String.format("no column type found for value type '%s'", type.getCanonicalName()));
        }
        return (Class<C>) column.getClass();
    }


    /**
     * Adds a new column type for a column value type
     *
     * @param col column class
     * @param <T> column value type
     * @param <C> column type
     * @return <code>self</code> for method chaining
     */
    public <T, C extends DataFrameColumn<T, C>> DataFrameTypeManager register(DataFrameColumn<?, ?> col) {
        col = col.copyEmpty();
        add(col);
        customTypes.add(col.getValueType().getType());
        return this;
    }

    public boolean isRegistered(DataFrameColumn<?, ?> col) {
        return isRegistered(col.getClass());
    }

    public boolean isRegistered(Class<? extends DataFrameColumn> colClass) {
        return columnTypesMap.containsKey(colClass);
    }

    /**
     * removes a custom column type
     *
     * @param col column class
     */
    public void unregister(DataFrameColumn<?, ?> col) {
        if (!typeExists(col.getValueType().getType())) {
            return;
        }
        if (customTypes.remove(col.getValueType().getType())) {
            columnValueTypeMap.remove(col.getValueType().getType());
            columnTypesMap.remove(col.getClass());
            //Add default type if required
            DEFAULT_COLUMN.forEach(c -> {
                if (c.getValueType().getType().equals(col.getValueType().getType())) {
                    add(c);
                }
            });
        }
    }

    public List<Class<?>> getCustomTypes() {
        return Collections.unmodifiableList(customTypes);
    }

    /**
     * Returns a data frame column for a provided column value type
     *
     * @param type column value class
     * @param <T>  column type
     * @return column matching the value type
     */
    @SuppressWarnings("unchecked")
    public <T> DataFrameColumn<T, ?> createColumnForType(Class<T> type) {
        DataFrameColumn<?, ?> column = columnValueTypeMap.get(type);
        if (column == null) {
            throw new DataFrameRuntimeException(String.format("no column type found for value type '%s'", type.getCanonicalName()));
        }
        return (DataFrameColumn<T, ?>) column.copyEmpty();
    }

    public <T> DataFrameColumn<T, ?> createColumn(ValueType<T> valueType) {
        return createColumnForType(valueType.getType());
    }

    public <T extends DataFrameColumn<?, T>> DataFrameColumn<?, ?> createColumn(Class<T> type) {
        DataFrameColumn<?, ?> column = columnTypesMap.get(type);
        if (column == null) {
            throw new DataFrameRuntimeException(String.format("no column type found for '%s'", type.getCanonicalName()));
        }
        return column.copyEmpty();
    }

    /**
     * Parses a String into an array. The corresponding value type for the array type is used.
     *
     * @param cl  array class (String[].class)
     * @param x   String to parse
     * @param <T> type of array class
     * @return parsed array object
     * @throws ParseException thrown if the string can not be parsed
     */
    public <T> T parseArray(Class<T> cl, String x) throws ParseException, ValueTypeNotFoundException {
        ValueType<?> p = getValueType(cl.getComponentType());
        Class<?> cc = cl.getComponentType();
        String[] vals = x.split("[;,|]");
        Object r = Array.newInstance(cc, vals.length);
        for (int i = 0; i < vals.length; i++) {
            Array.set(r, i, p.parse(vals[i]));
        }
        return cl.cast(r);
    }


    /**
     * Parses a String into an Object of a defined type
     *
     * @param cl  class of the returned object
     * @param x   String to parse
     * @param <T> type of the returned object
     * @return parsed object
     * @throws ParseException             thrown if the string can not be parsed
     * @throws ValueTypeNotFoundException thrown if no type is found for the object type
     */
    public <T> T parse(Class<T> cl, String x) throws ParseException, ValueTypeNotFoundException {
        if ((cl.isArray() && !typeExists(cl.getComponentType())) && !typeExists(cl)) {
            throw new ValueTypeNotFoundException(cl);
        }
        try {
            if (cl.isArray()) {
                return parseArray(cl, x);
            }
            ValueType<T> p = getValueType(cl);
            return p.parse(x);
        } catch (Exception e) {
            throw new ParseException("error parsing '" + x + "' to " + cl.getName(), 0);
        }
    }


    /**
     * Checks if a parser for the class is available
     *
     * @param cl input class
     * @return true if parser is available
     */
    public boolean typeExists(Class<?> cl) {
        return columnValueTypeMap.get(cl) != null;
    }

    /**
     * Returns a parser for the input class
     *
     * @param cl  input class
     * @param <T> type of class
     * @return type for input class
     * @throws ValueTypeNotFoundException thrown if no parser is found
     */
    @SuppressWarnings("unchecked")
    public <T> ValueType<T> getValueType(Class<T> cl) throws ValueTypeNotFoundException {
        DataFrameColumn<?, ?> col = columnValueTypeMap.get(cl);
        if (col == null) {
            throw new ValueTypeNotFoundException(cl);
        }
        return (ValueType<T>) col.getValueType();
    }

    /**
     * Returns a value type for the input class or null if no type is found
     *
     * @param cl  input class
     * @param <T> type of class
     * @return type for input class
     */
    public <T> ValueType<T> findValueTypeOrNull(Class<T> cl) {
        try {
            return getValueType(cl);
        } catch (ValueTypeNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Returns a value type for the input class throws a runtime exception if no type is found
     *
     * @param cl  input class
     * @param <T> type of class
     * @return type for input class
     */
    public <T> ValueType<T> findValueTypeOrThrow(Class<T> cl) {
        try {
            return getValueType(cl);
        } catch (ValueTypeNotFoundException e) {
            throw new DataFrameRuntimeException("value type not found", e);
        }
    }

    /**
     * Parses a String into an object of input type.
     * returns null if no type is found or the String can't be parsed.
     *
     * @param cl  Class of resulting object
     * @param x   String to parse
     * @param <T> type of resulting object
     * @return parsed Object
     */
    public <T> T parseOrNull(Class<T> cl, String x) {
        if ((cl.isArray() && !typeExists(cl.getComponentType())) && !typeExists(cl)) {
            return null;
        }
        try {
            if (cl.isArray()) {
                return parseArray(cl, x);
            }
            ValueType<T> p = getValueType(cl);
            return p.parse(x);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates a column converter instance including the default column types
     *
     * @return column converter
     */
    public static DataFrameTypeManager createNew() {
        return new DataFrameTypeManager();
    }

    public static DataFrameTypeManager get() {
        return defaultInstance;
    }


}
