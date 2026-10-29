package dev.gruen.dataframe.type;

import dev.gruen.dataframe.settings.ColumnSettings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Comparator;

public abstract class ValueType<T> {
    private final ColumnSettings settings;

    protected ValueType() {
        this(new ColumnSettings());
    }

    /**
     * Creates a value type that reads its configuration from the settings of a column.
     *
     * @param settings column settings
     */
    protected ValueType(ColumnSettings settings) {
        this.settings = settings == null ? new ColumnSettings() : settings;
    }

    public ColumnSettings getSettings() {
        return settings;
    }

    public abstract Class<T> getType();

    public abstract Comparator<T> getComparator();

    public abstract T read(DataInputStream dis) throws IOException;

    public abstract T read(ByteBuffer buf);

    public abstract T parse(String s) throws ParseException;

    public T parseOrNull(String s) {
        try {
            return parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public void writeRaw(Writer writer, Object value) throws IOException {
        write(writer, convertRaw(value));
    }

    public abstract void write(Writer writer, T value) throws IOException;

    public String toStringRaw(Object value) {
        return toString(convertRaw(value));
    }

    public abstract String toString(T value);


    public void writeRaw(DataOutputStream dos, Object value) throws IOException {
        write(dos, convertRaw(value));
    }

    public abstract int write(DataOutputStream dos, T value) throws IOException;

    public int compareRaw(Object a, Object b) {
        return compare(convertRaw(a), convertRaw(b));
    }

    public boolean equalsRaw(Object a, Object b) {
        return equals(convertRaw(a), convertRaw(b));
    }

    public T convertRaw(Object o) {
        assertNotNull(o);
        if (getType().isInstance(o)) {
            return getType().cast(o);
        }
        throw new ValueTypeRuntimeException(
                String.format("error casting raw value of type %s to %s",
                        o.getClass(), getType()));
    }

    public int compare(T a, T b) {
        return getComparator().compare(a, b);
    }

    public boolean equals(T a, T b) {
        return a.equals(b);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(getClass().isAssignableFrom(obj.getClass()))) {
            return false;
        }
        ValueType<?> other = (ValueType<?>) obj;
        return getType().equals(other.getType());
    }

    @Override
    public int hashCode() {
        return getType().hashCode();
    }

    public static void assertNotNull(Object v) {
        if (v == null) {
            throw new IllegalArgumentException("null value is not allowed");
        }
    }
}
