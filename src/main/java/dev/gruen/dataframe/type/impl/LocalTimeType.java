package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Value type for {@link LocalTime}. The binary format is the nano of day as <code>long</code>.
 */
public class LocalTimeType extends TemporalType<LocalTime> {
    public LocalTimeType() {
        super();
    }

    public LocalTimeType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<LocalTime> getType() {
        return LocalTime.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_LOCAL_TIME;
    }

    @Override
    protected LocalTime fromTemporal(TemporalAccessor parsed) {
        return LocalTime.from(parsed);
    }

    @Override
    protected boolean isExactMatch(String text, TemporalAccessor parsed) {
        return !parsed.isSupported(ChronoField.EPOCH_DAY);
    }

    @Override
    protected int compareValues(LocalTime a, LocalTime b) {
        return a.compareTo(b);
    }

    @Override
    public LocalTime read(DataInputStream dis) throws IOException {
        return LocalTime.ofNanoOfDay(dis.readLong());
    }

    @Override
    public LocalTime read(ByteBuffer buf) {
        return LocalTime.ofNanoOfDay(buf.getLong());
    }

    @Override
    public int write(DataOutputStream dos, LocalTime value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value.toNanoOfDay());
        return Long.BYTES;
    }
}
