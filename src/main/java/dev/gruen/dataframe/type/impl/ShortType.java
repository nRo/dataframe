package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class ShortType extends NumberType<Short> {
    public ShortType() {
        super();
    }

    public ShortType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Short> getType() {
        return Short.class;
    }

    @Override
    public Short read(DataInputStream dis) throws IOException {
        return dis.readShort();
    }

    @Override
    public Short read(ByteBuffer buf) {
        return buf.getShort();
    }

    @Override
    public Short parse(String s) {
        assertNotNull(s);
        return Short.parseShort(s);
    }

    @Override
    public void write(Writer writer, Short value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Short value) {
        assertNotNull(value);
        return Short.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Short value) throws IOException {
        assertNotNull(value);
        dos.writeShort(value);
        return Short.BYTES;
    }

}
