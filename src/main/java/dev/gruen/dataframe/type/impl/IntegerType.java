package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class IntegerType extends NumberType<Integer> {
    public IntegerType() {
        super();
    }

    public IntegerType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }


    @Override
    public Integer read(DataInputStream dis) throws IOException {
        return dis.readInt();
    }

    @Override
    public Integer read(ByteBuffer buf) {
        return buf.getInt();
    }

    @Override
    public Integer parse(String s) {
        assertNotNull(s);
        return Integer.parseInt(s);
    }

    @Override
    public void write(Writer writer, Integer value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Integer value) {
        assertNotNull(value);
        return Integer.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Integer value) throws IOException {
        assertNotNull(value);
        dos.writeInt(value);
        return Integer.BYTES;
    }
}
