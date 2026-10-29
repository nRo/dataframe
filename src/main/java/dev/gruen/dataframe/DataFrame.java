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
import dev.gruen.dataframe.common.DataContainer;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.dataframe.group.DataGrouping;
import dev.gruen.dataframe.group.GroupUtil;
import dev.gruen.dataframe.index.Index;
import dev.gruen.dataframe.io.*;
import dev.gruen.dataframe.join.JoinColumn;
import dev.gruen.dataframe.join.JoinUtil;
import dev.gruen.dataframe.join.JoinedDataFrame;
import dev.gruen.dataframe.sort.SortColumn;
import dev.gruen.dataframe.transform.DataFrameTransform;
import dev.gruen.dataframe.type.DataFrameTypeManager;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;

/**
 * Created by algru on 12.06.2017.
 */
public interface DataFrame extends DataContainer<DataFrameHeader, DataRow> {
    /**
     * Returns the name of this dataframe
     *
     * @return name
     */
    String getName();

    /**
     * Sets the name of this dataframe
     *
     * @param name dataframe name
     */
    void setName(String name);

    /**
     * Returns the version of this dataframe.
     * The version is automatically increased on each function that alters the dataframe (sort,...)
     *
     * @return version
     */
    int getVersion();

    /**
     * Sets the primary key columns using column names
     *
     * @param colNames primary key columns
     * @return <code>self</code> for method chaining
     */
    DataFrame setPrimaryKey(String... colNames);

    /**
     * Sets the primary key columns using column objects
     *
     * @param cols primary key columns
     * @return <code>self</code> for method chaining
     */
    DataFrame setPrimaryKey(DataFrameColumn<?, ?>... cols);

    /**
     * Removes the current primary key
     *
     * @return <code>self</code> for method chaining
     */
    DataFrame removePrimaryKey();

    /**
     * Removes the index with the specified name
     *
     * @param name name of index
     * @return <code>self</code> for method chaining
     */
    DataFrame removeIndex(String name);


    /**
     * Renames a column
     *
     * @param name    current column name
     * @param newName new column name
     * @return <code>self</code> for method chaining
     */
    DataFrame renameColumn(String name, String newName);

    /**
     * Returns a {@link ColumnSelection} for the selected columns.
     * The rows can be filtered using {@link ColumnSelection#where}.
     *
     * @param columnNames selected columns
     * @return {@link ColumnSelection} for row selection
     */
    ColumnSelection selectColumns(String... columnNames);

    /**
     * Returns a {@link ColumnSelection} for the selected columns.
     * The rows can be filtered using {@link ColumnSelection#where}.
     *
     * @param columns selected columns
     * @return {@link ColumnSelection} for row selection
     */
    ColumnSelection selectColumns(DataFrameColumn<?, ?>... columns);

    /**
     * Adds a column to the data frame.
     * If the column is already part of another data frame a {@link DataFrameRuntimeException} is thrown.
     *
     * @param column column to add
     * @return <code>self</code> for method chaining
     */
    DataFrame addColumn(DataFrameColumn<?, ?> column);

    /**
     * Creates a column for a specified column value type.
     *
     * @param type class of column values
     * @param name column name
     * @param <T>  type of column values
     * @return <code>self</code> for method chaining
     */
    <T> DataFrame addColumn(Class<T> type, String name);

    /**
     * Creates a column for a specified column value type using the provided {@link DataFrameTypeManager}.
     *
     * @param type                 class of column values
     * @param name                 column name
     * @param dataFrameTypeManager provided column type map
     * @param <T>                  type of column values
     * @return <code>self</code> for method chaining
     * @see #addColumn(Class, String, ColumnAppender)
     */
    <T> DataFrame addColumn(Class<T> type, String name, DataFrameTypeManager dataFrameTypeManager);


    /**
     * Creates and adds a new column based on a specified column value type and a {@link DataFrameTypeManager}.
     *
     * @param type                 column value value type
     * @param name                 name of new column
     * @param dataFrameTypeManager column type map (value type / column class mapper)
     * @param appender             column appender (value generator)
     * @param <T>                  type of column values
     * @param <C>                  type of created column
     * @return <code>self</code> for method chaining
     * @see #addColumn(Class, String, ColumnAppender)
     */
    <T, C extends DataFrameColumn<T, C>> DataFrame addColumn(Class<T> type, String name, DataFrameTypeManager dataFrameTypeManager, ColumnAppender<T> appender);

    /**
     * Creates and adds a column to this data frame based on a provided column class.
     * The values in the created column are generated by a {@link ColumnAppender}.
     *
     * @param type     class of created column
     * @param name     name of created column
     * @param appender column appender (value generator)
     * @param <T>      type of column values
     * @param <C>      type of created column
     * @return <code>self</code> for method chaining
     * @see #addColumn(DataFrameColumn)
     */
    <T, C extends DataFrameColumn<T, C>> DataFrame addColumn(Class<C> type, String name, ColumnAppender<T> appender);


    /**
     * Adds a new {@link BooleanColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addBooleanColumn(String name);

    /**
     * Adds a new {@link ByteColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addByteColumn(String name);

    /**
     * Adds a new {@link LocalDateColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addLocalDateColumn(String name);

    /**
     * Adds a new {@link LocalTimeColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addLocalTimeColumn(String name);

    /**
     * Adds a new {@link LocalDateTimeColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addLocalDateTimeColumn(String name);

    /**
     * Adds a new {@link InstantColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addInstantColumn(String name);

    /**
     * Adds a new {@link OffsetDateTimeColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addOffsetDateTimeColumn(String name);

    /**
     * Adds a new {@link ZonedDateTimeColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addZonedDateTimeColumn(String name);

    /**
     * Adds a new {@link DoubleColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addDoubleColumn(String name);

    /**
     * Adds a new {@link FloatColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addFloatColumn(String name);

    /**
     * Adds a new {@link IntegerColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addIntegerColumn(String name);

    /**
     * Adds a new {@link LongColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addLongColumn(String name);

    /**
     * Adds a new {@link ShortColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addShortColumn(String name);

    /**
     * Adds a new {@link StringColumn} to the dataframe.
     *
     * @param name name of the column
     * @return <code>self</code> for method chaining
     */
    DataFrame addStringColumn(String name);

    /**
     * Adds a collection of columns to this data frame
     *
     * @param columns columns to add
     * @return <code>self</code> for method chaining
     */
    DataFrame addColumns(Collection<DataFrameColumn<?, ?>> columns);

    /**
     * Adds an array of columns to this data frame
     *
     * @param columns columns to add
     * @return <code>self</code> for method chaining
     */
    DataFrame addColumns(DataFrameColumn<?, ?>... columns);

    DataFrame replaceColumn(DataFrameColumn<?, ?> existing, DataFrameColumn<?, ?> replacement);

    DataFrame replaceColumn(String existing, DataFrameColumn<?, ?> replacement);


    /**
     * Appends a new row based on {@link Object} values from another dataframe.
     * <p>There must be <b>exactly one value for each column</b>.</p>
     * <p><b>The object types have to match the column types</b>.</p>
     * If the wrong number of values or a wrong type is found a {@link DataFrameRuntimeException} is thrown.
     *
     * @param dataFrame other dataframe
     * @param rowIndex  row in other dataframe
     * @return <code>self</code> for method chaining
     */
    DataFrame append(DataFrame dataFrame, int rowIndex);

    /**
     * Appends a new row based on {@link Object} values.
     * <p>There must be <b>exactly one value for each column</b>.</p>
     * <p><b>The object types have to match the column types</b>.</p>
     * If the wrong number of values or a wrong type is found a {@link DataFrameRuntimeException} is thrown.
     * <p>If the data frame contains:<br>
     * <code>StringColumn,DoubleColumn,IntegerColumn</code><br>
     * The only correct call to this method is:<br>
     * <code>append(String, Double, Integer)</code>
     * </p>
     * <p>empty column values must be provided as <code>null</code> or {@link Values#NA NA}</p>
     *
     * @param values values for the appended row
     * @return <code>self</code> for method chaining
     */
    DataFrame append(Object... values);

    /**
     * Appends a new data row. Only row values matching a dataframe column are appended
     *
     * @param row row containing the new values
     * @return <code>self</code> for method chaining
     */
    DataFrame append(DataRow row);

    /**
     * Appends a new data row. The row must contain a value for all dataframe columns.
     * The order of the row values must match the column order in the dataframe.
     *
     * @param row row containing the new values
     * @return <code>self</code> for method chaining
     */
    DataFrame appendMatchingRow(DataRow row);

    /**
     * Persists the updated values of a data row.
     * <code>null</code> values are ignored. Use {@link Values#NA NA} instead-
     *
     * @param dataRow data row with updated values
     * @return <code>self</code> for method chaining
     */
    DataFrame update(DataRow dataRow);

    DataFrame set(DataFrameHeader header);

    /**
     * Clears all rows in this data frame and sets new rows using the provided {@link DataRows}.
     *
     * @param rows new collection of rows
     * @return <code>self</code> for method chaining
     */
    DataFrame set(DataRows rows);


    /**
     * Removes a column from this data frame
     *
     * @param header column header name
     * @return <code>self</code> for method chaining
     */
    DataFrame removeColumn(String header);

    /**
     * Removes a column from this data frame
     *
     * @param column column to remove
     * @return <code>self</code> for method chaining
     */
    DataFrame removeColumn(DataFrameColumn<?, ?> column);

    /**
     * Sorts the rows in this data frame by one or more {@link SortColumn}
     *
     * @param columns sort columns
     * @return <code>self</code> for method chaining
     */
    DataFrame sort(SortColumn... columns);

    /**
     * Sorts the rows in this data frame using a custom {@link Comparator}
     *
     * @param comp comparator used to sort the rows
     * @return <code>self</code> for method chaining
     */
    DataFrame sort(Comparator<DataRow> comp);

    /**
     * Sorts the rows in this data frame using one column and the default sort direction (<code>ascending</code>)
     *
     * @param name sort column
     * @return <code>self</code> for method chaining
     */
    DataFrame sort(String name);

    /**
     * Sorts the rows in this data frame using one column and sort direction.
     *
     * @param name sort column
     * @param dir  sort direction
     * @return <code>self</code> for method chaining
     */
    DataFrame sort(String name, SortColumn.Direction dir);

    /**
     * Shuffles all rows
     *
     * @return <code>self</code> for method chaining
     */
    DataFrame shuffle();

    /**
     * Returns a new data frame with all rows from this data frame where a specified column value equals
     * an input value.
     *
     * @param colName column name
     * @param value   input value
     * @return new data frame including the found rows
     */
    DataFrame select(String colName, Object value);

    /**
     * Returns the first found data row from this data frame where a specified column value equals
     * an input value.
     *
     * @param colName column name
     * @param value   input value
     * @return first found data row
     */
    DataRow selectFirst(String colName, Object value);

    /**
     * Returns the first found data row from this data frame matching an input predicate.
     *
     * @param predicateString input predicate string
     * @return first found data row
     * @see #select(FilterPredicate)
     */
    DataRow selectFirst(String predicateString);

    /**
     * Returns the first found data row from this data frame matching an input predicate.
     *
     * @param predicate input predicate
     * @return first found data row
     * @see #select(FilterPredicate)
     */
    DataRow selectFirst(FilterPredicate predicate);

    /**
     * Returns a new data frame based on filtered rows from this data frame.<br>
     * Rows that are valid according to the input predicate remain in the new data frame.<br>
     * <p><code>if(predicate.valid(row)) -&gt; add(row)</code></p>
     *
     * @param predicate filter predicate
     * @return new data frame including the found row
     * @see #filter(FilterPredicate)
     */
    DataFrame select(FilterPredicate predicate);

    /**
     * Returns a new data frame based on filtered rows from this data frame.<br>
     * Rows that are valid according to the input predicate remain in the new data frame.<br>
     * The predicate is compiled from the input string.<br>
     * <p><code>if(predicate.valid(row)) -&gt; add(row)</code></p>
     *
     * @param predicateString predicate string
     * @return new data frame including the found row
     * @see #select(FilterPredicate)
     */
    DataFrame select(String predicateString);

    /**
     * Filters data rows that are not valid according to an input predicate.<br>
     * Data rows are filtered by their column values. <br>
     * If a data row is <b>filtered</b> if it is <b>not valid</b> according to the predicate.<br>
     * The filtered data rows are removed from this data frame.<br>
     * <p><code>if(!predicate.valid(row)) -&gt; remove(row)</code></p>
     *
     * @param predicateString filter predicate string
     * @return <code>self</code> for method chaining
     */
    DataFrame filter(String predicateString);

    /**
     * Filters data rows that are not valid according to an input predicate.<br>
     * Data rows are filtered by their column values. <br>
     * If a data row is <b>filtered</b> if it is <b>not valid</b> according to the predicate.<br>
     * The filtered data rows are removed from this data frame.<br>
     * <p><code>if(!predicate.valid(row)) -&gt; remove(row)</code></p>
     *
     * @param predicate filter predicate
     * @return <code>self</code> for method chaining
     */
    DataFrame filter(FilterPredicate predicate);

    /**
     * Finds data rows from this data frame where a specified column value equals
     * an input value.
     *
     * @param colName column name
     * @param value   input value
     * @return list of found data rows
     */
    DataRows selectRows(String colName, Object value);

    /**
     * Finds data rows using a {@link FilterPredicate}.
     *
     * @param predicateString input predicate string
     * @return list of found data rows
     */
    DataRows selectRows(String predicateString);

    /**
     * Finds data rows using a {@link FilterPredicate}.
     *
     * @param predicate input predicate
     * @return list of found data rows
     */
    DataRows selectRows(FilterPredicate predicate);

    /**
     * Converts this dataframe into another dataframe using a specified transformer
     *
     * @param transformer the applied transformer
     * @return resulting dataframe
     */
    DataFrame transform(DataFrameTransform transformer);


    /**
     * Finds a data row using the primary key
     *
     * @param keyValues input key values
     * @return found data row
     */
    DataRow selectByPrimaryKey(Object... keyValues);

    /**
     * Reverses all columns
     *
     * @return <code>self</code> for method chaining
     */
    DataFrame reverse();

    /**
     * Adds a new index based on one or multiple index columns.
     * <p><b>Values in index columns must be unique for all rows</b></p>
     *
     * @param indexName   name of new index
     * @param columnNames index columns
     * @return <code>self</code> for method chaining
     */
    DataFrame addIndex(String indexName, String... columnNames);

    /**
     * Adds a new index based on one or multiple index columns.
     * <p><b>Values in index columns must be unique for all rows</b></p>
     *
     * @param indexName name of new index
     * @param columns   index columns
     * @return <code>self</code> for method chaining
     */
    DataFrame addIndex(String indexName, DataFrameColumn<?, ?>... columns);

    /**
     * Adds a new index to the dataframe
     *
     * @param index index to add
     * @return <code>self</code> for method chaining
     */
    DataFrame addIndex(Index index);

    /**
     * Returns the number of rows in this data frame
     *
     * @return number of rows
     */
    int size();

    /**
     * Returns true if the dataframe is empty (contains no rows)
     *
     * @return true if the dataframe is empty
     */
    boolean isEmpty();

    /**
     * Sets this data frame to a subset of itself.
     * Only rows between <code>from</code> and <code>to</code> remain in this data frame
     *
     * @param from lowest remaining row index
     * @param to   highest remaining row index
     * @return <code>self</code> for method chaining
     */
    DataFrame subset(int from, int to);

    /**
     * Sets this data frame to a subset of itself.
     * Only rows between <code>from</code> and <code>to</code> remain in this data frame
     *
     * @param from lowest remaining row index
     * @param to   highest remaining row index
     * @return <code>self</code> for method chaining
     */
    DataFrame filterSubset(int from, int to);


    /**
     * Creates a new data frame from a subset of this data frame.
     * Rows between <code>from</code> and <code>to</code> are added to the new data frame.
     *
     * @param from lowest row index
     * @param to   highest row index
     * @return created subset data frame
     */
    DataFrame selectSubset(int from, int to);

    /**
     * Returns a list the list of rows between <code>from</code> and <code>to</code>.
     *
     * @param from lowest row index
     * @param to   highest row index
     * @return list of rows between <code>from</code> and <code>to</code>
     */
    DataRows getRows(int from, int to);

    /**
     * Returns all rows in this data frame
     *
     * @return list of all rows
     */
    DataRows getRows();


    /**
     * Returns the header of this data frame
     *
     * @return data frame header
     */
    DataFrameHeader getHeader();

    /**
     * Concatenates two data frames. The rows from the other data frame are appended to this data frame.
     * Throws a {@link DataFrameRuntimeException} if the data frames are not compatible.
     *
     * @param other other data frame
     * @return <code>self</code> for method chaining
     */
    DataFrame concat(DataFrame other);

    /**
     * Appends the rows from a collection of data frames to this data frame.
     * Throws a {@link DataFrameRuntimeException} if the data frames are not compatible.
     *
     * @param dataFrames other data frames
     * @return <code>self</code> for method chaining
     */
    DataFrame concat(Collection<DataFrame> dataFrames);

    /**
     * Appends the rows from an array of data frames to this data frame.
     * Throws a {@link DataFrameRuntimeException} if the data frames are not compatible.
     *
     * @param dataFrames other data frames
     * @return <code>self</code> for method chaining
     */
    DataFrame concat(DataFrame... dataFrames);

    /**
     * Returns <code>true</code> if the header of an input data frame equals the header of this data frame.
     *
     * @param input input data frame
     * @return <code>true</code> if the other data frame is compatible with this data frame.
     * @see DataFrameHeader#equals(Object)
     */
    boolean isCompatible(DataFrame input);

    /**
     * Returns the data row at a specified index
     *
     * @param i index of data row
     * @return data row at  specified index
     */
    DataRow getRow(int i);

    /**
     * Returns a collection of the column names in this data frame
     *
     * @return column names
     */
    Collection<String> getColumnNames();

    /**
     * Returns a column based on its name
     *
     * @param name column name
     * @return column
     */
    DataFrameColumn<?, ?> getColumn(String name);

    /**
     * Returns a column as a specified column type.
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @param cl   class of column
     * @param <T>  type of column
     * @return found column
     */
    <T extends DataFrameColumn<?, T>> T getColumn(String name, Class<T> cl);

    /**
     * Returns a {@link NumberColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param <T>  value type of the column
     * @param <C>  column type
     * @param name column name
     * @return found column
     */
    <T extends Number, C extends NumberColumn<T, C>> NumberColumn<T, C> getNumberColumn(String name);

    /**
     * Returns a {@link StringColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    StringColumn getStringColumn(String name);

    /**
     * Returns a {@link DoubleColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    DoubleColumn getDoubleColumn(String name);

    /**
     * Returns a {@link IntegerColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    IntegerColumn getIntegerColumn(String name);

    /**
     * Returns a {@link FloatColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    FloatColumn getFloatColumn(String name);

    /**
     * Returns a {@link BooleanColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    BooleanColumn getBooleanColumn(String name);

    /**
     * Returns a {@link ByteColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    ByteColumn getByteColumn(String name);

    /**
     * Returns a {@link LocalDateColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    LocalDateColumn getLocalDateColumn(String name);

    /**
     * Returns a {@link LocalTimeColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    LocalTimeColumn getLocalTimeColumn(String name);

    /**
     * Returns a {@link LocalDateTimeColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    LocalDateTimeColumn getLocalDateTimeColumn(String name);

    /**
     * Returns an {@link InstantColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    InstantColumn getInstantColumn(String name);

    /**
     * Returns an {@link OffsetDateTimeColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    OffsetDateTimeColumn getOffsetDateTimeColumn(String name);

    /**
     * Returns a {@link ZonedDateTimeColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name name of the column
     * @return found column
     */
    ZonedDateTimeColumn getZonedDateTimeColumn(String name);

    /**
     * Returns a {@link LongColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    LongColumn getLongColumn(String name);

    /**
     * Returns a {@link ShortColumn}
     * If the column is not found or has the wrong type a {@link DataFrameRuntimeException} is thrown.
     *
     * @param name column name
     * @return found column
     */
    ShortColumn getShortColumn(String name);

    /**
     * Groups this data frame using one or more columns
     *
     * @param column group columns
     * @return {@link DataGrouping data grouping}
     * @see GroupUtil#groupBy(DataFrame, String...)
     */
    DataGrouping groupBy(String... column);

    /**
     * Joins this data frame with another data frame using the <code>LEFT JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#leftJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinLeft(DataFrame dataFrame, String... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>LEFT JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#leftJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinLeft(DataFrame dataFrame, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>LEFT JOIN</code> method.
     * Column names are altered using the provided suffixes.
     *
     * @param dataFrame   other data frame
     * @param suffixA     suffixes for columns from this data frame
     * @param suffixB     suffixes for columns from the other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#leftJoin(DataFrame, DataFrame, String, String, JoinColumn...)
     */
    JoinedDataFrame joinLeft(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>RIGHT JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#rightJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinRight(DataFrame dataFrame, String... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>LEFT JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#leftJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinRight(DataFrame dataFrame, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>RIGHT JOIN</code> method.
     * Column names are altered using the provided suffixes.
     *
     * @param dataFrame   other data frame
     * @param suffixA     suffixes for columns from this data frame
     * @param suffixB     suffixes for columns from the other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#rightJoin(DataFrame, DataFrame, String, String, JoinColumn...)
     */
    JoinedDataFrame joinRight(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>INNER JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinInner(DataFrame dataFrame, String... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>INNER JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinInner(DataFrame dataFrame, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>INNER JOIN</code> method.
     * Column names are altered using the provided suffixes.
     *
     * @param dataFrame   other data frame
     * @param suffixA     suffixes for columns from this data frame
     * @param suffixB     suffixes for columns from the other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, String, String, JoinColumn...)
     */
    JoinedDataFrame joinInner(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>OUTER JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinOuter(DataFrame dataFrame, String... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>OUTER JOIN</code> method.
     *
     * @param dataFrame   other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, JoinColumn...)
     */
    JoinedDataFrame joinOuter(DataFrame dataFrame, JoinColumn... joinColumns);

    /**
     * Joins this data frame with another data frame using the <code>OUTER JOIN</code> method.
     * Column names are altered using the provided suffixes.
     *
     * @param dataFrame   other data frame
     * @param suffixA     suffixes for columns from this data frame
     * @param suffixB     suffixes for columns from the other data frame
     * @param joinColumns join columns
     * @return joined data frame
     * @see JoinUtil#innerJoin(DataFrame, DataFrame, String, String, JoinColumn...)
     */
    JoinedDataFrame joinOuter(DataFrame dataFrame, String suffixA, String suffixB, JoinColumn... joinColumns);

    /**
     * Returns a copy of this data frame.
     * Header, columns, rows and indices are copied.
     *
     * @return copy of data frame
     */
    DataFrame copy();

    /**
     * Returns <code>true</code> if this data frame contains the input column
     *
     * @param column input column
     * @return <code>true</code> if this data frame contains the input column
     */
    boolean containsColumn(DataFrameColumn<?, ?> column);

    /**
     * Returns <code>true</code> if the input column is part of at least one index
     *
     * @param column input column
     * @return <code>true</code> if column is part of index
     */
    boolean isIndexColumn(DataFrameColumn<?, ?> column);



    /**
     * Finds matching data rows using an index and the corresponding index values
     *
     * @param name   name of index
     * @param values index values
     * @return rows found
     */
    DataRows selectRowsByIndex(String name, Object... values);


    DataRows selectRows(Collection<Integer> rowIndices);

    /**
     * Finds the first data row matching an index and the corresponding index values
     *
     * @param name   name of index
     * @param values index values
     * @return rows found
     */
    DataRow selectFirstRowByIndex(String name, Object... values);

    /**
     * Returns a new dataframe containing data rows found using an index and the corresponding index values
     *
     * @param name   name of index
     * @param values index values
     * @return dataframe containing found rows
     */
    DataFrame selectByIndex(String name, Object... values);

    /**
     * Returns a collection of all columns in this data frame
     *
     * @return collection of columns
     */
    Collection<DataFrameColumn<?, ?>> getColumns();

    /**
     * Returns the indices of this data frame
     *
     * @return data frame indices
     */
    Iterable<? extends DataRow> rows();


    /**
     * Creates a new {@link DefaultDataFrame} instance
     *
     * @return new dataframe
     */
    static DataFrame create() {
        return new DefaultDataFrame();
    }

    /**
     * Creates a new {@link DefaultDataFrame} instance with a name
     *
     * @param name dataframe name
     * @return new dataframe
     */
    static DataFrame create(String name) {
        return new DefaultDataFrame(name);
    }


    /**
     * Creates a new {@link DataFrameBuilder}
     *
     * @return dataframe builder
     */
    static DataFrameBuilder builder() {
        return new DataFrameBuilder();
    }

    /**
     * Loads a data frame from a file.
     * The matching data frame meta file must be present.
     * <code>file+'.dfm'</code>
     *
     * @param file data frame file
     * @return loaded data frame
     */
    static DataFrame load(File file) {
        return DataFrameLoader.load(file);

    }

    /**
     * Loads a data frame from a content string using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param content content string
     * @return resulting dataframe
     */
    static DataFrame load(String content) {
        return DataFrameLoader.load(content);
    }

    /**
     * Loads a data frame from a resource using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param resource    resource path
     * @param classLoader class loader used to find the resource
     * @return resulting dataframe
     */
    static DataFrame load(String resource, ClassLoader classLoader) {
        return DataFrameLoader.load(resource, classLoader);

    }

    /**
     * Loads a data frame from a URL using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param url input url
     * @return resulting dataframe
     */
    static DataFrame load(URL url) {
        return DataFrameLoader.load(url);

    }

    /**
     * Loads a data frame from a byte array using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param bytes input byte array
     * @return resulting dataframe
     */
    static DataFrame load(byte[] bytes) {
        return DataFrameLoader.load(bytes);
    }

    /**
     * Loads a data frame from a {@link InputStream} using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param is input stream
     * @return resulting dataframe
     */
    static DataFrame load(InputStream is) {
        return DataFrameLoader.load(is);
    }

    /**
     * Loads a data frame from a {@link Reader} using the default tab separated format ({@link dev.gruen.dataframe.csv.TSVFormat}).
     *
     * @param reader input reader
     * @return resulting dataframe
     */
    static DataFrame load(Reader reader) {
        return DataFrameLoader.load(reader);
    }


    /**
     * Loads a data frame from a file using a specified {@link ReadFormat}.
     *
     * @param file       input file
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(File file, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(file, readFormat);

    }


    /**
     * Loads a data frame from a content String using a specified {@link ReadFormat}.
     *
     * @param content    content string
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(String content, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(content, readFormat);
    }


    /**
     * Loads a data frame from a resource using a specified {@link ReadFormat}
     *
     * @param resource    resource path
     * @param classLoader ClassLoader used to find the resource
     * @param readFormat  read format
     * @return resulting dataframe
     */
    static DataFrame load(String resource, ClassLoader classLoader, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(resource, classLoader, readFormat);

    }

    /**
     * Loads a data frame from a URL array using a specified {@link ReadFormat}.
     *
     * @param url        input url
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(URL url, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(url, readFormat);

    }

    /**
     * Loads a data frame from a byte array using a specified {@link ReadFormat}.
     *
     * @param bytes      input byte array
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(byte[] bytes, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(bytes, readFormat);

    }

    /**
     * Loads a data frame from a {@link InputStream} using a specified {@link ReadFormat}.
     *
     * @param is         input stream
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(InputStream is, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(is, readFormat);
    }


    /**
     * Loads a data frame from a {@link Reader} using a specified {@link ReadFormat}.
     *
     * @param reader     input stream
     * @param readFormat read format
     * @return resulting dataframe
     */
    static DataFrame load(Reader reader, ReadFormat<?, ?> readFormat) {
        return DataFrameLoader.load(reader, readFormat);
    }


    /**
     * Loads a data frame from a file using a specified {@link DataReader}
     *
     * @param file   input file
     * @param reader data reader
     * @return resulting dataframe
     */
    static DataFrame load(File file, DataReader<?, ?> reader) {
        return DataFrameLoader.load(file, reader);

    }

    /**
     * Loads a data frame from a content String using a specified {@link DataReader}
     *
     * @param content content string
     * @param reader  data reader
     * @return resulting dataframe
     */
    static DataFrame load(String content, DataReader<?, ?> reader) {
        return DataFrameLoader.load(content, reader);
    }


    /**
     * Loads a data frame from a resource using a specified {@link DataReader}
     *
     * @param resource    resource path
     * @param classLoader ClassLoader used to find the resource
     * @param reader      data reader
     * @return resulting dataframe
     */
    static DataFrame load(String resource, ClassLoader classLoader, DataReader<?, ?> reader) {
        return DataFrameLoader.load(resource, classLoader, reader);

    }


    /**
     * Loads a data frame from a URL using a specified {@link DataReader}
     *
     * @param url    input url
     * @param reader data reader
     * @return resulting dataframe
     */
    static DataFrame load(URL url, DataReader<?, ?> reader) {
        return DataFrameLoader.load(url, reader);

    }

    /**
     * Loads a data frame from a byte array using a specified {@link DataReader}
     *
     * @param bytes  input byte array
     * @param reader data reader
     * @return resulting dataframe
     */
    static DataFrame load(byte[] bytes, DataReader<?, ?> reader) {
        return DataFrameLoader.load(bytes, reader);

    }

    /**
     * Loads a data frame from a {@link InputStream} using a specified {@link DataReader}
     *
     * @param is     input
     * @param reader data reader
     * @return resulting dataframe
     */
    static DataFrame load(InputStream is, DataReader<?, ?> reader) {
        return DataFrameLoader.load(is, reader);
    }

    /**
     * Loads a data frame from a {@link Reader} using a specified {@link DataReader}
     *
     * @param r      input reader
     * @param reader data reader
     * @return resulting dataframe
     */
    static DataFrame load(Reader r, DataReader<?, ?> reader) {
        return DataFrameLoader.load(r, reader);
    }


    /**
     * Loads a data frame from a {@link DataIterator}
     *
     * @param dataIterator data iterator
     * @return resulting dataframe
     */
    static DataFrame load(DataIterator<?> dataIterator) {
        return DataFrameLoader.load(dataIterator);
    }

    /**
     * Loads a data frame from a {@link DataIterator} and filters all rows using a specified predicate
     *
     * @param dataIterator data iterator
     * @param predicate    filter predicate
     * @return resulting dataframe
     */
    static DataFrame load(DataIterator<?> dataIterator, FilterPredicate predicate) {
        return DataFrameLoader.load(dataIterator, predicate);
    }

    /**
     * Loads a data frame from a file using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param file      input file
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(File file, char separator, boolean header) {
        return DataFrameLoader.fromCSV(file, separator, header);

    }

    /**
     * Loads a data frame from a content string using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param content   content string
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(String content, char separator, boolean header) {
        return DataFrameLoader.fromCSV(content, separator, header);
    }

    /**
     * Loads a data frame from a URL array using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param resource    resource path
     * @param classLoader class loader used to find the resource
     * @param separator   column separator
     * @param header      specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(String resource, ClassLoader classLoader, char separator, boolean header) {
        return DataFrameLoader.fromCSV(resource, classLoader, separator, header);

    }

    /**
     * Loads a data frame from a URL array using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param url       input url
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(URL url, char separator, boolean header) {
        return DataFrameLoader.fromCSV(url, separator, header);

    }

    /**
     * Loads a data frame from a byte array using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param bytes     input byte array
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(byte[] bytes, char separator, boolean header) {
        return DataFrameLoader.fromCSV(bytes, separator, header);

    }

    /**
     * Loads a data frame from a {@link InputStream} using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param is        input stream
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(InputStream is, char separator, boolean header) {
        return DataFrameLoader.fromCSV(is, separator, header);
    }

    /**
     * Loads a data frame from a {@link Reader} using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified. If the CSV contains no header, the columns are named V1, V2,...
     *
     * @param reader    input reader
     * @param separator column separator
     * @param header    specifies wether the csv contains a header or not
     * @return resulting dataframe
     */
    static DataFrame fromCSV(Reader reader, char separator, boolean header) {
        return DataFrameLoader.fromCSV(reader, separator, header);
    }

    /**
     * Loads a data frame from a file string using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param file         input file
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(File file, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(file, separator, headerPrefix);

    }

    /**
     * Loads a data frame from a content string using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param content      content string
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(String content, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(content, separator, headerPrefix);
    }

    /**
     * Loads a data frame from a resource using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param resource     resource path
     * @param classLoader  class loader used to find the resource
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(String resource, ClassLoader classLoader, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(resource, classLoader, separator, headerPrefix);

    }

    /**
     * Loads a data frame from a url using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param url          input url
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(URL url, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(url, separator, headerPrefix);

    }

    /**
     * Loads a data frame from a byte array using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param bytes        input byte array
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(byte[] bytes, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(bytes, separator, headerPrefix);

    }

    /**
     * Loads a data frame from a {@link InputStream} using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param is           input stream
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(InputStream is, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(is, separator, headerPrefix);
    }

    /**
     * Loads a data frame from a {@link Reader} using the CSV format ({@link dev.gruen.dataframe.csv.CSVFormat}).
     * The column separator can be specified.
     * If the header starts with a certain prefix it can be specified, otherwise the prefix should be set to "" or null
     *
     * @param r            input reader
     * @param separator    column separator
     * @param headerPrefix header prefix
     * @return resulting dataframe
     */
    static DataFrame fromCSV(Reader r, char separator, String headerPrefix) {
        return DataFrameLoader.fromCSV(r, separator, headerPrefix);
    }


    /**
     * Writes this dataframe to a file using a specified {@link DataWriter}.
     * If there is a matching {@link dev.gruen.dataframe.io.DataReader} for the {@link DataWriter}, a meta file is written automatically
     *
     * @param file       target file
     * @param dataWriter data writer used to write the dataframe
     */
    default void write(File file, DataWriter dataWriter) {
        DataFrameWriter.write(file, this, dataWriter);
    }

    /**
     * Writes this dataframe to a file using a specified {@link DataWriter}.
     * If there is a matching {@link dev.gruen.dataframe.io.ReadFormat} for the {@link DataWriter}, a meta file is written if specified.
     *
     * @param file          target file
     * @param writeMetaFile defines whether a meta file should be created
     * @param dataWriter    data writer used to write the dataframe
     */
    default void write(File file, DataWriter dataWriter, boolean writeMetaFile) {
        DataFrameWriter.write(file, this, dataWriter, writeMetaFile);
    }

    /**
     * Writes this dataframe to a {@link Writer} using a specified {@link DataWriter}.
     *
     * @param writer     target writer
     * @param dataWriter data writer used to write the dataframe
     */
    default void write(Writer writer, DataWriter dataWriter) {
        DataFrameWriter.write(writer, this, dataWriter);
    }

    /**
     * Writes this dataframe to a {@link OutputStream} using a specified {@link DataWriter}.
     *
     * @param outputStream target OutputStream
     * @param dataWriter   data writer used to write the dataframe
     */
    default void write(OutputStream outputStream, DataWriter dataWriter) {
        DataFrameWriter.write(outputStream, this, dataWriter);
    }

    /**
     * Writes this dataframe to a file using a specified {@link WriteFormat}.
     * If there is a matching {@link dev.gruen.dataframe.io.ReadFormat} for the {@link WriteFormat}, a meta file is written automatically
     *
     * @param file        target file
     * @param writeFormat defines the output format used to write the dataframe
     */
    default void write(File file, WriteFormat writeFormat) {
        DataFrameWriter.write(file, this, writeFormat);
    }

    /**
     * Writes this dataframe to a file using a specified {@link WriteFormat}.
     * If there is a matching {@link dev.gruen.dataframe.io.ReadFormat} for the {@link WriteFormat}, a meta file is written if specified
     *
     * @param file          target file
     * @param writeFormat   defines the output format used to write the dataframe
     * @param writeMetaFile defines whether a meta file should be created
     */
    default void write(File file, WriteFormat writeFormat, boolean writeMetaFile) {
        DataFrameWriter.write(file, this, writeFormat, writeMetaFile);
    }

    /**
     * Writes this dataframe to a {@link Writer} using a specified {@link WriteFormat}.
     *
     * @param writer      target writer
     * @param writeFormat data writer used to write the dataframe
     */
    default void write(Writer writer, WriteFormat writeFormat) {
        DataFrameWriter.write(writer, this, writeFormat);
    }

    /**
     * Writes this dataframe to a {@link OutputStream} using a specified {@link WriteFormat}.
     *
     * @param outputStream target OutputStream
     * @param writeFormat  data writer used to write the dataframe
     */
    default void write(OutputStream outputStream, WriteFormat writeFormat) {
        DataFrameWriter.write(outputStream, this, writeFormat);
    }

    /**
     * Writes this dataframe to a file using the default write format ({@link DataFrameWriter#DEFAULT_WRITE_FORMAT}).
     * A meta file is written automatically.
     *
     * @param file target file
     */
    default void write(File file) {
        DataFrameWriter.write(file, this);
    }

    /**
     * Writes this dataframe to a file using the default write format ({@link DataFrameWriter#DEFAULT_WRITE_FORMAT}).
     * A meta file is written if specified.
     *
     * @param file          target file
     * @param writeMetaFile defines whether a meta file should be created
     */
    default void write(File file, boolean writeMetaFile) {
        DataFrameWriter.write(file, this, writeMetaFile);
    }

    /**
     * Writes this dataframe to a {@link Writer} using the default write format ({@link DataFrameWriter#DEFAULT_WRITE_FORMAT}).
     *
     * @param writer target writer
     */
    default void write(Writer writer) {
        DataFrameWriter.write(writer, this);
    }

    /**
     * Writes this dataframe to a {@link OutputStream} using the default write format ({@link DataFrameWriter#DEFAULT_WRITE_FORMAT}).
     *
     * @param outputStream target outputStream
     */
    default void write(OutputStream outputStream) {
        DataFrameWriter.write(outputStream, this);
    }

    /**
     * Writes this dataframe to a file using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * A header is written if specified.
     * A meta file is written automatically.
     *
     * @param file        target file
     * @param separator   separator char
     * @param writeHeader defines whether the header should be written to the file
     */
    default void writeCSV(File file, char separator, boolean writeHeader) {
        DataFrameWriter.writeCSV(file, this, separator, writeHeader);
    }

    /**
     * Writes this dataframe to a file using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header and meta file are written if specified.
     *
     * @param file          target file
     * @param separator     separator char
     * @param writeHeader   defines whether the header should be written to the file
     * @param writeMetaFile defines whether a meta file should be written
     */
    default void writeCSV(File file, char separator, boolean writeHeader, boolean writeMetaFile) {
        DataFrameWriter.writeCSV(file, this, separator, writeHeader, writeMetaFile);
    }

    /**
     * Writes this dataframe to a {@link Writer} using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header and meta file are written if specified.
     *
     * @param writer      target writer
     * @param separator   separator char
     * @param writeHeader defines whether the header should be written to the file
     */
    default void writeCSV(Writer writer, char separator, boolean writeHeader) {
        DataFrameWriter.writeCSV(writer, this, separator, writeHeader);
    }

    /**
     * Writes this dataframe to a {@link OutputStream} using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header and meta file are written if specified.
     *
     * @param outputStream target OutputStream
     * @param separator    separator char
     * @param writeHeader  defines whether the header should be written to the file
     */
    default void writeCSV(OutputStream outputStream, char separator, boolean writeHeader) {
        DataFrameWriter.writeCSV(outputStream, this, separator, writeHeader);

    }

    /**
     * Writes this dataframe to a file using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header is written and a header prefix is added.
     * A meta file is written automatically.
     *
     * @param file         target file
     * @param separator    separator char
     * @param headerPrefix header prefix
     */
    default void writeCSV(File file, char separator, String headerPrefix) {
        DataFrameWriter.writeCSV(file, this, separator, headerPrefix);
    }

    /**
     * Writes this dataframe to a file using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header is written and a header prefix is added.
     * A meta file is written if specified.
     *
     * @param file          target file
     * @param separator     separator char
     * @param headerPrefix  header prefix
     * @param writeMetaFile defines whether a meta file should be written
     */
    default void writeCSV(File file, char separator, String headerPrefix, boolean writeMetaFile) {
        DataFrameWriter.writeCSV(file, this, separator, headerPrefix, writeMetaFile);
    }

    /**
     * Writes this dataframe to a  {@link Writer} using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header is written and a header prefix is added.
     *
     * @param writer       target writer
     * @param separator    separator char
     * @param headerPrefix header prefix
     */
    default void writeCSV(Writer writer, char separator, String headerPrefix) {
        DataFrameWriter.writeCSV(writer, this, separator, headerPrefix);

    }

    /**
     * Writes this dataframe to a  {@link OutputStream} using the CSV file format ({@link dev.gruen.dataframe.csv.CSVFormat}) and a specified separator.
     * Header is written and a header prefix is added.
     *
     * @param outputStream target OutputStream
     * @param separator    separator char
     * @param headerPrefix header prefix
     */
    default void writeCSV(OutputStream outputStream, char separator, String headerPrefix) {
        DataFrameWriter.writeCSV(outputStream, this, separator, headerPrefix);
    }


    /**
     * Prints this dataframe to {@link System#out}  using the default print format ({@link DataFrameWriter#DEFAULT_WRITE_FORMAT}).
     */
    default void print() {
        DataFrameWriter.print(this);
    }

    /**
     * Prints this dataframe to {@link System#out}  using a specified {@link DataWriter}.
     *
     * @param dataWriter data writer used to print the dataframe
     */
    default void print(DataWriter dataWriter) {
        DataFrameWriter.print(this, dataWriter);
    }

    /**
     * Prints this dataframe to {@link System#out}  using a specified {@link WriteFormat}.
     *
     * @param writeFormat write format used to print the dataframe
     */
    default void print(WriteFormat writeFormat) {
        DataFrameWriter.print(this, writeFormat);
    }

    /**
     * Prints this dataframe to a {@link PrintStream} using the default print format ({@link DataFrameWriter#DEFAULT_PRINT_FORMAT}).
     *
     * @param out target print stream
     */
    default void print(PrintStream out) {
        DataFrameWriter.print(this, out);
    }

    /**
     * Prints this dataframe to an {@link Appendable} (e.g. a {@link StringBuilder}) using the default print format
     * ({@link DataFrameWriter#DEFAULT_PRINT_FORMAT}). The appendable is not closed.
     *
     * @param out target appendable
     */
    default void print(Appendable out) {
        DataFrameWriter.print(this, out);
    }

    /**
     * Returns a value as {@link Object} from the specified column and row
     *
     * @param col column
     * @param row row
     * @return value
     */
    Object getValue(int col, int row);

    /**
     * Sets the value in the specified column and row
     *
     * @param col      column
     * @param row      row
     * @param newValue new value
     */
    void setValue(int col, int row, Object newValue);

    /**
     * Returns true if the value in the specified column and row is NA
     *
     * @param col column
     * @param row row
     * @return true if value is NA
     */
    boolean isNA(int col, int row);

    /**
     * Returns the head (top rows) of the dataframe
     *
     * @param size number of rows
     * @return head dataframe
     */
    DataFrame head(int size);

    /**
     * Returns the head (top rows) of the dataframe
     *
     * @return head dataframe
     */
    DataFrame head();

    /**
     * Returns the tail (bottom rows) of the dataframe
     *
     * @param size number of rows
     * @return head dataframe
     */
    DataFrame tail(int size);

    /**
     * Returns the tail (bottom rows) of the dataframe
     *
     * @return head dataframe
     */
    DataFrame tail();

    /**
     * Clears all columns
     */
    void clear();
}
