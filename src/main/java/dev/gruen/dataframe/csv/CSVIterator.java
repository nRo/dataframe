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

package dev.gruen.dataframe.csv;

import dev.gruen.dataframe.common.StringSplitter;
import dev.gruen.dataframe.io.BufferedStreamIterator;
import dev.gruen.dataframe.io.ColumnInformation;
import dev.gruen.dataframe.io.DataIterator;
import dev.gruen.dataframe.settings.DataFrameSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.*;

/**
 * Created by Alex on 17.06.2017.
 */
public class CSVIterator extends BufferedStreamIterator<CSVRow> implements DataIterator<CSVRow> {
    private static final Logger log = LoggerFactory.getLogger(CSVIterator.class);

    private int lineNumber = 0;
    private final CSVSettings csvSettings;
    private final DataFrameSettings dataFrameSettings;
    private final CSVHeader header = new CSVHeader();
    private int cols = -1;
    private final Set<String> ignoredColumns;
    private final Set<String> includedColumns;
    private final Map<String, Class<?>> colTypes;
    private final Map<String, Integer> selectedColumnsIndex = new HashMap<>();
    private final List<ColumnInformation> columnInformations = new ArrayList<>();
    private CSVRow bufferedRow = null;
    private boolean[] skipIndices;
    private final StringSplitter stringSplitter = new StringSplitter();

    public CSVIterator(BufferedReader reader, CSVSettings csvSettings, CSVColumnSettings columnSettings) {
        this(reader, csvSettings, columnSettings, null);
    }

    public CSVIterator(BufferedReader reader, CSVSettings csvSettings, CSVColumnSettings columnSettings,
                       DataFrameSettings dataFrameSettings) {
        super(reader);
        this.dataFrameSettings = dataFrameSettings;
        this.csvSettings = csvSettings;
        ignoredColumns = new HashSet<>(columnSettings.getIgnoreColumns());
        includedColumns = new HashSet<>(columnSettings.getSelectColumns());
        colTypes = new HashMap<>(columnSettings.getColumnTypeMap());
        this.stringSplitter.setDetectQuotes(csvSettings.isQuoteDetection());
        this.stringSplitter.setDetectSingleQuotes(csvSettings.isSingleQuoteDetection());
        this.stringSplitter.setEscapeChar(csvSettings.getEscapeChar());
        int j = 0;
        for (String col : columnSettings.getSelectColumns()) {
            selectedColumnsIndex.put(col, j++);
        }
        try {
            initHeader();
        } catch (RuntimeException | Error e) {
            close();
            throw e;
        }
    }


    /**
     * Reads and created the csv header
     */
    public void initHeader() {
        try {
            CSVRow row = getNext();
            skipIndices = new boolean[row.size()];

            int skipIndex = 0;
            if (csvSettings.isContainsHeader()) {
                if (!row.get(0).startsWith(csvSettings.getHeaderPrefix())) {
                    throw new CSVException("invalid header prefix in first line");
                }
                String name = row.get(0);
                name = csvSettings.getHeaderPrefix() == null ? name : name.substring(csvSettings.getHeaderPrefix().length());
                if (includeColumn(name)) {
                    header.add(name);
                    skipIndices[skipIndex++]  = false;
                }
                else{
                    header.incrementEmptyColumnIndex();
                    skipIndices[skipIndex++]  = true;
                }
                for (int i = 1; i < row.size(); i++) {
                    name = row.get(i);
                    if (!includeColumn(name)) {
                        skipIndices[skipIndex++]  = true;
                        continue;
                    }
                    skipIndices[skipIndex++]  = false;
                    header.add(name);
                }
            } else {
                for (int i = 0; i < row.size(); i++) {
                    if (!includeColumn(header.getNextEmptyColumnName())) {
                        header.incrementEmptyColumnIndex();
                        skipIndices[skipIndex++]  = true;
                        continue;
                    }
                    skipIndices[skipIndex++]  = false;
                    header.add();
                }
                bufferedRow = row;
            }
            for (int i = 0; i < header.size(); i++) {
                ColumnInformation columnInformation;
                String name = header.get(i);
                Class<?> type;
                if ((type = colTypes.get(name)) != null) {
                    columnInformation = new ColumnInformation(i, name, type);
                } else {
                    columnInformation = new ColumnInformation(i, name, true);
                }
                this.columnInformations.add(columnInformation);
            }
        } catch (Exception e) {
            throw new CSVRuntimeException("error creating csv header", e);
        }
        loadNext();
    }

    private boolean includeColumn(String col) {
        if (includedColumns.isEmpty()) {
            return !ignoredColumns.contains(col);
        }
        return includedColumns.contains(col);
    }

    @Override
    public CSVRow next() {
        if(bufferedRow != null){
            CSVRow nextRow = bufferedRow;
            bufferedRow = null;
            return nextRow;
        }
        return super.next();
    }

    /**
     * Reads the csv input stream and returns a csv row
     *
     * @return next csv row
     */
    @Override
    protected CSVRow getNext() {

        try {
            lineNumber++;
            String line = getLine();
            while (line != null && "".equals(line.trim())) {
                line = getLine();
            }
            if (line == null) {
                return null;
            }
            for (String prefix : csvSettings.getSkipPrefixes()) {
                if (prefix != null && !"".equals(prefix) && line.startsWith(prefix)) {
                    return getNext();
                }
            }
            String[] values = stringSplitter.splitQuoted(line, csvSettings.getSeparator());

            if (cols == -1) {
                cols = values.length;
            } else {
                if (values.length != cols) {
                    throw new CSVException(String.format("unequal number of column %d != %d in line %d", values.length, cols, lineNumber));
                }
            }
            if(skipIndices != null && header.size() != values.length){
                String[] filteredValues = new String[header.size()];
                int j = 0;
                for(int i = 0; i < values.length; i++){
                    if(!skipIndices[i]){
                        filteredValues[j++] = values[i];
                    }
                }
                values = filteredValues;
            }
            return new CSVRow(header, values, lineNumber);

        } catch (Exception e) {
            log.error("error reading csv row {}", lineNumber, e);
            close();
            throw new CSVRuntimeException(String.format("error reading csv row: %d", lineNumber),e);
        }
    }

    @Override
    public List<ColumnInformation> getColumnsInformation() {
        return columnInformations;
    }

    @Override
    public DataFrameSettings getDataFrameSettings() {
        return dataFrameSettings;
    }

    @Override
    public Iterator<CSVRow> iterator() {
        return this;
    }
}
