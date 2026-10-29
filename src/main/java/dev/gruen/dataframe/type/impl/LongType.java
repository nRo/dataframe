package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class LongType extends NumberType<Long> {
    public LongType() {
        super();
    }

    public LongType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Long> getType() {
        return Long.class;
    }


    @Override
    public Long read(DataInputStream dis) throws IOException {
        return dis.readLong();
    }

    @Override
    public Long read(ByteBuffer buf) {
        return buf.getLong();
    }

    @Override
    public Long parse(String s) {
        assertNotNull(s);
        return Long.parseLong(s);
    }

    @Override
    public void write(Writer writer, Long value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Long value) {
        assertNotNull(value);
        return Long.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Long value) throws IOException {
        assertNotNull(value);
        dos.writeLong(value);
        return Long.BYTES;
    }
}
