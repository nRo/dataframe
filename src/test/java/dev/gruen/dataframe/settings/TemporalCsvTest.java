package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.column.InstantColumn;
import dev.gruen.dataframe.column.LocalDateColumn;
import dev.gruen.dataframe.column.LocalDateTimeColumn;
import dev.gruen.dataframe.column.LocalTimeColumn;
import dev.gruen.dataframe.column.OffsetDateTimeColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.column.ZonedDateTimeColumn;
import dev.gruen.dataframe.csv.CSVReaderBuilder;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.dataframe.group.DataGroup;
import dev.gruen.dataframe.group.DataGrouping;
import dev.gruen.dataframe.join.JoinedDataFrame;
import dev.gruen.dataframe.type.DataFrameTypeManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TemporalCsvTest {
    private static final String CSV = "name;created\nbob;01.03.2020 13:05\namy;15.12.2019 08:30\ncid;20.07.2021 23:59\n";

    @TempDir
    File tempDir;

    private static DataFrame loadCreated(String csv) {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("dd.MM.yyyy HH:mm"), ColumnMatchers.byName("created"))
                .build();
        return DataFrame.load(csv, CSVReaderBuilder.create()
                .withSettings(settings)
                .setColumnType("created", LocalDateTime.class)
                .build());
    }

    @Test
    public void typesAreRegisteredTest() {
        DataFrameTypeManager manager = DataFrameTypeManager.get();
        Assertions.assertEquals(LocalDateColumn.class, manager.getColumnType(LocalDate.class));
        Assertions.assertEquals(LocalTimeColumn.class, manager.getColumnType(LocalTime.class));
        Assertions.assertEquals(LocalDateTimeColumn.class, manager.getColumnType(LocalDateTime.class));
        Assertions.assertEquals(InstantColumn.class, manager.getColumnType(Instant.class));
        Assertions.assertEquals(OffsetDateTimeColumn.class, manager.getColumnType(OffsetDateTime.class));
        Assertions.assertEquals(ZonedDateTimeColumn.class, manager.getColumnType(ZonedDateTime.class));
    }

    @Test
    public void csvReadWithCustomFormatTest() {
        DataFrame df = loadCreated(CSV);
        Assertions.assertInstanceOf(LocalDateTimeColumn.class, df.getColumn("created"));
        Assertions.assertEquals(3, df.size());
        Assertions.assertEquals(LocalDateTime.of(2020, 3, 1, 13, 5), df.getLocalDateTimeColumn("created").get(0));
        Assertions.assertEquals(LocalDateTime.of(2019, 12, 15, 8, 30), df.getLocalDateTimeColumn("created").get(1));
        Assertions.assertEquals(LocalDateTime.of(2021, 7, 20, 23, 59), df.getLocalDateTimeColumn("created").get(2));
    }

    @Test
    public void csvWriteUsesColumnFormatTest() throws Exception {
        DataFrame df = loadCreated(CSV);
        File file = new File(tempDir, "out.csv");
        df.writeCSV(file, ';', true);
        String written = Files.readString(file.toPath());
        Assertions.assertTrue(written.contains("01.03.2020 13:05"), written);
        Assertions.assertTrue(written.contains("15.12.2019 08:30"), written);
        Assertions.assertFalse(written.contains("2020-03-01"), written);

        // reading the written file with the same settings gives the same values
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("dd.MM.yyyy HH:mm"), ColumnMatchers.byName("created"))
                .build();
        DataFrame reloaded = DataFrame.load(written, CSVReaderBuilder.create()
                .withSettings(settings)
                .setColumnType("created", LocalDateTime.class)
                .build());
        Assertions.assertArrayEquals(df.getLocalDateTimeColumn("created").toArray(),
                reloaded.getLocalDateTimeColumn("created").toArray());
    }

    @Test
    public void csvWriteIsoWithoutSettingsTest() throws Exception {
        DataFrame df = DataFrame.create().addLocalDateColumn("d");
        df.append(LocalDate.of(2020, 3, 1));
        File file = new File(tempDir, "iso.csv");
        df.writeCSV(file, ';', true);
        Assertions.assertTrue(Files.readString(file.toPath()).contains("2020-03-01"));
    }

    @Test
    public void naValuesTest() {
        DataFrame df = loadCreated("name;created\nbob;01.03.2020 13:05\namy;\ncid;NA\n");
        Assertions.assertEquals(3, df.size());
        Assertions.assertFalse(df.getColumn("created").isNA(0));
        Assertions.assertTrue(df.getColumn("created").isNA(1));
        Assertions.assertTrue(df.getColumn("created").isNA(2));
    }

    @Test
    public void filterTest() {
        DataFrame df = loadCreated(CSV);
        df.filter("created > '01.01.2020 00:00'");
        Assertions.assertEquals(2, df.size());

        df = loadCreated(CSV);
        df.filter(FilterPredicate.lt("created", LocalDateTime.of(2020, 1, 1, 0, 0)));
        Assertions.assertEquals(1, df.size());
        Assertions.assertEquals("amy", df.getRow(0).getString("name"));
    }

    @Test
    public void isoFilterTest() {
        DataFrame df = DataFrame.create().addLocalDateColumn("d").addStringColumn("s");
        df.append(LocalDate.of(2019, 5, 1), "a");
        df.append(LocalDate.of(2020, 5, 1), "b");
        df.append(LocalDate.of(2021, 5, 1), "c");
        df.filter("d >= '2020-01-01'");
        Assertions.assertEquals(2, df.size());
        Assertions.assertEquals("b", df.getRow(0).getString("s"));
    }

    @Test
    public void sortTest() {
        DataFrame df = loadCreated(CSV);
        df.sort("created");
        Assertions.assertEquals("amy", df.getRow(0).getString("name"));
        Assertions.assertEquals("bob", df.getRow(1).getString("name"));
        Assertions.assertEquals("cid", df.getRow(2).getString("name"));
    }

    @Test
    public void sortWithNaTest() {
        DataFrame df = DataFrame.create().addLocalDateColumn("d");
        df.append(LocalDate.of(2020, 1, 2));
        df.append((Object) null);
        df.append(LocalDate.of(2020, 1, 1));
        df.sort("d");
        // NA values are sorted last
        Assertions.assertEquals(LocalDate.of(2020, 1, 1), df.getLocalDateColumn("d").get(0));
        Assertions.assertEquals(LocalDate.of(2020, 1, 2), df.getLocalDateColumn("d").get(1));
        Assertions.assertTrue(df.getColumn("d").isNA(2));
    }

    @Test
    public void printUsesColumnFormatTest() {
        DataFrame df = DataFrame.create().addLocalDateColumn("d");
        df.append(LocalDate.of(2020, 3, 1));
        StringBuilder iso = new StringBuilder();
        df.print(iso);
        Assertions.assertTrue(iso.toString().contains("2020-03-01"), iso.toString());

        // settings added after the column was created are used as well
        df.getColumn("d").getSettings().add(new TemporalFormatSetting("dd.MM.yyyy"));
        StringBuilder printed = new StringBuilder();
        df.print(printed);
        Assertions.assertTrue(printed.toString().contains("01.03.2020"), printed.toString());
        Assertions.assertFalse(printed.toString().contains("2020-03-01"), printed.toString());
    }

    @Test
    public void groupByTest() {
        DataFrame df = DataFrame.create().addLocalDateColumn("d").addIntegerColumn("v");
        df.append(LocalDate.of(2020, 1, 1), 1);
        df.append(LocalDate.of(2020, 1, 2), 2);
        df.append(LocalDate.of(2020, 1, 1), 3);
        DataGrouping groups = df.groupBy("d").agg("count", DataGroup::size);
        Assertions.assertEquals(2, groups.size());
        Assertions.assertEquals((Integer) 2, groups.findByGroupValues(LocalDate.of(2020, 1, 1)).getInteger("count"));
        Assertions.assertEquals((Integer) 1, groups.findByGroupValues(LocalDate.of(2020, 1, 2)).getInteger("count"));
    }

    @Test
    public void joinTest() {
        DataFrame a = DataFrame.create().addLocalDateColumn("d").addStringColumn("a");
        a.append(LocalDate.of(2020, 1, 1), "x");
        a.append(LocalDate.of(2020, 1, 2), "y");
        DataFrame b = DataFrame.create().addLocalDateColumn("d").addStringColumn("b");
        b.append(LocalDate.of(2020, 1, 2), "z");
        b.append(LocalDate.of(2020, 1, 3), "w");
        JoinedDataFrame joined = a.joinInner(b, "d");
        Assertions.assertEquals(1, joined.size());
        Assertions.assertEquals(LocalDate.of(2020, 1, 2), joined.getRow(0).get("d"));
        Assertions.assertEquals("y", joined.getRow(0).get("a"));
        Assertions.assertEquals("z", joined.getRow(0).get("b"));
    }

    @Test
    public void copyKeepsFormatTest() {
        DataFrame df = loadCreated(CSV);
        LocalDateTimeColumn column = df.getLocalDateTimeColumn("created");
        Assertions.assertEquals("01.03.2020 13:05", column.copy().getValueType().toString(column.get(0)));
        Assertions.assertEquals("01.03.2020 13:05", column.copyEmpty().getValueType().toString(column.get(0)));
        DataFrame copy = df.copy();
        Assertions.assertEquals("01.03.2020 13:05",
                copy.getLocalDateTimeColumn("created").getValueType().toString(column.get(0)));
    }

    @Test
    public void rowBinaryWriteTest() throws Exception {
        DataFrame df = DataFrame.create()
                .addLocalDateColumn("date")
                .addLocalTimeColumn("time")
                .addLocalDateTimeColumn("dateTime")
                .addInstantColumn("instant")
                .addOffsetDateTimeColumn("offset")
                .addZonedDateTimeColumn("zoned");
        Object[] values = {
                LocalDate.of(2020, 2, 29),
                LocalTime.of(23, 59, 59, 999),
                LocalDateTime.of(2020, 2, 29, 23, 59, 59),
                Instant.parse("2020-02-29T23:59:59.5Z"),
                OffsetDateTime.of(2020, 2, 29, 23, 59, 59, 0, ZoneOffset.ofHours(-4)),
                ZonedDateTime.of(2020, 2, 29, 23, 59, 59, 0, ZoneId.of("America/New_York"))
        };
        df.append(values);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        for (int i = 0; i < values.length; i++) {
            df.getRow(0).write(dos, i);
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
        for (int i = 0; i < values.length; i++) {
            Assertions.assertEquals(values[i], df.getRow(0).getType(i).read(dis));
        }
        Assertions.assertEquals(0, dis.available());
    }

    @Test
    public void dataFrameApiTest() {
        DataFrame df = DataFrame.create()
                .addLocalDateColumn("a")
                .addLocalTimeColumn("b")
                .addLocalDateTimeColumn("c")
                .addInstantColumn("d")
                .addOffsetDateTimeColumn("e")
                .addZonedDateTimeColumn("f")
                .addStringColumn("g");
        Assertions.assertNotNull(df.getLocalDateColumn("a"));
        Assertions.assertNotNull(df.getLocalTimeColumn("b"));
        Assertions.assertNotNull(df.getLocalDateTimeColumn("c"));
        Assertions.assertNotNull(df.getInstantColumn("d"));
        Assertions.assertNotNull(df.getOffsetDateTimeColumn("e"));
        Assertions.assertNotNull(df.getZonedDateTimeColumn("f"));
        Assertions.assertInstanceOf(StringColumn.class, df.getColumn("g"));
        Assertions.assertThrows(RuntimeException.class, () -> df.getLocalDateColumn("b"));
    }

    @Test
    public void appendWrongTypeTest() {
        DataFrame df = DataFrame.create().addLocalDateColumn("a");
        Assertions.assertThrows(RuntimeException.class, () -> df.append("2020-01-01"));
    }
}
