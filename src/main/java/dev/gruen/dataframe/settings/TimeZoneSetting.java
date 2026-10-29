package dev.gruen.dataframe.settings;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Defines the time zone of a temporal column.
 * <p>
 * The zone is used to complete parsed text that carries no zone or offset information
 * (for {@code Instant}, {@code OffsetDateTime} and {@code ZonedDateTime} columns).
 * It is also used to print {@code Instant} values with a custom format.
 * The default zone is UTC.
 */
public final class TimeZoneSetting extends ColumnSetting {
    public static final TimeZoneSetting UTC = new TimeZoneSetting(ZoneOffset.UTC);

    private final ZoneId zone;

    public TimeZoneSetting(ZoneId zone) {
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    public ZoneId getZone() {
        return zone;
    }
}
