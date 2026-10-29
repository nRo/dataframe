package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;
import dev.gruen.dataframe.settings.EncodingSetting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StringType extends ComparableType<String> {
    public StringType() {
        super();
    }

    public StringType(ColumnSettings settings) {
        super(settings);
    }

    private Charset charSet() {
        return getSettings().getOrDefault(EncodingSetting.class, EncodingSetting.UTF8).getCharset();
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }


    @Override
    public String read(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return new String(data, charSet());
    }

    @Override
    public String read(ByteBuffer buf) {
        int length = buf.getInt();
        byte[] data = new byte[length];
        buf.get(data);
        return new String(data, charSet());
    }

    @Override
    public String convertRaw(Object o) {
        return super.convertRaw(o == null ? null : String.valueOf(o));
    }

    @Override
    public String parse(String s) {
        assertNotNull(s);
        return s;
    }

    @Override
    public void write(Writer writer, String value) throws IOException {
        assertNotNull(value);
        writer.write(value);
    }

    @Override
    public String toString(String value) {
        assertNotNull(value);
        return value;
    }

    @Override
    public int write(DataOutputStream dos, String value) throws IOException {
        assertNotNull(value);
        byte[] data = value.getBytes(charSet());
        dos.writeInt(data.length);
        dos.write(data);
        return Integer.BYTES + data.length;
    }
}
