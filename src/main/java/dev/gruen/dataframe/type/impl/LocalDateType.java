package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Value type for {@link LocalDate}. The binary format is the epoch day as <code>long</code>.
 */
public class LocalDateType extends TemporalType<LocalDate> {
    public LocalDateType() {
        super();
    }

    public LocalDateType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<LocalDate> getType() {
        return LocalDate.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_LOCAL_DATE;
    }

    @Override
    protected LocalDate fromTemporal(TemporalAccessor parsed) {
        return LocalDate.from(parsed);
    }

    @Override
    protected boolean isExactMatch(String text, TemporalAccessor parsed) {
        return !parsed.isSupported(ChronoField.HOUR_OF_DAY) && !parsed.isSupported(ChronoField.NANO_OF_DAY);
    }

    @Override
    protected int compareValues(LocalDate a, LocalDate b) {
        return a.compareTo(b);
    }

    @Override
    public LocalDate read(DataInputStream dis) throws IOException {
        return LocalDate.ofEpochDay(dis.readLong());
    }

    @Override
    public LocalDate read(ByteBuffer buf) {
        return LocalDate.ofEpochDay(buf.getLong());
    }

    @Override
    public int write(DataOutputStream dos, LocalDate value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value.toEpochDay());
        return Long.BYTES;
    }
}
