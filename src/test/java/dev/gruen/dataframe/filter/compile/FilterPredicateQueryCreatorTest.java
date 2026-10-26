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

package dev.gruen.dataframe.filter.compile;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.DefaultDataFrame;
import dev.gruen.dataframe.column.BooleanColumn;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.query.term.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilterPredicateQueryCreatorTest {
    private DataFrame dataFrame;

    @BeforeEach
    public void setUp() {
        dataFrame = new DefaultDataFrame();
        dataFrame.addColumn(new StringColumn("name"));
        dataFrame.addColumn(new IntegerColumn("x"));
        dataFrame.addColumn(new IntegerColumn("y"));
        dataFrame.addColumn(new IntegerColumn("my col"));
        dataFrame.addColumn(new BooleanColumn("flag"));
        dataFrame.append("abc", 1, 1, 5, true);
        dataFrame.append("bcd", 2, 2, 1, false);
        dataFrame.append("cde", 2, 1, 2, true);
        dataFrame.append("def", 3, 4, 3, false);
    }

    private String names(String query) {
        DataFrame filtered = dataFrame.select(query);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filtered.size(); i++) {
            sb.append(filtered.getRow(i).getString("name"));
        }
        return sb.toString();
    }

    @Test
    public void testColumnName() {
        Assertions.assertEquals("abc", FilterPredicateQueryCreator.columnName(new Field("abc")));
        Assertions.assertEquals("abc", FilterPredicateQueryCreator.columnName(new Field(".abc")));
        Assertions.assertEquals("a b", FilterPredicateQueryCreator.columnName(new Field(".'a b'")));
        Assertions.assertEquals("a b", FilterPredicateQueryCreator.columnName(new Field("'a b'")));
        Assertions.assertEquals("a b", FilterPredicateQueryCreator.columnName(new Field("\"a b\"")));
    }

    @Test
    public void testBlankIsEmpty() {
        Assertions.assertEquals("", PredicateCompiler.compile("  ").toString());
        Assertions.assertEquals("abcbcdcdedef", names("  "));
    }

    @Test
    public void testPrefixedColumn() {
        Assertions.assertEquals("abc", names(".x == 1"));
        Assertions.assertEquals("abc", names("x eq 1"));
        Assertions.assertEquals("bcdcdedef", names("x != 1"));
    }

    @Test
    public void testColumnComparison() {
        Assertions.assertEquals("abcbcd", names(".x == .y"));
        Assertions.assertEquals("cde", names("x > .y"));
        Assertions.assertEquals("def", names(".x < .y"));
        Assertions.assertEquals("abcbcdcde", names("x >= .y && name != 'def'"));
    }

    @Test
    public void testQuotedColumns() {
        Assertions.assertEquals("abc", names("'my col' > 4"));
        Assertions.assertEquals("abc", names("'my col' > .x && x != 2"));
    }

    @Test
    public void testPrecedence() {
        Assertions.assertEquals("abcbcd", names("x == 1 || x == 2 && y == 2"));
        Assertions.assertEquals("bcd", names("(x == 1 || x == 2) && y == 2"));
    }

    @Test
    public void testLogicalOperators() {
        Assertions.assertEquals("abcbcdcde", names("flag == true XOR x < 3 && y == 2"));
        Assertions.assertEquals("bcddef", names("flag == true NOR x == 1"));
        Assertions.assertEquals("bcddef", names("!(flag == true)"));
    }

    @Test
    public void testInAndText() {
        Assertions.assertEquals("abcdef", names("x in (1, 3)"));
        Assertions.assertEquals("bcdcde", names("x !in (1, 3)"));
        Assertions.assertEquals("abcbcd", names("name *= 'bc'"));
    }

    @Test
    public void testLikeAndRegex() {
        Assertions.assertEquals("abc", names("name LIKE 'a%'"));
        Assertions.assertEquals("bcdcde", names("name ~= /[b-d]{2}.*/ && name != 'abc'"));
    }

    @Test
    public void testNull() {
        dataFrame.append("ghi", null, 1, 1, null);
        Assertions.assertEquals("ghi", names("x == null"));
    }

    @Test
    public void testFlagIsNotSupported() {
        Assertions.assertThrows(PredicateCompilerException.class, () -> PredicateCompiler.compile("flag"));
        Assertions.assertThrows(PredicateCompilerException.class, () -> PredicateCompiler.compile("!flag"));
        Assertions.assertThrows(PredicateCompilerException.class, () -> PredicateCompiler.compile("x == 1 && flag"));
    }

    @Test
    public void testCommaDecimalIsNotSupported() {
        Assertions.assertThrows(PredicateCompilerException.class, () -> PredicateCompiler.compile("x > 1,5"));
    }

    @Test
    public void testSyntaxError() {
        PredicateCompilerException e = Assertions.assertThrows(PredicateCompilerException.class,
                () -> PredicateCompiler.compile("x == "));
        Assertions.assertNotNull(e.getMessage());
    }

    @Test
    public void testFilterPredicateCompile() {
        FilterPredicate predicate = FilterPredicate.compile("x == 2 && y == 1");
        Assertions.assertEquals(1, dataFrame.select(predicate).size());
    }
}
