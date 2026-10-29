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
import dev.gruen.dataframe.type.impl.BooleanType;

/**
 * Created by Alex on 09.03.2016.
 */
public class BooleanColumn extends BasicColumn<Boolean, BooleanColumn> {

    private final BooleanType valueType = new BooleanType(getSettings());

    public BooleanColumn() {
        super(Boolean.class);
    }

    public BooleanColumn(String name) {
        super(name, Boolean.class);
    }

    public BooleanColumn(String name, Boolean[] values) {
        super(name, values);
    }

    public BooleanColumn(String name, Boolean[] values, int size) {
        super(name, values, size);
    }


    @Override
    public BooleanType getValueType() {
        return valueType;
    }

    public BooleanColumn and(BooleanColumn other) {
        for (int i = 0; i < Math.min(size(), other.size()); i++) {
            values[i] = values[i] && other.values[i];
        }
        return this;
    }

    public BooleanColumn andNot(BooleanColumn other) {
        for (int i = 0; i < Math.min(size(), other.size()); i++) {
            values[i] = values[i] && !other.values[i];
        }
        return this;
    }

    public BooleanColumn or(BooleanColumn other) {
        for(int i  = 0; i < Math.min(size(),other.size());i++){
            values[i] = values[i] || other.values[i];
        }
        return this;
    }

    public BooleanColumn xor(BooleanColumn other) {
        for(int i  = 0; i < Math.min(size(),other.size());i++){
            values[i] = values[i].booleanValue() != other.values[i].booleanValue();
        }
        return this;
    }

    public BooleanColumn flip() {
        for(int i  = 0; i < size();i++){
            values[i] = !values[i];
        }
        return this;
    }


    @Override
    protected BooleanColumn getThis() {
        return this;
    }


    @Override
    public BooleanColumn copy() {
        Boolean[] copyValues = new Boolean[values.length];
        toArray(copyValues);
        BooleanColumn copy = new BooleanColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public BooleanColumn copyEmpty() {
        BooleanColumn copy = new BooleanColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public <H> Boolean getValueFromRow(Row<?, H> row, H headerName) {
        return row.getBoolean(headerName);
    }

    @Override
    public Boolean getValueFromRow(Row<?, ?> row, int headerIndex) {
        return row.getBoolean(headerIndex);
    }


}
