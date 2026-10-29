package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;
import dev.gruen.dataframe.settings.TemporalFormatSetting;
import dev.gruen.dataframe.settings.TimeZoneSetting;
import dev.gruen.dataframe.type.ValueType;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Comparator;

/**
 * Base class of the value types for {@code java.time} values.
 * <p>
 * Values are parsed and printed in the ISO-8601 format of the type.
 * A {@link TemporalFormatSetting} of the column replaces this format.
 * A {@link TimeZoneSetting} supplies the zone for text without zone infrmation.
 * Both settings are read from the settings of the column when they are needed,
 * so settings that are added after the column was created are used as well.
 *
 * @param <T> temporal type
 */
public abstract class TemporalType<T extends TemporalAccessor> extends ValueType<T> {
    private final Comparator<T> comparator = (a, b) -> {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return compareValues(a, b);
    };

    private volatile FormatterCache formatterCache;

    protected TemporalType() {
        super();
    }

    protected TemporalType(ColumnSettings settings) {
        super(settings);
    }

    /**
     * Returns the ISO-8601 formatter that is used if the column has no {@link TemporalFormatSetting}.
     *
     * @return default formatter
     */
    protected abstract DateTimeFormatter defaultFormatter();

    /**
     * Creates a value from the result of parsing text with {@link #formatter()}.
     *
     * @param parsed parsed text
     * @return temporal value
     * @throws DateTimeException if the parsed text does not contain the required information
     */
    protected abstract T fromTemporal(TemporalAccessor parsed);

    /**
     * Compares two values that are not <code>null</code>.
     *
     * @param a first value
     * @param b second value
     * @return comparison result
     */
    protected abstract int compareValues(T a, T b);

    /**
     * Prints a value using {@link #formatter()}.
     *
     * @param value value that is not <code>null</code>
     * @return formatted value
     */
    protected String format(T value) {
        return formatter().format(value);
    }

    /**
     * Returns the formatter of the column.
     * This is the formatter of the {@link TemporalFormatSetting} or the ISO formatter of this type.
     *
     * @return formatter
     */
    protected DateTimeFormatter formatter() {
        TemporalFormatSetting setting = getSettings().get(TemporalFormatSetting.class);
        FormatterCache cache = formatterCache;
        if (cache != null && cache.setting == setting) {
            return cache.formatter;
        }
        DateTimeFormatter formatter = setting == null ? defaultFormatter() : setting.getFormatter();
        formatterCache = new FormatterCache(setting, formatter);
        return formatter;
    }

    /**
     * Returns the zone used for text without zone information.
     *
     * @return time zone
     */
    protected ZoneId zone() {
        return getSettings().getOrDefault(TimeZoneSetting.class, TimeZoneSetting.UTC).getZone();
    }

    /**
     * Returns the zone or offset of parsed text. If the text carries no such information, the zone
     * of the column is returned.
     *
     * @param parsed parsed text
     * @return zone
     */
    protected ZoneId zoneOf(TemporalAccessor parsed) {
        ZoneId zone = parsed.query(TemporalQueries.zone());
        return zone != null ? zone : zone();
    }

    /**
     * Checks if a text can be detected as a value of this type during column type auto detection.
     * This is stricter than {@link #parseOrNull(String)}: the text must not carry more information
     * than this type can hold (e.g. a date time text is not detected as date).
     *
     * @param s text
     * @return <code>true</code> if the text matches this type
     */
    public boolean matchesForDetection(String s) {
        if (s == null) {
            return false;
        }
        try {
            TemporalAccessor parsed = formatter().parse(s);
            fromTemporal(parsed);
            return isExactMatch(s, parsed);
        } catch (DateTimeException e) {
            return false;
        }
    }

    /**
     * Checks if the parsed text is an exact match for this type during auto detection.
     *
     * @param text   original text
     * @param parsed parsed text
     * @return <code>true</code> if the text is an exact match
     */
    protected boolean isExactMatch(String text, TemporalAccessor parsed) {
        return true;
    }

    protected boolean hasFormatSetting() {
        return getSettings().get(TemporalFormatSetting.class) != null;
    }

    @Override
    public Comparator<T> getComparator() {
        return comparator;
    }

    @Override
    public T parse(String s) throws ParseException {
        assertNotNull(s);
        try {
            return fromTemporal(formatter().parse(s));
        } catch (DateTimeParseException e) {
            throw new ParseException(String.format("error parsing '%s' as %s: %s",
                    s, getType().getSimpleName(), e.getMessage()), e.getErrorIndex());
        } catch (DateTimeException e) {
            throw new ParseException(String.format("error parsing '%s' as %s: %s",
                    s, getType().getSimpleName(), e.getMessage()), 0);
        }
    }

    @Override
    public String toString(T value) {
        assertNotNull(value);
        return format(value);
    }

    @Override
    public void write(Writer writer, T value) throws IOException {
        assertNotNull(value);
        writer.write(format(value));
    }

    private record FormatterCache(TemporalFormatSetting setting, DateTimeFormatter formatter) {
    }
}
