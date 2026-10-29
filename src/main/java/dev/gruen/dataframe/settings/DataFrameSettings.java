package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrameColumn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maps {@link ColumnSetting column settings} to columns using {@link ColumnMatcher column matchers}.
 * Entries are applied in insertion order, so a later setting replaces an earlier setting of the same class.
 */
public class DataFrameSettings {
    private final List<Entry> entries;

    public DataFrameSettings() {
        this.entries = new ArrayList<>();
    }

    private DataFrameSettings(List<Entry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    /**
     * Applies all settings with a matching matcher to a column.
     *
     * @param column column
     */
    public void applyToColumn(DataFrameColumn<?, ?> column) {
        for (Entry entry : entries) {
            if (entry.matcher.match(column)) {
                column.getSettings().add(entry.setting);
            }
        }
    }

    public DataFrameSettings add(ColumnSetting setting, String... names) {
        return addColumnSetting(ColumnMatchers.byName(names), setting);
    }

    public DataFrameSettings add(ColumnSetting setting, Integer... columnIndices) {
        return addColumnSetting(ColumnMatchers.byIndex(columnIndices), setting);
    }

    public DataFrameSettings addByValueType(ColumnSetting setting, Class<?>... types) {
        return addColumnSetting(ColumnMatchers.byValueType(types), setting);
    }

    @SafeVarargs
    public final DataFrameSettings addByColumnType(ColumnSetting setting, Class<? extends DataFrameColumn<?, ?>>... types) {
        return addColumnSetting(ColumnMatchers.byColumnType(types), setting);
    }

    public DataFrameSettings addColumnSettings(ColumnMatcher matcher, ColumnSetting... settings) {
        for (ColumnSetting setting : settings) {
            addColumnSetting(matcher, setting);
        }
        return this;
    }

    public DataFrameSettings addColumnSetting(ColumnMatcher matcher, ColumnSetting setting) {
        entries.add(new Entry(matcher, setting));
        return this;
    }

    public boolean remove(ColumnSetting setting) {
        return entries.removeIf(e -> e.setting == setting);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public static DataFrameSettingsBuilder create() {
        return new DataFrameSettingsBuilder();
    }

    public static DataFrameSettingsBuilder builder() {
        return create();
    }

    private static final class Entry {
        private final ColumnMatcher matcher;
        private final ColumnSetting setting;

        private Entry(ColumnMatcher matcher, ColumnSetting setting) {
            this.matcher = matcher;
            this.setting = setting;
        }
    }

    public static final class DataFrameSettingsBuilder {
        private final List<Entry> entries = new ArrayList<>();

        private DataFrameSettingsBuilder() {
        }

        public DataFrameSettingsBuilder addColumnSettings(ColumnMatcher matcher, ColumnSetting... settings) {
            for (ColumnSetting setting : settings) {
                addColumnSetting(matcher, setting);
            }
            return this;
        }

        public DataFrameSettingsBuilder addColumnSetting(ColumnMatcher matcher, ColumnSetting setting) {
            entries.add(new Entry(matcher, setting));
            return this;
        }

        public DataFrameSettingsBuilder add(ColumnSetting setting, ColumnMatcher matcher) {
            return addColumnSetting(matcher, setting);
        }

        /**
         * Creates an independent snapshot of the added entries.
         *
         * @return data frame settings
         */
        public DataFrameSettings build() {
            return new DataFrameSettings(Collections.unmodifiableList(entries));
        }
    }
}
