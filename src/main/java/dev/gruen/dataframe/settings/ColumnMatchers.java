package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrameColumn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ColumnMatchers {

    private ColumnMatchers() {
    }

    /**
     * Matches every column
     *
     * @return matcher
     */
    public static ColumnMatcher all() {
        return (c) -> true;
    }

    public static ColumnMatcher byName(String... names) {
        final Set<String> nameSet = new HashSet<>(Arrays.asList(names));
        return (c) -> nameSet.contains(c.getName());
    }

    public static ColumnMatcher byIndex(Integer... columnIndices) {
        final Set<Integer> idxSet = new HashSet<>(Arrays.asList(columnIndices));
        return (c) -> idxSet.contains(c.getColumnIndex());
    }

    public static ColumnMatcher byValueType(Class<?>... types) {
        final Set<Class<?>> clSet = new HashSet<>(Arrays.asList(types));
        return (c) -> clSet.contains(c.getValueType().getType()) ||
                clSet.contains(c.getValueType().getClass());
    }

    @SafeVarargs
    public static ColumnMatcher byColumnType(Class<? extends DataFrameColumn<?, ?>>... types) {
        final Set<Class<?>> clSet = new HashSet<>(Arrays.asList(types));
        return (c) -> clSet.contains(c.getClass());
    }

    /**
     * Matches if at least one of the matchers matches
     *
     * @param matchers matchers
     * @return matcher
     */
    public static ColumnMatcher any(ColumnMatcher... matchers) {
        final ColumnMatcher[] copy = matchers.clone();
        return (c) -> {
            for (ColumnMatcher matcher : copy) {
                if (matcher.match(c)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Matches if all matchers match
     *
     * @param matchers matchers
     * @return matcher
     */
    public static ColumnMatcher all(ColumnMatcher... matchers) {
        final ColumnMatcher[] copy = matchers.clone();
        return (c) -> {
            for (ColumnMatcher matcher : copy) {
                if (!matcher.match(c)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static ColumnMatcher not(ColumnMatcher matcher) {
        return (c) -> !matcher.match(c);
    }
}
