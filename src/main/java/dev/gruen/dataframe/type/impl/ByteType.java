package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class ByteType extends NumberType<Byte> {
    public ByteType() {
        super();
    }

    public ByteType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Byte> getType() {
        return Byte.class;
    }


    @Override
    public Byte read(DataInputStream dis) throws IOException {
        dis.readByte();
        return dis.readByte();
    }

    @Override
    public Byte read(ByteBuffer buf) {
        return buf.get();
    }

    @Override
    public Byte parse(String s) {
        assertNotNull(s);
        return Byte.parseByte(s);
    }

    @Override
    public void write(Writer writer, Byte value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Byte value) {
        assertNotNull(value);
        return Byte.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Byte value) throws IOException {
        assertNotNull(value);
        dos.writeByte(value);
        return 1;
    }
}
