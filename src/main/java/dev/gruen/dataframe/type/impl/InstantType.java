package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Value type for {@link Instant}.
 * The binary format is the epoch second as <code>long</code> followed by the nano of second as <code>int</code>.
 * <p>
 * Text without zone or offset is interpreted in the zone of the column
 * (see {@link dev.gruen.dataframe.settings.TimeZoneSetting}). Custom formats are printed in that zone as well.
 */
public class InstantType extends TemporalType<Instant> {
    public InstantType() {
        super();
    }

    public InstantType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Instant> getType() {
        return Instant.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_INSTANT;
    }

    @Override
    protected Instant fromTemporal(TemporalAccessor parsed) {
        if (parsed.isSupported(ChronoField.INSTANT_SECONDS)) {
            return Instant.from(parsed);
        }
        return LocalDateTime.from(parsed).atZone(zoneOf(parsed)).toInstant();
    }

    @Override
    protected String format(Instant value) {
        DateTimeFormatter formatter = formatter();
        if (formatter.getZone() == null) {
            formatter = formatter.withZone(zone());
        }
        return formatter.format(value);
    }

    /**
     * Without a custom format only text in UTC (ending with <code>Z</code>) is detected as instant,
     * text with other offsets is detected as offset date time.
     */
    @Override
    protected boolean isExactMatch(String text, TemporalAccessor parsed) {
        return hasFormatSetting() || text.endsWith("Z");
    }

    @Override
    protected int compareValues(Instant a, Instant b) {
        return a.compareTo(b);
    }

    @Override
    public Instant read(DataInputStream dis) throws IOException {
        long epochSecond = dis.readLong();
        int nano = dis.readInt();
        return Instant.ofEpochSecond(epochSecond, nano);
    }

    @Override
    public Instant read(ByteBuffer buf) {
        long epochSecond = buf.getLong();
        int nano = buf.getInt();
        return Instant.ofEpochSecond(epochSecond, nano);
    }

    @Override
    public int write(DataOutputStream dos, Instant value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value.getEpochSecond());
        dos.writeInt(value.getNano());
        return Long.BYTES + Integer.BYTES;
    }
}
