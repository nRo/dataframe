package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.DataFrameColumn;
import dev.gruen.dataframe.column.BooleanColumn;
import dev.gruen.dataframe.column.DoubleColumn;
import dev.gruen.dataframe.column.InstantColumn;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.LocalDateColumn;
import dev.gruen.dataframe.column.LocalDateTimeColumn;
import dev.gruen.dataframe.column.LocalTimeColumn;
import dev.gruen.dataframe.column.LongColumn;
import dev.gruen.dataframe.column.OffsetDateTimeColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.csv.CSVReaderBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TemporalAutoDetectTest {

    private static DataFrame load(String csv) {
        return DataFrame.load(csv, CSVReaderBuilder.create().build());
    }

    private static DataFrame load(String csv, DataFrameSettings settings) {
        return DataFrame.load(csv, CSVReaderBuilder.create().withSettings(settings).build());
    }

    private static Class<?> typeOf(DataFrame df, String column) {
        DataFrameColumn<?, ?> c = df.getColumn(column);
        return c.getClass();
    }

    @Test
    public void isoTypesAreDetectedTest() {
        DataFrame df = load("date;time;dateTime;instant;offset\n"
                + "2020-03-01;13:45:10;2020-03-01T13:45:10;2020-03-01T13:45:10Z;2020-03-01T13:45:10+02:00\n"
                + "2019-12-31;00:00:00;2019-12-31T00:00:00;2019-12-31T00:00:00Z;2019-12-31T00:00:00-05:00\n");
        Assertions.assertEquals(LocalDateColumn.class, typeOf(df, "date"));
        Assertions.assertEquals(LocalTimeColumn.class, typeOf(df, "time"));
        Assertions.assertEquals(LocalDateTimeColumn.class, typeOf(df, "dateTime"));
        Assertions.assertEquals(InstantColumn.class, typeOf(df, "instant"));
        Assertions.assertEquals(OffsetDateTimeColumn.class, typeOf(df, "offset"));

        Assertions.assertEquals(LocalDate.of(2020, 3, 1), df.getLocalDateColumn("date").get(0));
        Assertions.assertEquals(LocalTime.of(13, 45, 10), df.getLocalTimeColumn("time").get(0));
        Assertions.assertEquals(LocalDateTime.of(2020, 3, 1, 13, 45, 10), df.getLocalDateTimeColumn("dateTime").get(0));
        Assertions.assertEquals(Instant.parse("2020-03-01T13:45:10Z"), df.getInstantColumn("instant").get(0));
        Assertions.assertEquals(OffsetDateTime.of(2019, 12, 31, 0, 0, 0, 0, ZoneOffset.ofHours(-5)),
                df.getOffsetDateTimeColumn("offset").get(1));
    }

    @Test
    public void zoneDetectionOrderTest() {
        // all values end with Z -> Instant
        Assertions.assertEquals(InstantColumn.class,
                typeOf(load("v\n2020-03-01T13:45:10Z\n2020-03-02T13:45:10Z\n"), "v"));
        // an offset in one value -> OffsetDateTime
        Assertions.assertEquals(OffsetDateTimeColumn.class,
                typeOf(load("v\n2020-03-01T13:45:10Z\n2020-03-02T13:45:10+01:00\n"), "v"));
        // only offsets
        Assertions.assertEquals(OffsetDateTimeColumn.class,
                typeOf(load("v\n2020-03-01T13:45:10+00:00\n"), "v"));
    }

    @Test
    public void zonedDateTimeIsNotDetectedTest() {
        DataFrame df = load("v\n2020-03-01T13:45:10+01:00[Europe/Berlin]\n");
        Assertions.assertEquals(StringColumn.class, typeOf(df, "v"));
    }

    @Test
    public void dateTimeIsNotDetectedAsDateOrTimeTest() {
        Assertions.assertEquals(LocalDateTimeColumn.class,
                typeOf(load("v\n2020-03-01T13:45:10\n"), "v"));
        Assertions.assertEquals(LocalDateColumn.class,
                typeOf(load("v\n2020-03-01\n"), "v"));
        Assertions.assertEquals(LocalTimeColumn.class,
                typeOf(load("v\n13:45\n"), "v"));
    }

    @Test
    public void mixedColumnStaysStringTest() {
        DataFrame df = load("v\n2020-03-01\nnot a date\n2020-03-03\n");
        Assertions.assertEquals(StringColumn.class, typeOf(df, "v"));
        Assertions.assertEquals("2020-03-01", df.getRow(0).getString("v"));

        df = load("v\n2020-03-01\n2020-03-01T10:00:00\n");
        // a date and a date time mix: neither date nor date time matches all values
        Assertions.assertEquals(StringColumn.class, typeOf(df, "v"));

        df = load("v\n2020-02-30\n");
        Assertions.assertEquals(StringColumn.class, typeOf(df, "v"));
    }

    @Test
    public void naValuesTest() {
        DataFrame df = load("v;w\n2020-03-01;a\n;b\nNA;c\n2020-03-04;d\n");
        Assertions.assertEquals(LocalDateColumn.class, typeOf(df, "v"));
        Assertions.assertEquals(4, df.size());
        Assertions.assertFalse(df.getColumn("v").isNA(0));
        Assertions.assertTrue(df.getColumn("v").isNA(1));
        Assertions.assertTrue(df.getColumn("v").isNA(2));
        Assertions.assertEquals(LocalDate.of(2020, 3, 4), df.getLocalDateColumn("v").get(3));
    }

    @Test
    public void formatSettingIsUsedForDetectionTest() {
        String csv = "d;dt;t\n01.03.2020;01.03.2020 13:05;07.30\n15.12.2019;15.12.2019 08:30;08.45\n";
        // without settings nothing is detected
        DataFrame plain = load(csv);
        Assertions.assertEquals(StringColumn.class, typeOf(plain, "d"));
        Assertions.assertEquals(StringColumn.class, typeOf(plain, "dt"));

        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("dd.MM.yyyy"), ColumnMatchers.byName("d"))
                .add(new TemporalFormatSetting("dd.MM.yyyy HH:mm"), ColumnMatchers.byName("dt"))
                .add(new TemporalFormatSetting("HH.mm"), ColumnMatchers.byName("t"))
                .build();
        DataFrame df = load(csv, settings);
        Assertions.assertEquals(LocalDateColumn.class, typeOf(df, "d"));
        Assertions.assertEquals(LocalDateTimeColumn.class, typeOf(df, "dt"));
        Assertions.assertEquals(LocalTimeColumn.class, typeOf(df, "t"));
        Assertions.assertEquals(LocalDate.of(2020, 3, 1), df.getLocalDateColumn("d").get(0));
        Assertions.assertEquals(LocalDateTime.of(2019, 12, 15, 8, 30), df.getLocalDateTimeColumn("dt").get(1));
        Assertions.assertEquals(LocalTime.of(8, 45), df.getLocalTimeColumn("t").get(1));

        // the setting stays on the replaced column
        Assertions.assertNotNull(df.getColumn("d").getSettings().get(TemporalFormatSetting.class));
        Assertions.assertEquals("01.03.2020",
                df.getLocalDateColumn("d").getValueType().toString(LocalDate.of(2020, 3, 1)));
    }

    @Test
    public void dateTimePatternIsNotDetectedAsDateTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("dd.MM.yyyy HH:mm"), ColumnMatchers.byName("dt"))
                .build();
        DataFrame df = load("dt\n01.03.2020 13:05\n", settings);
        Assertions.assertEquals(LocalDateTimeColumn.class, typeOf(df, "dt"));
    }

    @Test
    public void timeZoneSettingIsUsedForDetectedColumnsTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("yyyy-MM-dd HH:mm"), ColumnMatchers.byName("i"))
                .add(new TimeZoneSetting(ZoneId.of("Europe/Berlin")), ColumnMatchers.byName("i"))
                .build();
        DataFrame df = load("i\n2020-01-01 12:00\n", settings);
        // LocalDateTime is checked before Instant, the same text is a local date time
        Assertions.assertEquals(LocalDateTimeColumn.class, typeOf(df, "i"));

        DataFrame instant = DataFrame.load("i\n2020-01-01 12:00\n", CSVReaderBuilder.create()
                .withSettings(settings).setColumnType("i", Instant.class).build());
        Assertions.assertEquals(Instant.parse("2020-01-01T11:00:00Z"), instant.getInstantColumn("i").get(0));
    }

    @Test
    public void settingByColumnTypeIsAppliedDuringDetectionTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(new TemporalFormatSetting("dd.MM.yyyy"), ColumnMatchers.byColumnType(LocalDateColumn.class))
                .build();
        DataFrame df = load("a;b\n01.03.2020;2020-03-01\n", settings);
        Assertions.assertEquals(LocalDateColumn.class, typeOf(df, "a"));
        // the setting applies to every date column: b is no date in this format
        Assertions.assertEquals(StringColumn.class, typeOf(df, "b"));
    }

    @Test
    public void existingDetectionIsUnchangedTest() {
        DataFrame df = load("b;i;l;d;s\ntrue;1;10000000000;1.5;abc\nfalse;2;20000000000;2.5;def\n");
        Assertions.assertEquals(BooleanColumn.class, typeOf(df, "b"));
        Assertions.assertEquals(IntegerColumn.class, typeOf(df, "i"));
        Assertions.assertEquals(LongColumn.class, typeOf(df, "l"));
        Assertions.assertEquals(DoubleColumn.class, typeOf(df, "d"));
        Assertions.assertEquals(StringColumn.class, typeOf(df, "s"));
        // plain numbers are not dates or times
        Assertions.assertEquals(IntegerColumn.class, typeOf(load("v\n2020\n2021\n"), "v"));
        Assertions.assertEquals(IntegerColumn.class, typeOf(load("v\n20200301\n"), "v"));
    }

    @Test
    public void explicitColumnTypeIsNotDetectedTest() {
        DataFrame df = DataFrame.load("v\n2020-03-01\n", CSVReaderBuilder.create()
                .setColumnType("v", String.class).build());
        Assertions.assertEquals(StringColumn.class, typeOf(df, "v"));
    }

    @Test
    public void filterAfterDetectionTest() {
        DataFrame df = load("d;n\n2019-05-01;a\n2020-05-01;b\n2021-05-01;c\n");
        Assertions.assertEquals(LocalDateColumn.class, typeOf(df, "d"));
        df.filter("d > '2019-12-31'");
        Assertions.assertEquals(2, df.size());
        Assertions.assertEquals("b", df.getRow(0).getString("n"));
    }
}
