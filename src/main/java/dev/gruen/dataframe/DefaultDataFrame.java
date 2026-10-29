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

package dev.gruen.dataframe;

import dev.gruen.dataframe.column.*;
import dev.gruen.dataframe.common.mapping.DataMapper;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.dataframe.filter.compile.PredicateCompiler;
import dev.gruen.dataframe.group.DataGrouping;
import dev.gruen.dataframe.group.GroupUtil;
import dev.gruen.dataframe.group.impl.TreeGroupUtil;
import dev.gruen.dataframe.index.Index;
import dev.gruen.dataframe.index.Indices;
import dev.gruen.dataframe.join.JoinColumn;
import dev.gruen.dataframe.join.JoinUtil;
import dev.gruen.dataframe.join.JoinedDataFrame;
import dev.gruen.dataframe.join.impl.DefaultJoinUtil;
import dev.gruen.dataframe.sort.RowColumnComparator;
import dev.gruen.dataframe.sort.SortColumn;
import dev.gruen.dataframe.transform.DataFrameTransform;
import dev.gruen.dataframe.type.DataFrameTypeManager;
import dev.gruen.dataframe.type.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Alex on 09.03.2016.
 */
public class DefaultDataFrame implements DataFrame {
    private static final Logger log = LoggerFactory.getLogger(DefaultDataFrame.class);
    public final static int DEFAULT_HEAD_SIZE = 20;
    public final static int DEFAULT_TAIL_SIZE = 20;
    private int size;
    private final Map<String, DataFrameColumn<?, ?>> columnsMap = new LinkedHashMap<>();
    private DataFrameColumn<?, ?>[] columns = null;
    private DataFrameHeader header = new DataFrameHeader();
    private final Indices indices = new Indices(this);
    private JoinUtil joinUtil = new DefaultJoinUtil();
    private GroupUtil groupUtil = new TreeGroupUtil();
    private final AtomicInteger version = new AtomicInteger(0);
    private String name;

    public DefaultDataFrame() {

    }

    public DefaultDataFrame(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getVersion() {
        return version.get();
    }

    @Override
    public DefaultDataFrame setPrimaryKey(String... colNames) {
        DataFrameColumn<?, ?>[] columns = new DataFrameColumn[colNames.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = getColumn(colNames[i]);
        }
        return setPrimaryKey(columns);
    }


    @Override
    public DefaultDataFrame setPrimaryKey(DataFrameColumn<?, ?>... cols) {
        this.indices.setPrimaryKey(cols);
        return this;
    }


    @Override
    public DefaultDataFrame removePrimaryKey() {
        indices.removeIndex(Indices.PRIMARY_KEY_NAME);
        return this;
    }


    @Override
    public DefaultDataFrame removeIndex(String name) {
        indices.removeIndex(name);
        return this;
    }


    @Override
    public DefaultDataFrame renameColumn(String name, String newName) {
        DataFrameColumn<?, ?> column = columnsMap.get(name);
        if (column == null) {
            return this;
        }
        header.rename(name, newName);
        column.setName(newName);
        columnsMap.remove(name);
        columnsMap.put(newName, column);
        return this;
    }


    public DefaultDataFrame replaceColumn(String existing, DataFrameColumn<?, ?> replacement) {
        DataFrameColumn<?, ?> existingColumn = getColumn(existing);
        return replaceColumn(existingColumn, replacement);
    }


    public DefaultDataFrame replaceColumn(DataFrameColumn<?, ?> existing, DataFrameColumn<?, ?> replacement) {
        int existingIndex = header.getIndex(existing.getName());
        columns[existingIndex] = replacement;
        header.replace(existing, replacement);
        columnsMap.remove(existing.getName());
        columnsMap.put(replacement.getName(), replacement);
        indices.replace(existing, replacement);
        version.incrementAndGet();
        return this;
    }

    @Override
    public ColumnSelection selectColumns(String... columnNames) {
        DataFrameColumn<?, ?>[] columns = new DataFrameColumn[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = getColumn(columnNames[i]);
        }
        return selectColumns(columns);
    }

    @Override
    public ColumnSelection selectColumns(DataFrameColumn<?, ?>... columns) {
        return new ColumnSelection(this, columns);
    }


    @Override
    public DefaultDataFrame addColumn(DataFrameColumn<?, ?> column) {
        if (!DataFrameTypeManager.get().isRegistered(column)) {
            throw new DataFrameRuntimeException(
                    String.format("column type '%s' is not registered with DataFrameTypeManager", column.getClass()));
        }
        if (column.size() == 0 && size != 0) {
            for (int i = 0; i < size; i++) {
                column.appendNA();
            }
        }
        if (columns != null && column.size() != size) {
            throw new DataFrameRuntimeException("column lengths must be equal");
        }

        if (column.getDataFrame() != null && column.getDataFrame() != this) {
            throw new DataFrameRuntimeException("column can not be added to multiple data frames. use column.copy() first");
        }
        addToColumns(column);
        if (columns.length == 1) {
            this.size = column.size();
        }
        try {
            column.setDataFrame(this);
        } catch (DataFrameException e) {
            throw new DataFrameRuntimeException("error adding column", e);
        }
        header.add(column.getName(), column.getClass(), column.getValueType());
        columnsMap.put(column.getName(), column);
        return this;
    }

    private void addToColumns(DataFrameColumn<?, ?> column) {
        DataFrameColumn<?, ?>[] newColumns = new DataFrameColumn[columns == null ? 1 : columns.length + 1];
        if (columns != null) {
            System.arraycopy(columns, 0, newColumns, 0, columns.length);
        }
        newColumns[newColumns.length - 1] = column;
        columns = newColumns;
    }


    public DefaultDataFrame addBooleanColumn(String name) {
        BooleanColumn column = new BooleanColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addByteColumn(String name) {
        ByteColumn column = new ByteColumn(name);
        return addColumn(column);
    }

    public DefaultDataFrame addLocalDateColumn(String name) {
        return addColumn(new LocalDateColumn(name));
    }

    public DefaultDataFrame addLocalTimeColumn(String name) {
        return addColumn(new LocalTimeColumn(name));
    }

    public DefaultDataFrame addLocalDateTimeColumn(String name) {
        return addColumn(new LocalDateTimeColumn(name));
    }

    public DefaultDataFrame addInstantColumn(String name) {
        return addColumn(new InstantColumn(name));
    }

    public DefaultDataFrame addOffsetDateTimeColumn(String name) {
        return addColumn(new OffsetDateTimeColumn(name));
    }

    public DefaultDataFrame addZonedDateTimeColumn(String name) {
        return addColumn(new ZonedDateTimeColumn(name));
    }


    public DefaultDataFrame addDoubleColumn(String name) {
        DoubleColumn column = new DoubleColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addFloatColumn(String name) {
        FloatColumn column = new FloatColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addIntegerColumn(String name) {
        IntegerColumn column = new IntegerColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addLongColumn(String name) {
        LongColumn column = new LongColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addShortColumn(String name) {
        ShortColumn column = new ShortColumn(name);
        return addColumn(column);
    }


    public DefaultDataFrame addStringColumn(String name) {
        StringColumn column = new StringColumn(name);
        return addColumn(column);
    }


    @Override
    public <T> DataFrame addColumn(Class<T> type, String name) {
        return addColumn(type, name, DataFrameTypeManager.get());
    }


    @Override
    public <T> DataFrame addColumn(Class<T> type, String name, DataFrameTypeManager dataFrameTypeManager) {
        return addColumn(type, name, dataFrameTypeManager, null);
    }


    @Override
    public <T, C extends DataFrameColumn<T, C>> DataFrame addColumn(Class<T> type, String name,
                                                                    DataFrameTypeManager dataFrameTypeManager,
                                                                    ColumnAppender<T> appender) {
        Class<C> columnType = dataFrameTypeManager.getColumnType(type);
        if (columnType == null) {
            throw new DataFrameRuntimeException(String.format("no  column type found for %s", type.getName()));
        }

        return addColumn(columnType, name, appender);
    }

    /**
     * {@inheritDoc}
     * If no column appender is specified, the column is filled with {@link Values#NA NA} values.
     * If the column can not be created or added a {@link DataFrameRuntimeException} is thrown.
     */
    @Override
    public <T, C extends DataFrameColumn<T, C>> DataFrame addColumn(Class<C> type, String name,
                                                                    ColumnAppender<T> appender) {
        C col = type.cast(DataFrameTypeManager.get().createColumn(type));
        col.setName(name);
        if (appender != null) {
            for (DataRow row : this) {
                T val = appender.createRowValue(row);
                if (val == null || val == Values.NA) {
                    col.doAppendNA();
                } else {
                    col.doAppend(val);
                }
            }
        } else {
            for (int i = 0; i < size(); i++) {
                col.doAppendNA();
            }
        }
        addColumn(col);
        return this;
    }


    @Override
    public DefaultDataFrame addColumns(Collection<DataFrameColumn<?, ?>> columns) {
        for (DataFrameColumn<?, ?> column : columns) {
            addColumn(column);
        }
        return this;
    }


    @Override
    public DefaultDataFrame addColumns(DataFrameColumn<?, ?>... columns) {
        for (DataFrameColumn<?, ?> column : columns) {
            addColumn(column);
        }
        return this;
    }

    @Override
    public DefaultDataFrame append(DataFrame dataFrame, int rowIndex) {
        if (columns == null) {
            throw new DataFrameRuntimeException("dataframe contains no columns");
        }
        if (dataFrame.getHeader().size() != columns.length) {
            throw new DataFrameRuntimeException("value for each column required");
        }
        DataFrameColumn<?, ?> column;
        Object value;
        for (int i = 0; i < columns.length; i++) {
            column = columns[i];
            column.startDataFrameAppend();
            value = dataFrame.getValue(i, rowIndex);
            if (value == null || Values.NA.equals(value)) {
                column.appendNA();
            } else {
                column.appendRaw(value);
            }
            column.endDataFrameAppend();
        }
        size++;
        indices.update(getRow(size - 1));
        return this;
    }

    /**
     * {@inheritDoc} If the wrong number of values or a wrong type is found a {@link DataFrameRuntimeException} is thrown.
     */
    @Override
    public DefaultDataFrame append(Object... values) {
        if (columns == null) {
            throw new DataFrameRuntimeException("dataframe contains no columns");
        }
        if (values.length != columns.length) {
            throw new DataFrameRuntimeException("value for each column required");
        }
        DataFrameColumn<?, ?> column;
        Object value;
        for (int i = 0; i < columns.length; i++) {
            column = columns[i];
            value = values[i];
            if (!column.isValueValid(value)) {
                throw new DataFrameRuntimeException(
                        String.format("value %d has wrong type (%s != %s)", i,
                                value == null ? "null" : value.getClass().getName(),
                                column.getValueType().getType().getName()));
            }
        }
        for (int i = 0; i < columns.length; i++) {
            column = columns[i];
            column.startDataFrameAppend();
            value = values[i];
            if (value == null || value == Values.NA) {
                column.appendNA();
            } else {
                column.appendRaw(value);
            }
            column.endDataFrameAppend();
        }
        size++;
        indices.update(getRow(size - 1));
        return this;
    }

    /**
     * @param row row containing the new values
     * @return <code>self</code> for method chaining
     * {@inheritDoc} {@link Values#NA NA} is added for all columns with no value in the provided row.
     */
    @Override
    public DefaultDataFrame append(DataRow row) {
        Object value;
        for (String h : header) {
            DataFrameColumn<?, ?> column = columnsMap.get(h);
            column.startDataFrameAppend();
            value = row.get(h);
            if (value == null || value == Values.NA) {
                column.appendNA();
            } else {
                column.appendRaw(value);
            }
            column.endDataFrameAppend();

        }
        this.size++;
        indices.update(getRow(size - 1));
        return this;
    }

    @Override
    public DefaultDataFrame appendMatchingRow(DataRow row) {
        Object value;
        for (int i = 0; i < row.size(); i++) {
            DataFrameColumn<?, ?> column = columns[i];
            column.startDataFrameAppend();
            value = row.get(i);
            if (value == null || value == Values.NA) {
                column.appendNA();
            } else {
                column.appendRaw(value);
            }
            column.endDataFrameAppend();
        }
        this.size++;
        indices.update(getRow(size - 1));
        return this;
    }

    @Override
    public DefaultDataFrame update(DataRow dataRow) {
        for (String h : header) {
            DataFrameColumn<?, ?> column = getColumn(h);
            Object newValue = dataRow.get(h);
            if (newValue == null) {
                continue;
            }
            if (newValue == Values.NA) {
                column.setNA(dataRow.getIndex());
            } else {
                column.setRaw(dataRow.getIndex(), newValue);
            }
        }
        return this;
    }


    @Override
    public DefaultDataFrame set(DataFrameHeader header) {
        this.version.incrementAndGet();
        this.columns = null;
        this.header.clear();
        this.size = 0;
        this.indices.clearValues();
        this.columnsMap.clear();
        for (String columnName : header) {
            ValueType<?> type = header.getValueType(columnName);
            try {
                DataFrameColumn<?, ?> column = DataFrameTypeManager.get().createColumn(type);
                column.setName(columnName);
                // the value type shares the settings of the column it was taken from
                type.getSettings().applyTo(column.getSettings());
                addColumn(column);
            } catch (Exception e) {
                throw new DataFrameRuntimeException("error creating column instance", e);
            }
        }
        return this;
    }

    @Override
    public DefaultDataFrame set(DataRows dataRows) {
        return set(dataRows, null);
    }


    protected DefaultDataFrame set(DataRows dataRows, Indices indices) {
        DataFrame temp = dataRows.toDataFrame();
        set(temp, indices);
        return this;
    }

    protected DefaultDataFrame set(DataFrame dataFrame, Indices indices) {
        this.version.incrementAndGet();
        this.columnsMap.clear();
        this.columns = null;

        this.size = 0;
        this.header = new DataFrameHeader();
        for (DataFrameColumn<?, ?> column : dataFrame.getColumns()) {
            try {
                column.setDataFrame(null);
                addColumn(column);
            } catch (DataFrameException e) {
                log.error("error adding column", e);
            }
        }
        if (indices == this.indices) {
            this.indices.clearValues();
        } else if (indices != null) {
            indices.copyTo(this);
        } else {
            this.indices.clear();
        }

        this.indices.updateAllRows();
        return this;
    }


    @Override
    public DefaultDataFrame removeColumn(String header) {
        DataFrameColumn<?, ?> column = getColumn(header);
        if (column == null) {
            log.error("error column not found {}", header);
            return this;
        }
        return removeColumn(column);
    }


    @Override
    public DefaultDataFrame removeColumn(DataFrameColumn<?, ?> column) {
        try {
            column.setDataFrame(null);
        } catch (DataFrameException e) {
            throw new DataFrameRuntimeException("error removing column", e);

        }
        this.version.incrementAndGet();
        removeFromColumns(column);
        this.header.remove(column.getName());
        this.indices.removeColumn(column);
        this.columnsMap.remove(column.getName());
        return this;
    }

    private void removeFromColumns(DataFrameColumn<?, ?> column) {
        if (columns == null) {
            throw new DataFrameRuntimeException("error removing column: dataframe contains no column");
        }
        if (columns.length == 1 && columns[0] == column) {
            columns = null;
            return;
        }
        DataFrameColumn<?, ?>[] newColumns = new DataFrameColumn[columns.length - 1];
        int newIndex = 0;
        boolean columnFound = false;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == column) {
                columnFound = true;
                continue;
            }
            newColumns[newIndex++] = columns[i];
        }
        if (!columnFound) {
            throw new DataFrameRuntimeException(
                    String.format("error removing column: column not found '%s'", column.getName()));
        }
        columns = newColumns;
    }


    @Override
    public DefaultDataFrame sort(SortColumn... columns) {
        DataRows rows = getRows(0, size);
        rows.sort(new RowColumnComparator(columns));
        set(rows, indices);
        return this;
    }


    @Override
    public DefaultDataFrame sort(Comparator<DataRow> comp) {
        DataRows rows = getRows(0, size);
        rows.sort(comp);
        set(rows, indices);
        return this;
    }


    @Override
    public DefaultDataFrame sort(String name) {
        return sort(name, SortColumn.Direction.Ascending);
    }


    @Override
    public DefaultDataFrame sort(String name, SortColumn.Direction dir) {
        DataRows rows = getRows(0, size);
        rows.sort(new RowColumnComparator(new SortColumn[]{new SortColumn(name, dir)}));
        set(rows, indices);
        return this;
    }


    @Override
    public DefaultDataFrame shuffle() {
        DataRows rows = getRows(0, size);
        Collections.shuffle(rows);
        set(rows, indices);
        return this;
    }


    @Override
    public DefaultDataFrame select(String colName, Object value) {
        return select(FilterPredicate.eq(colName, value));
    }


    @Override
    public DataRow selectFirst(String colName, Object value) {
        return selectFirst(FilterPredicate.eq(colName, value));
    }


    @Override
    public DataRow selectFirst(String predicateString) {
        return selectFirst(FilterPredicate.compile(predicateString));
    }


    @Override
    public DataRow selectFirst(FilterPredicate predicate) {
        for (DataRow row : this) {
            if (predicate.valid(row)) {
                return row;
            }
        }
        return null;
    }


    @Override
    public DefaultDataFrame select(FilterPredicate predicate) {
        DefaultDataFrame df = new DefaultDataFrame();
        df.set(getHeader());
        indices.copyTo(df);
        for (DataRow row : this) {
            if (predicate.valid(row)) {
                df.append(row);
            }
        }
        return df;
    }


    @Override
    public DefaultDataFrame select(String predicateString) {
        return select(PredicateCompiler.compile(predicateString));
    }


    @Override
    public DefaultDataFrame filter(String predicateString) {
        filter(FilterPredicate.compile(predicateString));
        return this;
    }


    @Override
    public DefaultDataFrame filter(FilterPredicate predicate) {
        set(select(predicate), getIndices());
        return this;
    }


    @Override
    public DataRows selectRows(String colName, Object value) {
        return selectRows(FilterPredicate.eq(colName, value));
    }

    @Override
    public DataRows selectRows(String predicateString) {
        return selectRows(FilterPredicate.compile(predicateString));
    }


    @Override
    public DataRows selectRows(FilterPredicate predicate) {
        List<DataRow> rows = new ArrayList<>();
        for (DataRow row : this) {
            if (predicate.valid(row)) {
                rows.add(row);
            }
        }
        return new DataRows(this, rows);
    }


    @Override
    public DefaultDataFrame transform(DataFrameTransform transformer) {
        return transformer.transform(this);
    }

    @Override
    public DataRow selectByPrimaryKey(Object... keyValues) {
        Integer index = this.indices.findByPrimaryKey(keyValues);
        if (index == null || index < 0) {
            return null;
        }
        return getRow(index);
    }


    @Override
    public DefaultDataFrame reverse() {
        this.version.incrementAndGet();
        for (DataFrameColumn<?, ?> col : columns) {
            col.doReverse();
        }
        this.indices.updateAllRows();
        return this;
    }


    @Override
    public DefaultDataFrame addIndex(String indexName, String... columnNames) {
        DataFrameColumn<?, ?>[] columns = new DataFrameColumn[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = getColumn(columnNames[i]);
        }

        return addIndex(indexName, columns);
    }


    @Override
    public DefaultDataFrame addIndex(String indexName, DataFrameColumn<?, ?>... columns) {
        indices.addIndex(indexName, columns);
        return this;
    }

    @Override
    public DefaultDataFrame addIndex(Index index) {
        indices.addIndex(index);
        return this;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @Deprecated
    public DefaultDataFrame subset(int from, int to) {
        return filterSubset(from, to);
    }

    @Override
    public DefaultDataFrame filterSubset(int from, int to) {
        set(selectSubset(from, to), indices);
        return this;
    }

    @Override
    public DefaultDataFrame selectSubset(int from, int to) {
        DefaultDataFrame newFrame = new DefaultDataFrame();
        newFrame.set(getRows(from, to), indices);
        return newFrame;
    }


    @Override
    public DataRows getRows(int from, int to) {
        DataRows rows = new DataRows(this);
        for (int i = from; i < to; i++) {
            rows.add(getRow(i));
        }
        return rows;
    }


    @Override
    public DataRows getRows() {
        return getRows(0, size);
    }


    @Override
    public DataFrameHeader getHeader() {
        return header;
    }


    @Override
    public DefaultDataFrame concat(DataFrame other) {
        if (!header.equals(other.getHeader())) {
            throw new DataFrameRuntimeException("data frames not compatible");
        }
        for (DataRow row : other) {
            append(row);
        }
        return this;
    }


    @Override
    public DefaultDataFrame concat(Collection<DataFrame> dataFrames) {
        for (DataFrame dataFrame : dataFrames) {
            if (!header.equals(dataFrame.getHeader())) {
                throw new DataFrameRuntimeException("data frames not compatible");
            }
            for (DataRow row : dataFrame) {
                append(row);
            }
        }
        return this;
    }


    @Override
    public DefaultDataFrame concat(DataFrame... dataFrames) {
        return concat(Arrays.asList(dataFrames));
    }


    @Override
    public boolean isCompatible(DataFrame input) {
        return header.equals(input.getHeader());
    }


    @Override
    public DataRow getRow(int i) {
        return new DataRow(this, i);
    }


    @Override
    public Collection<String> getColumnNames() {
        return new ArrayList<>(columnsMap.keySet());
    }


    @Override
    public DataFrameColumn<?, ?> getColumn(String name) {
        return columnsMap.get(name);
    }


    @Override
    public <T extends DataFrameColumn<?, T>> T getColumn(String name, Class<T> cl) {
        DataFrameColumn<?, ?> column = columnsMap.get(name);
        if (column == null) {
            throw new DataFrameRuntimeException(String.format("column '%s' not found", name));
        }
        if (!cl.isInstance(column)) {
            throw new DataFrameRuntimeException(String.format("column '%s' has wrong type", name));
        }
        return cl.cast(column);
    }


    // unchecked: the generic type arguments of NumberColumn can not be expressed by a class literal.
    // The column type is verified by getColumn(String, Class).
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Number, C extends NumberColumn<T, C>> NumberColumn<T, C> getNumberColumn(String name) {
        return getColumn(name, NumberColumn.class);
    }


    @Override
    public StringColumn getStringColumn(String name) {
        return getColumn(name, StringColumn.class);
    }


    @Override
    public DoubleColumn getDoubleColumn(String name) {
        return getColumn(name, DoubleColumn.class);
    }


    @Override
    public IntegerColumn getIntegerColumn(String name) {
        return getColumn(name, IntegerColumn.class);
    }


    @Override
    public FloatColumn getFloatColumn(String name) {
        return getColumn(name, FloatColumn.class);
    }


    @Override
    public BooleanColumn getBooleanColumn(String name) {
        return getColumn(name, BooleanColumn.class);
    }


    @Override
    public ByteColumn getByteColumn(String name) {
        return getColumn(name, ByteColumn.class);
    }

    @Override
    public LocalDateColumn getLocalDateColumn(String name) {
        return getColumn(name, LocalDateColumn.class);
    }

    @Override
    public LocalTimeColumn getLocalTimeColumn(String name) {
        return getColumn(name, LocalTimeColumn.class);
    }

    @Override
    public LocalDateTimeColumn getLocalDateTimeColumn(String name) {
        return getColumn(name, LocalDateTimeColumn.class);
    }

    @Override
    public InstantColumn getInstantColumn(String name) {
        return getColumn(name, InstantColumn.class);
    }

    @Override
    public OffsetDateTimeColumn getOffsetDateTimeColumn(String name) {
        return getColumn(name, OffsetDateTimeColumn.class);
    }

    @Override
    public ZonedDateTimeColumn getZonedDateTimeColumn(String name) {
        return getColumn(name, ZonedDateTimeColumn.class);
    }


    @Override
    public LongColumn getLongColumn(String name) {
        return getColumn(name, LongColumn.class);
    }


    @Override
    public ShortColumn getShortColumn(String name) {
        return getColumn(name, ShortColumn.class);
    }


    @Override
    public DataGrouping groupBy(String... column) {
        return groupUtil.groupBy(this, column);
    }


    @Override
    public JoinedDataFrame joinLeft(DataFrame dataFrame, String... joinColumns) {
        JoinColumn[] joinColumnsArray = new JoinColumn[joinColumns.length];
        for (int i = 0; i < joinColumns.length; i++) {
            joinColumnsArray[i] = new JoinColumn(joinColumns[i]);
        }
        return joinLeft(dataFrame, joinColumnsArray);
    }


    @Override
    public JoinedDataFrame joinLeft(DataFrame dataFrame, JoinColumn... joinColumns) {
        return joinUtil.leftJoin(this, dataFrame, joinColumns);
    }


    @Override
    public JoinedDataFrame joinLeft(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns) {
        return joinUtil.leftJoin(this, dataFrame, suffixA, suffixB, joinColumns);
    }


    @Override
    public JoinedDataFrame joinRight(DataFrame dataFrame, String... joinColumns) {
        JoinColumn[] joinColumnsArray = new JoinColumn[joinColumns.length];
        for (int i = 0; i < joinColumns.length; i++) {
            joinColumnsArray[i] = new JoinColumn(joinColumns[i]);
        }
        return joinRight(dataFrame, joinColumnsArray);
    }


    @Override
    public JoinedDataFrame joinRight(DataFrame dataFrame, JoinColumn... joinColumns) {
        return joinUtil.rightJoin(this, dataFrame, joinColumns);
    }


    @Override
    public JoinedDataFrame joinRight(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns) {
        return joinUtil.rightJoin(this, dataFrame, suffixA, suffixB, joinColumns);
    }


    @Override
    public JoinedDataFrame joinInner(DataFrame dataFrame, String... joinColumns) {
        JoinColumn[] joinColumnsArray = new JoinColumn[joinColumns.length];
        for (int i = 0; i < joinColumns.length; i++) {
            joinColumnsArray[i] = new JoinColumn(joinColumns[i]);
        }
        return joinInner(dataFrame, joinColumnsArray);
    }


    @Override
    public JoinedDataFrame joinInner(DataFrame dataFrame, JoinColumn... joinColumns) {
        return joinUtil.innerJoin(this, dataFrame, joinColumns);
    }


    @Override
    public JoinedDataFrame joinInner(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns) {
        return joinUtil.innerJoin(this, dataFrame, suffixA, suffixB, joinColumns);
    }

    @Override
    public JoinedDataFrame joinOuter(DataFrame dataFrame, String... joinColumns) {
        JoinColumn[] joinColumnsArray = new JoinColumn[joinColumns.length];
        for (int i = 0; i < joinColumns.length; i++) {
            joinColumnsArray[i] = new JoinColumn(joinColumns[i]);
        }
        return joinOuter(dataFrame, joinColumnsArray);
    }


    @Override
    public JoinedDataFrame joinOuter(DataFrame dataFrame, JoinColumn... joinColumns) {
        return joinUtil.outerJoin(this, dataFrame, joinColumns);
    }


    @Override
    public JoinedDataFrame joinOuter(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns) {
        return joinUtil.outerJoin(this, dataFrame, suffixA, suffixB, joinColumns);
    }

    @Override
    public DefaultDataFrame copy() {
        DataRows rows = getRows(0, size);
        DefaultDataFrame copy = new DefaultDataFrame();
        copy.set(rows, indices);
        return copy;
    }


    @Override
    public boolean containsColumn(DataFrameColumn<?, ?> column) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == column) {
                return true;
            }
        }
        return false;
    }


    protected void notifyColumnValueChanged(DataFrameColumn<?, ?> column, int index, Object value) {
        if (indices.isIndexColumn(column)) {
            indices.updateValue(column, getRow(index));
        }
    }


    protected void notifyColumnChanged(DataFrameColumn<?, ?> column) {
        if (indices.isIndexColumn(column)) {
            indices.updateColumn(column);
        }
    }


    @Override
    public boolean isIndexColumn(DataFrameColumn<?, ?> column) {
        return indices.isIndexColumn(column);
    }


    @Override
    public DataRows selectRowsByIndex(String name, Object... values) {
        Collection<Integer> rowIndices = indices.find(name, values);
        return selectRows(rowIndices);
    }

    @Override
    public DataRows selectRows(Collection<Integer> rowIndices) {
        if (!rowIndices.isEmpty()) {
            List<DataRow> rows = new ArrayList<>();
            for (Integer i : rowIndices) {
                rows.add(getRow(i));
            }
            return new DataRows(this, rows);
        }
        return new DataRows(this, new ArrayList<>(0));
    }

    @Override
    public DataRow selectFirstRowByIndex(String name, Object... values) {
        Integer idx = indices.findFirst(name, values);
        return idx == null ? null : getRow(idx);
    }

    @Override
    public DataFrame selectByIndex(String name, Object... values) {
        DataRows rows = selectRowsByIndex(name, values);
        DefaultDataFrame df = new DefaultDataFrame();
        df.set(rows, indices);
        return df;
    }


    @Override
    public Collection<DataFrameColumn<?, ?>> getColumns() {
        return Arrays.asList(columns);
    }

    @Override
    public Iterable<? extends DataRow> rows() {
        return this;
    }


    protected Indices getIndices() {
        return indices;
    }

    public GroupUtil getGroupUtil() {
        return groupUtil;
    }

    public JoinUtil getJoinUtil() {
        return joinUtil;
    }

    public void setGroupUtil(GroupUtil groupUtil) {
        this.groupUtil = groupUtil;
    }

    public void setJoinUtil(JoinUtil joinUtil) {
        this.joinUtil = joinUtil;
    }

    @Override
    public <T> List<T> map(Class<T> cl) {
        return DataMapper.map(this, cl);
    }


    @Override
    public Iterator<DataRow> iterator() {
        return new Iterator<DataRow>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public DataRow next() {
                if (index == size()) {
                    throw new NoSuchElementException("index out of bounds");
                }
                return getRow(index++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove is not supported for data frames");
            }
        };
    }

    @Override
    public Object getValue(int col, int row) {
        if (columns == null) {
            throw new DataFrameRuntimeException("dataframe contains no columns");
        }
        if (col >= columns.length || row > size) {
            throw new DataFrameRuntimeException("index out of bounds");
        }
        return columns[col].get(row);
    }

    @Override
    public void setValue(int col, int row, Object newValue) {
        if (columns == null) {
            throw new DataFrameRuntimeException("dataframe contains no columns");
        }
        if (col >= columns.length || row > size) {
            throw new DataFrameRuntimeException("index out of bounds");
        }
        if (newValue == null || newValue == Values.NA) {
            columns[col].setNA(row);
        } else {
            columns[col].setRaw(row, newValue);
        }
        indices.update(getRow(row));
    }

    @Override
    public boolean isNA(int col, int row) {
        if (columns == null) {
            throw new DataFrameRuntimeException("dataframe contains no columns");
        }
        if (col >= columns.length || row > size) {
            throw new DataFrameRuntimeException("index out of bounds");
        }
        return columns[col].isNA(row);
    }

    @Override
    public DataFrame head(int size) {
        return selectSubset(0, Math.min(size(), size));
    }


    @Override
    public DataFrame head() {
        return head(DEFAULT_HEAD_SIZE);
    }

    @Override
    public DataFrame tail(int size) {
        return selectSubset(Math.max(0, size() - size), size());
    }


    @Override
    public DataFrame tail() {
        return tail(DEFAULT_TAIL_SIZE);
    }

    @Override
    public void clear() {
        for (DataFrameColumn<?, ?> col : columns) {
            col.clear();
        }
        size = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultDataFrame)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        DataFrame d = (DataFrame) o;
        if (size() != d.size()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            if (!getRow(i).equals(d.getRow(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // consistent with equals: equal data frames have the same size
        return Integer.hashCode(size());
    }
}
