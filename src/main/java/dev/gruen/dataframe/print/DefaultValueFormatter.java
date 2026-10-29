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

package dev.gruen.dataframe.print;


import dev.gruen.dataframe.type.ValueType;
import dev.gruen.dataframe.type.impl.TemporalType;

public class DefaultValueFormatter implements ValueFormatter {
    @Override
    public String format(ValueType<?> valueType, Object value, int maxWidth) {
        if (valueType instanceof TemporalType<?> && valueType.getType().isInstance(value)) {
            // temporal values are printed with the format of their column
            value = valueType.toStringRaw(value);
        }
        if ((value instanceof String)) {
            String strVal = (String) value;
            if (strVal.length() > maxWidth) {
                value = strVal.substring(0, maxWidth - 3);
                value += "...";
            }
        }
        return String.format("%." + maxWidth + "s", value);

    }


}
