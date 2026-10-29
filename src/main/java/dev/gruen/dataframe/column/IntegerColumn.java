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

package dev.gruen.dataframe.column;

import dev.gruen.dataframe.common.Row;
import dev.gruen.dataframe.type.impl.IntegerType;

/**
 * Created by Alex on 09.03.2016.
 */
public class IntegerColumn extends NumberColumn<Integer, IntegerColumn> {

    private final IntegerType valueType = new IntegerType(getSettings());

    public IntegerColumn() {
        super(Integer.class);
    }

    public IntegerColumn(String name) {
        super(name, Integer.class);
    }

    public IntegerColumn(String name, Integer[] values) {
        super(name, values);
    }

    public IntegerColumn(String name, Integer[] values, int size) {
        super(name, values, size);
    }


    @Override
    public IntegerType getValueType() {
        return valueType;
    }

    @Override
    protected IntegerColumn getThis() {
        return this;
    }

    @Override
    public IntegerColumn copy() {
        Integer[] copyValues = new Integer[values.length];
        toArray(copyValues);
        IntegerColumn copy = new IntegerColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public <H> Integer getValueFromRow(Row<?, H> row, H headerName) {
        return row.getInteger(headerName);
    }

    @Override
    public Integer getValueFromRow(Row<?, ?> row, int headerIndex) {
        return row.getInteger(headerIndex);
    }

    @Override
    public IntegerColumn copyEmpty() {
        IntegerColumn copy = new IntegerColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
