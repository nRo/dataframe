package dev.gruen.dataframe.settings;

import dev.gruen.dataframe.DataFrame;
import dev.gruen.dataframe.DataFrameColumn;
import dev.gruen.dataframe.column.DoubleColumn;
import dev.gruen.dataframe.column.IntegerColumn;
import dev.gruen.dataframe.column.StringColumn;
import dev.gruen.dataframe.csv.CSVReaderBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ReaderSettingsTest {
    private static final String CSV = "name;age;score\nbob;30;1.5\namy;25;2.5\n";

    private static final EncodingSetting NAME = new EncodingSetting(StandardCharsets.ISO_8859_1);
    private static final EncodingSetting AGE = new EncodingSetting(StandardCharsets.UTF_16);
    private static final EncodingSetting SCORE = new EncodingSetting(StandardCharsets.US_ASCII);

    private static EncodingSetting encoding(DataFrame df, String column) {
        return df.getColumn(column).getSettings().get(EncodingSetting.class);
    }

    @Test
    public void settingsAreAppliedByNameTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(NAME, ColumnMatchers.byName("name"))
                .build();
        DataFrame df = DataFrame.load(CSV, CSVReaderBuilder.create().withSettings(settings).build());

        Assertions.assertInstanceOf(StringColumn.class, df.getColumn("name"));
        Assertions.assertSame(NAME, encoding(df, "name"));
        Assertions.assertNull(encoding(df, "age"));
        Assertions.assertNull(encoding(df, "score"));
    }

    @Test
    public void settingsAreAppliedToAutoDetectedColumnsTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(AGE, ColumnMatchers.byName("age"))
                .add(SCORE, ColumnMatchers.byIndex(2))
                .build();
        DataFrame df = DataFrame.load(CSV, CSVReaderBuilder.create().withSettings(settings).build());

        DataFrameColumn<?, ?> age = df.getColumn("age");
        DataFrameColumn<?, ?> score = df.getColumn("score");
        Assertions.assertInstanceOf(IntegerColumn.class, age);
        Assertions.assertInstanceOf(DoubleColumn.class, score);
        Assertions.assertSame(AGE, encoding(df, "age"));
        Assertions.assertSame(SCORE, encoding(df, "score"));
        Assertions.assertNull(encoding(df, "name"));
    }

    @Test
    public void settingsAreAppliedByColumnTypeAfterDetectionTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(AGE, ColumnMatchers.byColumnType(IntegerColumn.class))
                .build();
        DataFrame df = DataFrame.load(CSV, CSVReaderBuilder.create().withSettings(settings).build());

        Assertions.assertInstanceOf(IntegerColumn.class, df.getColumn("age"));
        Assertions.assertSame(AGE, encoding(df, "age"));
        Assertions.assertNull(encoding(df, "score"));
    }

    @Test
    public void settingsOfReplacedColumnAreKeptTest() {
        // the setting is matched against the initial string column and has to survive the type replacement
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(NAME, ColumnMatchers.byColumnType(StringColumn.class))
                .build();
        DataFrame df = DataFrame.load(CSV, CSVReaderBuilder.create().withSettings(settings).build());

        Assertions.assertInstanceOf(IntegerColumn.class, df.getColumn("age"));
        Assertions.assertSame(NAME, encoding(df, "age"));
        Assertions.assertSame(NAME, encoding(df, "score"));
        Assertions.assertSame(NAME, encoding(df, "name"));
    }

    @Test
    public void noSettingsLeavesColumnsUnchangedTest() {
        DataFrame df = DataFrame.load(CSV, CSVReaderBuilder.create().build());
        for (String column : df.getColumnNames()) {
            Assertions.assertTrue(df.getColumn(column).getSettings().isEmpty());
        }
    }

    @Test
    public void settingsAreAvailableDuringLoadingTest() {
        DataFrameSettings settings = DataFrameSettings.builder()
                .add(NAME, ColumnMatchers.all())
                .build();
        Assertions.assertSame(settings, CSVReaderBuilder.create().withSettings(settings).build()
                .load(CSV).getDataFrameSettings());
    }
}
