package dev.gruen.dataframe.type.impl;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;

public class CharacterType extends ComparableType<Character> {
    public CharacterType() {
        super();
    }

    public CharacterType(ColumnSettings settings) {
        super(settings);
    }

    @Override
    public Class<Character> getType() {
        return Character.class;
    }

    @Override
    public Character read(DataInputStream dis) throws IOException {
        return dis.readChar();
    }

    @Override
    public Character read(ByteBuffer buf) {
        return buf.getChar();
    }

    @Override
    public Character parse(String s) {
        assertNotNull(s);
        return s.charAt(0);
    }

    @Override
    public void write(Writer writer, Character value) throws IOException {
        writer.write(toString(value));
    }

    @Override
    public String toString(Character value) {
        assertNotNull(value);
        return Character.toString(value);
    }

    @Override
    public int write(DataOutputStream dos, Character value) throws IOException {
        assertNotNull(value);
        dos.writeChar(value);
        return Character.BYTES;
    }
}
