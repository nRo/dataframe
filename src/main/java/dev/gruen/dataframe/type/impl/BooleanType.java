package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.text.ParseException;

public class BooleanType extends ComparableType<Boolean> {
    public BooleanType() {
        super();
    }

    public BooleanType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public Boolean read(DataInputStream dis) throws IOException {
        return dis.readBoolean();
    }

    @Override
    public Boolean read(ByteBuffer buf) {
        return buf.get() == 1;
    }

    @Override
    public Boolean parse(String s) throws ParseException {
        assertNotNull(s);
        if (!(
                "false".equals((s = s.toLowerCase()))
                        || "true".equals(s)
                        || "f".equals(s)
                        || "t".equals(s)
        )) {
            throw new ParseException(String.format("illegal boolean value: %s", s), 0);
        }
        if (s.equals("f")) {
            return false;
        }
        if ("t".equals(s)) {
            return true;
        }
        return Boolean.parseBoolean(s);
    }

    @Override
    public void write(Writer writer, Boolean value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Boolean value) {
        assertNotNull(value);
        return Boolean.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Boolean value) throws IOException {
        assertNotNull(value);
        dos.writeBoolean(value);
        return 1;
    }
}
