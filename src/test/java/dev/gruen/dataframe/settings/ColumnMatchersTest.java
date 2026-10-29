package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.column.DoubleColumn;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.type.impl.IntegerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColumnMatchersTest {

    private final StringColumn a = new StringColumn("a");
    private final IntegerColumn b = new IntegerColumn("b");
    private final DoubleColumn c = new DoubleColumn("c");

    @Test
    public void byNameTest() {
        ColumnMatcher matcher = ColumnMatchers.byName("a", "c");
        Assertions.assertTrue(matcher.match(a));
        Assertions.assertFalse(matcher.match(b));
        Assertions.assertTrue(matcher.match(c));
    }

    @Test
    public void byIndexTest() {
        DataFrame.create().addColumns(a, b, c);
        ColumnMatcher matcher = ColumnMatchers.byIndex(0, 2);
        Assertions.assertTrue(matcher.match(a));
        Assertions.assertFalse(matcher.match(b));
        Assertions.assertTrue(matcher.match(c));
    }

    @Test
    public void byIndexWithoutDataFrameTest() {
        Assertions.assertEquals(-1, a.getColumnIndex());
        a.setColumnIndex(3);
        Assertions.assertEquals(3, a.getColumnIndex());
        Assertions.assertTrue(ColumnMatchers.byIndex(3).match(a));
        Assertions.assertFalse(ColumnMatchers.byIndex(0).match(a));
    }

    @Test
    public void byValueTypeTest() {
        Assertions.assertTrue(ColumnMatchers.byValueType(Integer.class).match(b));
        Assertions.assertTrue(ColumnMatchers.byValueType(IntegerType.class).match(b));
        Assertions.assertFalse(ColumnMatchers.byValueType(Integer.class).match(a));
    }

    @Test
    public void byColumnTypeTest() {
        ColumnMatcher matcher = ColumnMatchers.byColumnType(StringColumn.class, DoubleColumn.class);
        Assertions.assertTrue(matcher.match(a));
        Assertions.assertFalse(matcher.match(b));
        Assertions.assertTrue(matcher.match(c));
    }

    @Test
    public void allWithoutArgumentsMatchesEverythingTest() {
        ColumnMatcher matcher = ColumnMatchers.all();
        Assertions.assertTrue(matcher.match(a));
        Assertions.assertTrue(matcher.match(b));
        Assertions.assertTrue(matcher.match(c));
    }

    @Test
    public void anyTest() {
        ColumnMatcher matcher = ColumnMatchers.any(ColumnMatchers.byName("a"), ColumnMatchers.byName("b"));
        Assertions.assertTrue(matcher.match(a));
        Assertions.assertTrue(matcher.match(b));
        Assertions.assertFalse(matcher.match(c));
    }

    @Test
    public void allMatchersTest() {
        ColumnMatcher matcher = ColumnMatchers.all(
                ColumnMatchers.byName("a", "b"),
                ColumnMatchers.byValueType(Integer.class));
        Assertions.assertFalse(matcher.match(a));
        Assertions.assertTrue(matcher.match(b));
        Assertions.assertFalse(matcher.match(c));
    }

    @Test
    public void notTest() {
        ColumnMatcher matcher = ColumnMatchers.not(ColumnMatchers.byName("a"));
        Assertions.assertFalse(matcher.match(a));
        Assertions.assertTrue(matcher.match(b));
    }
}
