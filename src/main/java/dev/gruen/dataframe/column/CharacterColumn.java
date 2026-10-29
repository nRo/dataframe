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


import dev.gruen.dataframe.type.impl.CharacterType;

/**
 * Created by Alex on 09.03.2016.
 */
public class CharacterColumn extends BasicColumn<Character, CharacterColumn> {

    private final CharacterType valueType = new CharacterType(getSettings());

    public CharacterColumn() {
        super(Character.class);
    }

    public CharacterColumn(String name) {
        super(name, Character.class);
    }

    public CharacterColumn(String name, Character[] values) {
        super(name, values);
    }

    public CharacterColumn(String name, Character[] values, int size) {
        super(name, values, size);
    }


    @Override
    public CharacterType getValueType() {
        return valueType;
    }

    @Override
    protected CharacterColumn getThis() {
        return this;
    }


    @Override
    public CharacterColumn copy() {
        Character[] copyValues = new Character[values.length];
        toArray(copyValues);
        CharacterColumn copy = new CharacterColumn(getName(), copyValues, size());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }

    @Override
    public CharacterColumn copyEmpty() {
        CharacterColumn copy = new CharacterColumn(getName());
        getSettings().applyTo(copy.getSettings());
        return copy;
    }
}
