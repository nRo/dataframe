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
import dev.gruen.dataframe.type.impl.ShortType;

/**
 * Created by Alex on 09.03.2016.
 */
public class ShortColumn extends NumberColumn<Short, ShortColumn> {

    private final ShortType valueType = new ShortType(getSettings());

    public ShortColumn() {
        super(Short.class);
    }

    public ShortColumn(String name) {
        super(name, Short.class);
    }

    public ShortColumn(String name, Short[] values) {
        super(name, values);
    }

    public ShortColumn(String name, Short[] values, int size) {
        super(name, values, size);
    }


    @Override
    public ShortType getValueType() {
        return valueType;
    }

    @Override
    protected ShortColumn getThis() {
        return this;
    }

    @Override
    public ShortColumn copy() {
        Short[] copyValues = new Short[values.length];
        toArray(copyValues);
        ShortColumn copy = new ShortColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
    @Override
    public <H> Short getValueFromRow(Row<?, H> row, H headerName) {
        return row.getShort(headerName);
    }

    @Override
    public Short getValueFromRow(Row<?, ?> row, int headerIndex) {
        return row.getShort(headerIndex);
    }

    @Override
    public ShortColumn copyEmpty() {
        ShortColumn copy = new ShortColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
