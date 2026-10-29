package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrameColumn;

/**
 * Selects the columns a setting is applied to.
 */
@FunctionalInterface
public interface ColumnMatcher {
    boolean match(DataFrameColumn<?, ?> column);
}
