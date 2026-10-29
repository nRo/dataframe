package dev.gruen.dataframe.value.impl;

import dev.gruen.dataframe.settings.ColumnSettings;
import dev.gruen.dataframe.settings.TemporalFormatSetting;
import dev.gruen.dataframe.settings.TimeZoneSetting;
import dev.gruen.dataframe.type.ValueType;
import dev.gruen.dataframe.type.ValueTypeRuntimeException;
import dev.gruen.dataframe.type.impl.InstantType;
import dev.gruen.dataframe.type.impl.LocalDateTimeType;
import dev.gruen.dataframe.type.impl.LocalDateType;
import dev.gruen.dataframe.type.impl.LocalTimeType;
import dev.gruen.dataframe.type.impl.OffsetDateTimeType;
import dev.gruen.dataframe.type.impl.ZonedDateTimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TemporalValueTypeTest {
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    private static ColumnSettings settings(Object... settings) {
        ColumnSettings columnSettings = new ColumnSettings();
        for (Object setting : settings) {
            if (setting instanceof String pattern) {
                columnSettings.add(new TemporalFormatSetting(pattern));
            } else if (setting instanceof ZoneId zone) {
                columnSettings.add(new TimeZoneSetting(zone));
            }
        }
        return columnSettings;
    }

    @Test
    public void isoRoundTripTest() throws Exception {
        assertRoundTrip(new LocalDateType(), LocalDate.of(2020, 3, 1), "2020-03-01");
        assertRoundTrip(new LocalTimeType(), LocalTime.of(13, 45, 10), "13:45:10");
        assertRoundTrip(new LocalDateTimeType(), LocalDateTime.of(2020, 3, 1, 13, 45, 10), "2020-03-01T13:45:10");
        assertRoundTrip(new InstantType(), Instant.parse("2020-03-01T13:45:10Z"), "2020-03-01T13:45:10Z");
        assertRoundTrip(new OffsetDateTimeType(),
                OffsetDateTime.of(2020, 3, 1, 13, 45, 10, 0, ZoneOffset.ofHours(2)), "2020-03-01T13:45:10+02:00");
        assertRoundTrip(new ZonedDateTimeType(),
                ZonedDateTime.of(2020, 3, 1, 13, 45, 10, 0, BERLIN), "2020-03-01T13:45:10+01:00[Europe/Berlin]");
    }

    private static <T> void assertRoundTrip(ValueType<T> type, T value, String text) throws Exception {
        Assertions.assertEquals(text, type.toString(value));
        Assertions.assertEquals(value, type.parse(text));
        StringWriter writer = new StringWriter();
        type.write(writer, value);
        Assertions.assertEquals(text, writer.toString());
        Assertions.assertEquals(text, type.toStringRaw(value));
    }

    @Test
    public void fractionsAndLeapDayTest() throws Exception {
        LocalDateTimeType type = new LocalDateTimeType();
        LocalDateTime value = LocalDateTime.of(2020, 2, 29, 23, 59, 59, 123_456_789);
        Assertions.assertEquals(value, type.parse(type.toString(value)));
        Assertions.assertEquals(LocalDate.of(2020, 2, 29), new LocalDateType().parse("2020-02-29"));
        Assertions.assertEquals(LocalTime.MIDNIGHT, new LocalTimeType().parse("00:00:00"));
        Assertions.assertEquals(LocalTime.MIDNIGHT, new LocalTimeType().parse("00:00"));
    }

    @Test
    public void invalidValuesTest() {
        LocalDateType type = new LocalDateType();
        Assertions.assertThrows(ParseException.class, () -> type.parse("not a date"));
        Assertions.assertThrows(ParseException.class, () -> type.parse("2019-02-30"));
        Assertions.assertThrows(ParseException.class, () -> type.parse("2020-03-01T10:00:00"));
        Assertions.assertThrows(ParseException.class, () -> type.parse(""));
        Assertions.assertNull(type.parseOrNull("not a date"));
        Assertions.assertNull(type.parseOrNull(null));
        // a date time is not a local date and an offset is not a local date time
        Assertions.assertNull(new LocalDateTimeType().parseOrNull("2020-03-01T10:00:00+01:00"));
        Assertions.assertNull(new LocalDateTimeType().parseOrNull("2020-03-01"));
    }

    @Test
    public void customPatternTest() throws Exception {
        LocalDateType date = new LocalDateType(settings("dd.MM.yyyy"));
        Assertions.assertEquals(LocalDate.of(2020, 3, 1), date.parse("01.03.2020"));
        Assertions.assertEquals("01.03.2020", date.toString(LocalDate.of(2020, 3, 1)));
        Assertions.assertNull(date.parseOrNull("2020-03-01"));

        LocalDateTimeType dateTime = new LocalDateTimeType(settings("dd.MM.yyyy HH:mm"));
        Assertions.assertEquals(LocalDateTime.of(2020, 3, 1, 13, 5), dateTime.parse("01.03.2020 13:05"));
        Assertions.assertEquals("01.03.2020 13:05", dateTime.toString(LocalDateTime.of(2020, 3, 1, 13, 5)));

        LocalTimeType time = new LocalTimeType(settings("HH.mm"));
        Assertions.assertEquals(LocalTime.of(7, 30), time.parse("07.30"));
        Assertions.assertEquals("07.30", time.toString(LocalTime.of(7, 30)));
    }

    @Test
    public void localeTest() throws Exception {
        ColumnSettings columnSettings = new ColumnSettings();
        columnSettings.add(new TemporalFormatSetting("d. MMMM yyyy", Locale.GERMAN));
        LocalDateType type = new LocalDateType(columnSettings);
        Assertions.assertEquals(LocalDate.of(2020, 3, 1), type.parse("1. M\u00e4rz 2020"));
        Assertions.assertEquals("1. M\u00e4rz 2020", type.toString(LocalDate.of(2020, 3, 1)));
    }

    @Test
    public void settingsAddedAfterCreationAreUsedTest() throws Exception {
        ColumnSettings columnSettings = new ColumnSettings();
        LocalDateType type = new LocalDateType(columnSettings);
        Assertions.assertEquals("2020-03-01", type.toString(LocalDate.of(2020, 3, 1)));

        columnSettings.add(new TemporalFormatSetting("dd.MM.yyyy"));
        Assertions.assertEquals("01.03.2020", type.toString(LocalDate.of(2020, 3, 1)));
        Assertions.assertEquals(LocalDate.of(2020, 3, 1), type.parse("01.03.2020"));

        columnSettings.add(new TemporalFormatSetting("yyyy/MM/dd"));
        Assertions.assertEquals("2020/03/01", type.toString(LocalDate.of(2020, 3, 1)));

        columnSettings.remove(TemporalFormatSetting.class);
        Assertions.assertEquals("2020-03-01", type.toString(LocalDate.of(2020, 3, 1)));
    }

    @Test
    public void instantTimeZoneTest() throws Exception {
        InstantType utc = new InstantType(settings("yyyy-MM-dd HH:mm"));
        Assertions.assertEquals(Instant.parse("2020-03-01T12:00:00Z"), utc.parse("2020-03-01 12:00"));
        Assertions.assertEquals("2020-03-01 12:00", utc.toString(Instant.parse("2020-03-01T12:00:00Z")));

        InstantType berlin = new InstantType(settings("yyyy-MM-dd HH:mm", BERLIN));
        Assertions.assertEquals(Instant.parse("2020-03-01T11:00:00Z"), berlin.parse("2020-03-01 12:00"));
        Assertions.assertEquals("2020-03-01 12:00", berlin.toString(Instant.parse("2020-03-01T11:00:00Z")));
        Assertions.assertEquals(Instant.parse("2020-07-01T10:00:00Z"), berlin.parse("2020-07-01 12:00"));
    }

    @Test
    public void instantParsesOffsetsAndZonesTest() throws Exception {
        InstantType type = new InstantType();
        Assertions.assertEquals(Instant.parse("2020-03-01T11:00:00Z"), type.parse("2020-03-01T12:00:00+01:00"));
        Assertions.assertEquals(Instant.parse("2020-03-01T12:00:00Z"), type.parse("2020-03-01T12:00:00Z"));
    }

    @Test
    public void offsetDateTimeTimeZoneTest() throws Exception {
        OffsetDateTimeType type = new OffsetDateTimeType(settings("yyyy-MM-dd HH:mm", BERLIN));
        Assertions.assertEquals(OffsetDateTime.of(2020, 7, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)),
                type.parse("2020-07-01 12:00"));
        Assertions.assertEquals(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(1)),
                type.parse("2020-01-01 12:00"));
        Assertions.assertEquals("2020-07-01 12:00",
                type.toString(OffsetDateTime.of(2020, 7, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2))));

        OffsetDateTimeType withOffset = new OffsetDateTimeType();
        Assertions.assertEquals(OffsetDateTime.of(2020, 7, 1, 12, 0, 0, 0, ZoneOffset.ofHours(-5)),
                withOffset.parse("2020-07-01T12:00:00-05:00"));
    }

    @Test
    public void zonedDateTimeTimeZoneTest() throws Exception {
        ZonedDateTimeType type = new ZonedDateTimeType(settings("yyyy-MM-dd HH:mm", BERLIN));
        ZonedDateTime parsed = type.parse("2020-07-01 12:00");
        Assertions.assertEquals(ZonedDateTime.of(2020, 7, 1, 12, 0, 0, 0, BERLIN), parsed);
        Assertions.assertEquals(BERLIN, parsed.getZone());
        Assertions.assertEquals("2020-07-01 12:00", type.toString(parsed));

        ZonedDateTimeType iso = new ZonedDateTimeType();
        Assertions.assertEquals(ZonedDateTime.of(2020, 7, 1, 12, 0, 0, 0, BERLIN),
                iso.parse("2020-07-01T12:00:00+02:00[Europe/Berlin]"));
        Assertions.assertEquals(ZoneOffset.ofHours(2),
                iso.parse("2020-07-01T12:00:00+02:00").getZone());
    }

    @Test
    public void daylightSavingTimeTest() throws Exception {
        ZonedDateTimeType type = new ZonedDateTimeType(settings("yyyy-MM-dd HH:mm", BERLIN));
        // 02:30 does not exist on 2020-03-29 in Berlin, the time is moved forward
        ZonedDateTime gap = type.parse("2020-03-29 02:30");
        Assertions.assertEquals(LocalDateTime.of(2020, 3, 29, 3, 30), gap.toLocalDateTime());
        Assertions.assertEquals(ZoneOffset.ofHours(2), gap.getOffset());
        // 02:30 exists twice on 2020-10-25, the earlier offset is used
        ZonedDateTime overlap = type.parse("2020-10-25 02:30");
        Assertions.assertEquals(LocalDateTime.of(2020, 10, 25, 2, 30), overlap.toLocalDateTime());
        Assertions.assertEquals(ZoneOffset.ofHours(2), overlap.getOffset());

        // both instants of the overlap survive a binary round trip
        ZonedDateTime later = overlap.withLaterOffsetAtOverlap();
        Assertions.assertEquals(ZoneOffset.ofHours(1), later.getOffset());
        Assertions.assertEquals(later, binaryRoundTrip(type, later));
        Assertions.assertEquals(overlap, binaryRoundTrip(type, overlap));
    }

    @Test
    public void comparatorNullOrderTest() {
        assertNullFirstOrder(new LocalDateType(), LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2));
        assertNullFirstOrder(new LocalTimeType(), LocalTime.of(1, 0), LocalTime.of(2, 0));
        assertNullFirstOrder(new LocalDateTimeType(), LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2020, 1, 1, 0, 1));
        assertNullFirstOrder(new InstantType(), Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-01T00:00:01Z"));
        assertNullFirstOrder(new OffsetDateTimeType(),
                OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(2)),
                OffsetDateTime.of(2020, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC));
        assertNullFirstOrder(new ZonedDateTimeType(),
                ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, BERLIN),
                ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static <T> void assertNullFirstOrder(ValueType<T> type, T smaller, T bigger) {
        Assertions.assertTrue(type.compare(smaller, bigger) < 0);
        Assertions.assertTrue(type.compare(bigger, smaller) > 0);
        Assertions.assertEquals(0, type.compare(smaller, smaller));
        Assertions.assertTrue(type.compare(null, smaller) < 0);
        Assertions.assertTrue(type.compare(smaller, null) > 0);
        Assertions.assertEquals(0, type.compare(null, null));
        Assertions.assertEquals(0, type.compare(null, null));
        List<T> list = Arrays.asList(bigger, null, smaller);
        list.sort(type.getComparator());
        Assertions.assertEquals(Arrays.asList(null, smaller, bigger), list);
    }

    @Test
    public void rawConversionTest() {
        LocalDateType type = new LocalDateType();
        Assertions.assertEquals(LocalDate.of(2020, 1, 1), type.convertRaw(LocalDate.of(2020, 1, 1)));
        Assertions.assertThrows(ValueTypeRuntimeException.class, () -> type.convertRaw("2020-01-01"));
        Assertions.assertTrue(type.compareRaw(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)) < 0);
        Assertions.assertTrue(type.equalsRaw(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1)));
    }

    @Test
    public void binaryRoundTripTest() throws Exception {
        assertBinaryRoundTrip(new LocalDateType(), LocalDate.of(1969, 12, 31), LocalDate.MAX);
        assertBinaryRoundTrip(new LocalTimeType(), LocalTime.MIDNIGHT, LocalTime.MAX);
        assertBinaryRoundTrip(new LocalDateTimeType(), LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(2020, 2, 29, 23, 59, 59, 999_999_999));
        assertBinaryRoundTrip(new InstantType(), Instant.EPOCH, Instant.parse("2020-03-01T13:45:10.123456789Z"));
        assertBinaryRoundTrip(new OffsetDateTimeType(),
                OffsetDateTime.of(2020, 3, 1, 13, 45, 10, 5, ZoneOffset.ofHoursMinutes(-3, -30)),
                OffsetDateTime.of(2020, 3, 1, 13, 45, 10, 5, ZoneOffset.UTC));
        assertBinaryRoundTrip(new ZonedDateTimeType(),
                ZonedDateTime.of(2020, 3, 1, 13, 45, 10, 5, BERLIN),
                ZonedDateTime.of(2020, 3, 1, 13, 45, 10, 5, ZoneOffset.ofHours(5)));
    }

    @SafeVarargs
    private static <T> void assertBinaryRoundTrip(ValueType<T> type, T... values) throws Exception {
        for (T value : values) {
            Assertions.assertEquals(value, binaryRoundTrip(type, value));
        }
    }

    private static <T> T binaryRoundTrip(ValueType<T> type, T value) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = type.write(new DataOutputStream(bos), value);
        byte[] bytes = bos.toByteArray();
        Assertions.assertEquals(bytes.length, written);

        T fromStream = type.read(new DataInputStream(new ByteArrayInputStream(bytes)));
        T fromBuffer = type.read(ByteBuffer.wrap(bytes));
        Assertions.assertEquals(fromStream, fromBuffer);

        ByteArrayOutputStream rawBos = new ByteArrayOutputStream();
        type.writeRaw(new DataOutputStream(rawBos), value);
        Assertions.assertArrayEquals(bytes, rawBos.toByteArray());
        return fromStream;
    }

    @Test
    public void binaryFormatIsIndependentOfTextFormatTest() throws Exception {
        LocalDateType iso = new LocalDateType();
        LocalDateType custom = new LocalDateType(settings("dd.MM.yyyy"));
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        iso.write(new DataOutputStream(a), LocalDate.of(2020, 3, 1));
        custom.write(new DataOutputStream(b), LocalDate.of(2020, 3, 1));
        Assertions.assertArrayEquals(a.toByteArray(), b.toByteArray());
    }

    @Test
    public void unknownZoneInBinaryDataTest() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] zone = "Not/AZone".getBytes(StandardCharsets.UTF_8);
        dos.writeLong(0);
        dos.writeInt(0);
        dos.writeInt(zone.length);
        dos.write(zone);
        byte[] bytes = bos.toByteArray();

        ZonedDateTimeType type = new ZonedDateTimeType();
        ValueTypeRuntimeException e = Assertions.assertThrows(ValueTypeRuntimeException.class,
                () -> type.read(ByteBuffer.wrap(bytes)));
        Assertions.assertTrue(e.getMessage().contains("Not/AZone"));
        Assertions.assertThrows(ValueTypeRuntimeException.class,
                () -> type.read(new DataInputStream(new ByteArrayInputStream(bytes))));
    }
}
