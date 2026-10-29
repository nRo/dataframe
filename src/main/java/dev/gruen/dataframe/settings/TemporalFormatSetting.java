package dev.gruen.dataframe.settings;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Defines the format used to parse and print the values of a temporal column.
 * Without this setting, temporal columns use the ISO-8601 format.
 * <p>
 * Patterns are created with {@link Locale#ENGLISH} if no locale is specified.
 */
public final class TemporalFormatSetting extends ColumnSetting {
    private final DateTimeFormatter formatter;

    public TemporalFormatSetting(String pattern) {
        this(pattern, Locale.ENGLISH);
    }

    public TemporalFormatSetting(String pattern, Locale locale) {
        this(DateTimeFormatter.ofPattern(
                Objects.requireNonNull(pattern, "pattern"),
                Objects.requireNonNull(locale, "locale")));
    }

    public TemporalFormatSetting(DateTimeFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}
