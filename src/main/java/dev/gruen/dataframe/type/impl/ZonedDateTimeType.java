package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;
import dev.gruen.dataframe.type.ValueTypeRuntimeException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

/**
 * Value type for {@link ZonedDateTime}.
 * The binary format is the epoch second as <code>long</code>, the nano of second as <code>int</code>
 * and the zone id as length prefixed UTF-8 string.
 * <p>
 * Text without zone or offset is interpreted in the zone of the column
 * (see {@link dev.gruen.dataframe.settings.TimeZoneSetting}).
 * Values are printed in their own zone.
 */
public class ZonedDateTimeType extends TemporalType<ZonedDateTime> {
    public ZonedDateTimeType() {
        super();
    }

    public ZonedDateTimeType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<ZonedDateTime> getType() {
        return ZonedDateTime.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_ZONED_DATE_TIME;
    }

    @Override
    protected ZonedDateTime fromTemporal(TemporalAccessor parsed) {
        if (parsed.query(TemporalQueries.zone()) != null) {
            return ZonedDateTime.from(parsed);
        }
        return LocalDateTime.from(parsed).atZone(zone());
    }

    @Override
    protected int compareValues(ZonedDateTime a, ZonedDateTime b) {
        return a.compareTo(b);
    }

    @Override
    public ZonedDateTime read(DataInputStream dis) throws IOException {
        long epochSecond = dis.readLong();
        int nano = dis.readInt();
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return create(epochSecond, nano, data);
    }

    @Override
    public ZonedDateTime read(ByteBuffer buf) {
        long epochSecond = buf.getLong();
        int nano = buf.getInt();
        int length = buf.getInt();
        byte[] data = new byte[length];
        buf.get(data);
        return create(epochSecond, nano, data);
    }

    private static ZonedDateTime create(long epochSecond, int nano, byte[] zoneId) {
        String id = new String(zoneId, StandardCharsets.UTF_8);
        ZoneId zone;
        try {
            zone = ZoneId.of(id);
        } catch (DateTimeException e) {
            throw new ValueTypeRuntimeException(
                    String.format("unknown time zone '%s', the zone data of this JVM does not contain it", id), e);
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond, nano), zone);
    }

    @Override
    public int write(DataOutputStream dos, ZonedDateTime value) throws IOException {
        assertNotNull(value);
        byte[] zoneId = value.getZone().getId().getBytes(StandardCharsets.UTF_8);
        dos.writeLong(value.toEpochSecond());
        dos.writeInt(value.getNano());
        dos.writeInt(zoneId.length);
        dos.write(zoneId);
        return Long.BYTES + Integer.BYTES * 2 + zoneId.length;
    }
}
