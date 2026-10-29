package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Value type for {@link LocalDateTime}.
 * The binary format is the epoch day followed by the nano of day, both as <code>long</code>.
 */
public class LocalDateTimeType extends TemporalType<LocalDateTime> {
    public LocalDateTimeType() {
        super();
    }

    public LocalDateTimeType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<LocalDateTime> getType() {
        return LocalDateTime.class;
    }

    @Override
    protected DateTimeFormatter defaultFormatter() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    }

    @Override
    protected LocalDateTime fromTemporal(TemporalAccessor parsed) {
        return LocalDateTime.from(parsed);
    }

    @Override
    protected int compareValues(LocalDateTime a, LocalDateTime b) {
        return a.compareTo(b);
    }

    @Override
    public LocalDateTime read(DataInputStream dis) throws IOException {
        long epochDay = dis.readLong();
        long nanoOfDay = dis.readLong();
        return LocalDateTime.of(LocalDate.ofEpochDay(epochDay), LocalTime.ofNanoOfDay(nanoOfDay));
    }

    @Override
    public LocalDateTime read(ByteBuffer buf) {
        long epochDay = buf.getLong();
        long nanoOfDay = buf.getLong();
        return LocalDateTime.of(LocalDate.ofEpochDay(epochDay), LocalTime.ofNanoOfDay(nanoOfDay));
    }

    @Override
    public int write(DataOutputStream dos, LocalDateTime value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value.toLocalDate().toEpochDay());
        dos.writeLong(value.toLocalTime().toNanoOfDay());
        return Long.BYTES * 2;
    }
}
