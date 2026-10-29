package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Value type for {@link OffsetDateTime}.
 * The binary format is the epoch second as <code>long</code>, the nano of second as <code>int</code>
 * and the offset in seconds as <code>int</code>.
 * <p>
 * Text without offset is interpreted in the zone of the column
 * (see {@link dev.gruen.dataframe.settings.TimeZoneSetting}).
 */
public class OffsetDateTimeType extends TemporalType<OffsetDateTime> {
    public OffsetDateTimeType() {
        super();
    }

    public OffsetDateTimeType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<OffsetDateTime> getType() {
        return OffsetDateTime.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    }

    @Override
    protected OffsetDateTime fromTemporal(TemporalAccessor parsed) {
        if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
            return OffsetDateTime.from(parsed);
        }
        return LocalDateTime.from(parsed).atZone(zoneOf(parsed)).toOffsetDateTime();
    }

    @Override
    protected int compareValues(OffsetDateTime a, OffsetDateTime b) {
        return a.compareTo(b);
    }

    @Override
    public OffsetDateTime read(DataInputStream dis) throws IOException {
        long epochSecond = dis.readLong();
        int nano = dis.readInt();
        int offsetSeconds = dis.readInt();
        return create(epochSecond, nano, offsetSeconds);
    }

    @Override
    public OffsetDateTime read(ByteBuffer buf) {
        long epochSecond = buf.getLong();
        int nano = buf.getInt();
        int offsetSeconds = buf.getInt();
        return create(epochSecond, nano, offsetSeconds);
    }

    private static OffsetDateTime create(long epochSecond, int nano, int offsetSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, nano),
                ZoneOffset.ofTotalSeconds(offsetSeconds));
    }

    @Override
    public int write(DataOutputStream dos, OffsetDateTime value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value.toEpochSecond());
        dos.writeInt(value.getNano());
        dos.writeInt(value.getOffset().getTotalSeconds());
        return Long.BYTES + Integer.BYTES * 2;
    }
}
