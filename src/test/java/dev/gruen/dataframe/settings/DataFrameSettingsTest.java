package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.StringColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class DataFrameSettingsTest {
    private static final EncodingSetting ISO = new EncodingSetting(StandardCharsets.ISO_8859_1);
    private static final EncodingSetting UTF16 = new EncodingSetting(StandardCharsets.UTF_16);

    @Test
    public void builtSettingsAreAppliedTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(ISO, ColumnMatchers.byName("a"))
                .build();
        StringColumn a = new StringColumn("a");
        StringColumn b = new StringColumn("b");
        settings.applyToColumn(a);
        settings.applyToColumn(b);

        Assertions.assertSame(ISO, a.getSettings().get(EncodingSetting.class));
        Assertions.assertNull(b.getSettings().get(EncodingSetting.class));
    }

    @Test
    public void laterSettingOfSameClassWinsTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(ISO, ColumnMatchers.all())
                .add(UTF16, ColumnMatchers.byName("a"))
                .build();
        StringColumn a = new StringColumn("a");
        StringColumn b = new StringColumn("b");
        settings.applyToColumn(a);
        settings.applyToColumn(b);

        Assertions.assertSame(UTF16, a.getSettings().get(EncodingSetting.class));
        Assertions.assertSame(ISO, b.getSettings().get(EncodingSetting.class));
    }

    @Test
    public void buildCreatesSnapshotTest() {
        DataFrameSettings.DataFrameSettingsBuilder builder = DataFrameSettings.builder()
                .add(ISO, ColumnMatchers.all());
        DataFrameSettings settings = builder.build();
        builder.add(UTF16, ColumnMatchers.all());

        Assertions.assertEquals(1, settings.size());
        StringColumn a = new StringColumn("a");
        settings.applyToColumn(a);
        Assertions.assertSame(ISO, a.getSettings().get(EncodingSetting.class));
    }

    @Test
    public void noMatchLeavesColumnUnchangedTest() {
        DataFrameSettings settings = new DataFrameSettings()
                .add(ISO, "does-not-exist");
        StringColumn a = new StringColumn("a");
        settings.applyToColumn(a);
        Assertions.assertTrue(a.getSettings().isEmpty());
    }

    @Test
    public void addByIndexAndTypeTest() {
        StringColumn a = new StringColumn("a");
        IntegerColumn b = new IntegerColumn("b");
        DataFrame.create().addColumns(a, b);
        DataFrameSettings settings = new DataFrameSettings()
                .add(ISO, 0)
                .addByColumnType(UTF16, IntegerColumn.class);
        settings.applyToColumn(a);
        settings.applyToColumn(b);

        Assertions.assertSame(ISO, a.getSettings().get(EncodingSetting.class));
        Assertions.assertSame(UTF16, b.getSettings().get(EncodingSetting.class));
    }

    @Test
    public void removeSettingTest() {
        DataFrameSettings settings = new DataFrameSettings().add(ISO, "a");
        Assertions.assertTrue(settings.remove(ISO));
        Assertions.assertFalse(settings.remove(ISO));
        Assertions.assertTrue(settings.isEmpty());
    }

    @Test
    public void columnSettingsRemoveTest() {
        ColumnSettings settings = new ColumnSettings();
        settings.add(ISO);
        Assertions.assertNull(settings.remove(UTF16));
        Assertions.assertSame(ISO, settings.remove(ISO));
        Assertions.assertTrue(settings.isEmpty());
        settings.add(ISO);
        Assertions.assertSame(ISO, settings.remove(EncodingSetting.class));
        Assertions.assertNull(settings.remove(EncodingSetting.class));
    }

    @Test
    public void copyKeepsSettingsTest() {
        StringColumn a = new StringColumn("a");
        a.getSettings().add(ISO);
        Assertions.assertSame(ISO, a.copy().getSettings().get(EncodingSetting.class));
        Assertions.assertSame(ISO, a.copyEmpty().getSettings().get(EncodingSetting.class));
        Assertions.assertNotSame(a.getSettings(), a.copy().getSettings());
    }

    @Test
    public void stringTypeUsesEncodingSettingTest() throws Exception {
        StringColumn a = new StringColumn("a");
        a.getSettings().add(ISO);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        a.getValueType().write(new java.io.DataOutputStream(bos), "\u00e4");
        byte[] bytes = bos.toByteArray();
        // length prefix (4 bytes) + a single ISO-8859-1 byte
        Assertions.assertEquals(5, bytes.length);

        String read = a.getValueType().read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes)));
        Assertions.assertEquals("\u00e4", read);

        StringColumn utf8 = new StringColumn("b");
        bos = new java.io.ByteArrayOutputStream();
        utf8.getValueType().write(new java.io.DataOutputStream(bos), "\u00e4");
        Assertions.assertEquals(6, bos.toByteArray().length);
    }
}
