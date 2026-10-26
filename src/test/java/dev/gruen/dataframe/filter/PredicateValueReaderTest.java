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

package dev.gruen.dataframe.filter;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.DefaultDataFrame;
import dev.gruen.dataframe.Values;
import dev.gruen.dataframe.column.BooleanColumn;
import dev.gruen.dataframe.column.DoubleColumn;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.filter.compile.PredicateCompiler;
import dev.gruen.dataframe.filter.compile.PredicateCompilerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Alex on 12.03.2016.
 */
public class PredicateValueReaderTest {
    @Test
    public void test() {

        DataFrame dataFrame = new DefaultDataFrame();
        dataFrame.addColumn(new StringColumn("name"));
        dataFrame.addColumn(new DoubleColumn("x"));
        dataFrame.addColumn(new IntegerColumn("y"));
        dataFrame.addColumn(new BooleanColumn("z"));
        dataFrame.addColumn(new BooleanColumn("v"));
        dataFrame.addColumn(new StringColumn("r"));
        dataFrame.addColumn(new IntegerColumn("n"));


        dataFrame.append("a", 1d, 5, true, true, "abc123", 1);
        dataFrame.append("b", 2d, 4, true, false, "abc/123", null);
        dataFrame.append("c", 3d, 3, false, true, "abc", Values.NA);
        dataFrame.append("d", 4d, 2, false, false, "123", 1);


        FilterPredicate predicate = PredicateCompiler.compile("(.name != 'a') AND (x < 3)");
        DataFrame filtered = dataFrame.select(predicate);
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));

        filtered = dataFrame.select("(name != 'a' && z == true) OR (name != 'd' && z == false)");
        Assertions.assertEquals(2, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("c", filtered.getRow(1).getString("name"));


        filtered = dataFrame.select("x >= 1");
        Assertions.assertEquals(4, filtered.size());

        filtered = dataFrame.select("x > 1");
        Assertions.assertEquals(3, filtered.size());

        filtered = dataFrame.select("x < 1.5");
        Assertions.assertEquals(1, filtered.size());

        filtered = dataFrame.select("x <= 1");
        Assertions.assertEquals(1, filtered.size());


        filtered = dataFrame.select("r >= '123'");
        Assertions.assertEquals(4, filtered.size());

        filtered = dataFrame.select("r > '123'");
        Assertions.assertEquals(3, filtered.size());

        filtered = dataFrame.select("r < 'xyz'");
        Assertions.assertEquals(4, filtered.size());

        filtered = dataFrame.select("r <= '123'");
        Assertions.assertEquals(1, filtered.size());

        filtered = dataFrame.select("((((.name != 'a')) AND (x < 3.5))) OR((y == 2))");
        Assertions.assertEquals(3, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("c", filtered.getRow(1).getString("name"));
        Assertions.assertEquals("d", filtered.getRow(2).getString("name"));


        filtered = dataFrame.select("r ~= /[a-z]+.+/");
        Assertions.assertEquals(3, filtered.size());

        filtered = dataFrame.select(".r ~= /[a-z]+\\/[0-9]+/");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));


        filtered = dataFrame.select("!(name == 'a')");
        Assertions.assertEquals(3, filtered.size());


        filtered = dataFrame.select("!((name == 'a') OR (.x > 2))");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));


        filtered = dataFrame.select("z == true");
        Assertions.assertEquals(2, filtered.size());

        filtered = dataFrame.select("(.z == true) XOR (v == true)");
        Assertions.assertEquals(2, filtered.size());

        DataFrame filtered2 = dataFrame.select(".z == true XOR v == true");
        Assertions.assertEquals(2, filtered2.size());
        Assertions.assertEquals(filtered, filtered2);


        filtered = dataFrame.select("(z == true) NOR (v == true)");
        Assertions.assertEquals(1, filtered.size());
        filtered2 = dataFrame.select("z == true NOR v == true");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals(filtered, filtered2);


        filtered = dataFrame.select(".z == .v");
        Assertions.assertEquals(2, filtered.size());
        Assertions.assertEquals("a", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("d", filtered.getRow(1).getString("name"));

        filtered = dataFrame.select(".z != .v");
        Assertions.assertEquals(2, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("c", filtered.getRow(1).getString("name"));

        filtered = dataFrame.select(".z == true && .v == true");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("a", filtered.getRow(0).getString("name"));

        filtered = dataFrame.select("z == true || v == true");
        Assertions.assertEquals(3, filtered.size());

        filtered = dataFrame.select("n == null");
        Assertions.assertEquals(2, filtered.size());
    }


    @Test
    public void likeTest() {
        DataFrame dataFrame = new DefaultDataFrame();
        dataFrame.addColumn(new StringColumn("name"));
        dataFrame.addColumn(new IntegerColumn("value"));
        dataFrame.append("A B C", 1);
        dataFrame.append("A B C", 2);
        dataFrame.append("A D C", 3);
        dataFrame.append("C D E", 4);
        dataFrame.append("E F C", 5);
        dataFrame.append("A F C", 6);
        dataFrame.append("G C H", 7);
        DataFrame filtered = dataFrame.select("name LIKE 'A B C'");
        Assertions.assertEquals(2, filtered.size());

        filtered = dataFrame.select("name LIKE 'A _ C'");
        Assertions.assertEquals(4, filtered.size());

        filtered = dataFrame.select("name LIKE '%C'");
        Assertions.assertEquals(5, filtered.size());

        filtered = dataFrame.select("name LIKE 'A%'");
        Assertions.assertEquals(4, filtered.size());

        filtered = dataFrame.select("name LIKE 'E%'");
        Assertions.assertEquals(1, filtered.size());

        filtered = dataFrame.select("name LIKE '% _ C'");
        Assertions.assertEquals(5, filtered.size());

        filtered = dataFrame.select("name LIKE '% _ %'");
        Assertions.assertEquals(7, filtered.size());

        filtered = dataFrame.select("name LIKE '%D%'");
        Assertions.assertEquals(2, filtered.size());

        filtered = dataFrame.select("name LIKE '%D%'");
        Assertions.assertEquals(2, filtered.size());

        filtered = dataFrame.select("name LIKE '%C%'");
        Assertions.assertEquals(7, filtered.size());

        filtered = dataFrame.select("name LIKE 'A'");
        Assertions.assertEquals(0, filtered.size());

        filtered = dataFrame.select("name LIKE '_'");
        Assertions.assertEquals(0, filtered.size());

        filtered = dataFrame.select("name LIKE '%AAAAAAAA'");
        Assertions.assertEquals(0, filtered.size());

        filtered = dataFrame.select("name LIKE '%AAAAAAAA%'");
        Assertions.assertEquals(0, filtered.size());

        filtered = dataFrame.select("name LIKE 'AAAAAAAA%'");
        Assertions.assertEquals(0, filtered.size());
    }

    @Test
    public void numberTest() {
        DataFrame dataFrame = new DefaultDataFrame();
        dataFrame.addColumn(new StringColumn("name"));
        dataFrame.addColumn(new DoubleColumn("x"));
        dataFrame.addColumn(new IntegerColumn("y"));
        dataFrame.addColumn(new StringColumn("z"));

        dataFrame.append("a", 1d, 5, "10");
        dataFrame.append("b", 2.3d, 4, "2");
        dataFrame.append("c", 3.5d, 3, null);
        dataFrame.append("d", null, 5, "4.5");
        dataFrame.append("e", 4.5, 5, "5.4321");


        DataFrame filtered = dataFrame.select("x == 3.5");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("c", filtered.getRow(0).getString("name"));

        filtered = dataFrame.select("x > 4");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("e", filtered.getRow(0).getString("name"));

        filtered = dataFrame.select("x <= 3.5");
        Assertions.assertEquals(3, filtered.size());
        Assertions.assertEquals("a", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("b", filtered.getRow(1).getString("name"));
        Assertions.assertEquals("c", filtered.getRow(2).getString("name"));

        filtered = dataFrame.select("x >= 2 && x < 5.5");
        Assertions.assertEquals(3, filtered.size());
        Assertions.assertEquals("b", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("c", filtered.getRow(1).getString("name"));
        Assertions.assertEquals("e", filtered.getRow(2).getString("name"));

        filtered = dataFrame.select("x == null");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("d", filtered.getRow(0).getString("name"));


        filtered = dataFrame.select("z == 5.4321");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("e", filtered.getRow(0).getString("name"));

        filtered = dataFrame.select("z == 10");
        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("a", filtered.getRow(0).getString("name"));


        filtered = dataFrame.select("z >= 4.5 && z < 5.5");
        Assertions.assertEquals(2, filtered.size());
        Assertions.assertEquals("d", filtered.getRow(0).getString("name"));
        Assertions.assertEquals("e", filtered.getRow(1).getString("name"));

        filtered = dataFrame.select("z > 6");
        Assertions.assertEquals(0, filtered.size());
    }

    @Test
    public void testWrongOperation() {
        Assertions.assertThrows(PredicateCompilerException.class,
                () -> PredicateCompiler.compile("((name != 'a') X (x < 3.5)) OR (y == 2)"));
    }

    @Test
    public void testBrackets() {
        Assertions.assertThrows(PredicateCompilerException.class,
                () -> PredicateCompiler.compile("((name != 'a') AND (x < 3.5) OR (y == 2)"));
    }

    @Test
    public void testValue() {
        Assertions.assertThrows(PredicateCompilerException.class,
                () -> PredicateCompiler.compile("((name != 'a') AND (x < a) OR (y == 2)"));
    }

    @Test
    public void testColValue() {
        Assertions.assertThrows(PredicateCompilerException.class,
                () -> PredicateCompiler.compile("((name != .a) AND (x < a) OR (y == 2)"));
    }
}
