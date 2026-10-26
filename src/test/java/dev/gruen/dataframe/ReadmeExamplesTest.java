package dev.gruen.dataframe;

import dev.gruen.dataframe.csv.CSVReader;
import dev.gruen.dataframe.csv.CSVReaderBuilder;
import dev.gruen.dataframe.csv.CSVWriter;
import dev.gruen.dataframe.csv.CSVWriterBuilder;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.dataframe.filter.compile.PredicateCompilerException;
import dev.gruen.dataframe.group.DataGroup;
import dev.gruen.dataframe.group.DataGrouping;
import dev.gruen.dataframe.group.aggr.Aggregate;
import dev.gruen.dataframe.index.interval.IntervalIndex;
import dev.gruen.dataframe.io.FileFormat;
import dev.gruen.dataframe.join.JoinColumn;
import dev.gruen.dataframe.sort.SortColumn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Runs the examples from the README against the local test resources.
 * If an example in the README changes, this test must be updated accordingly.
 */
public class ReadmeExamplesTest {

    @TempDir
    File tempDir;

    private static URL resource(String name) {
        URL url = ReadmeExamplesTest.class.getClassLoader().getResource(name);
        Assertions.assertNotNull(url, name);
        return url;
    }

    private static File resourceFile(String name) throws Exception {
        return new File(resource(name).toURI());
    }

    private DataFrame users() {
        return DataFrame.load(resource("users.csv"), FileFormat.CSV);
    }

    @Test
    public void testSelectGroupSortPrint() {
        DataFrame users = users();

        DataFrame result = users.select("(name == 'Schmitt' || name == 'Meier') && country == 'Germany'")
                .groupBy("age").agg("count", Aggregate.count())
                .sort("age");

        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(20, result.getRow(0).getInteger("age"));
        Assertions.assertEquals(1, result.getRow(0).getInteger("count"));
        Assertions.assertEquals(24, result.getRow(1).getInteger("age"));
        Assertions.assertEquals(2, result.getRow(1).getInteger("count"));
        Assertions.assertEquals(30, result.getRow(2).getInteger("age"));
        Assertions.assertEquals(2, result.getRow(2).getInteger("count"));

        StringBuilder printed = new StringBuilder();
        result.print(printed);
        Assertions.assertTrue(printed.toString().contains("count"));
    }

    @Test
    public void testPrimaryKeyIndexAndJoin() {
        DataFrame dataFrame = DataFrame.load(resource("data_index.csv"), FileFormat.CSV);

        dataFrame.setPrimaryKey("UID");
        dataFrame.addIndex("id_name_idx", "ID", "NAME");

        DataRow row = dataFrame.selectByPrimaryKey(1);
        Assertions.assertEquals("A", row.getString("NAME"));

        DataFrame idxExample = dataFrame.selectByIndex("id_name_idx", 3, "A");
        Assertions.assertEquals(2, idxExample.size());
        Assertions.assertEquals(4, idxExample.getRow(0).getInteger("UID"));
        Assertions.assertEquals(8, idxExample.getRow(1).getInteger("UID"));

        idxExample.getStringColumn("NAME").map((value -> value + "_idx_example"));
        Assertions.assertEquals("A_idx_example", idxExample.getRow(0).getString("NAME"));
        Assertions.assertEquals("A_idx_example", idxExample.getRow(1).getString("NAME"));

        DataFrame joined = dataFrame.joinInner(idxExample, "UID");
        Assertions.assertEquals(2, joined.size());
    }

    @Test
    public void testLoadFromFileWithOptions() throws Exception {
        File file = resourceFile("users.csv");
        DataFrame simple = DataFrame.fromCSV(file, ';', true);
        Assertions.assertEquals(9, simple.size());

        CSVReader csvReader = CSVReaderBuilder.create()
                .withHeader(true)
                .withHeaderPrefix("")
                .withSeparator(';')
                .setColumnType("name", String.class)
                .setColumnType("age", Integer.class)
                .setColumnType("country", String.class).build();

        DataFrame users = DataFrame.load(file, csvReader);
        Assertions.assertEquals(3, users.getHeader().size());
        int count = 0;
        for (DataRow row : users) {
            Assertions.assertNotNull(row.getInteger("age"));
            count++;
        }
        Assertions.assertEquals(9, count);
    }

    @Test
    public void testWriteAndLoadWithMeta() {
        DataFrame dataFrame = users();

        File csv = new File(tempDir, "dataFrame.csv");
        dataFrame.write(csv, FileFormat.CSV);
        Assertions.assertTrue(csv.exists());

        File tsv = new File(tempDir, "dataFrame.tsv");
        dataFrame.write(tsv); // TSV per default
        Assertions.assertTrue(tsv.exists());

        File csvSemicolon = new File(tempDir, "semicolon.csv");
        dataFrame.writeCSV(csvSemicolon, ';', true);
        Assertions.assertTrue(csvSemicolon.exists());

        CSVWriter csvWriter = CSVWriterBuilder.create()
                .withHeader(true)
                .withSeparator('\t')
                .useGzip(true).build();
        File gzip = new File(tempDir, "gzip.csv.gz");
        dataFrame.write(gzip, csvWriter);

        File withMeta = new File(tempDir, "meta.csv");
        dataFrame.write(withMeta, FileFormat.CSV, true);
        DataFrame loaded = DataFrame.load(withMeta);
        Assertions.assertEquals(dataFrame.size(), loaded.size());
        Assertions.assertEquals(dataFrame.getHeader().size(), loaded.getHeader().size());
        Assertions.assertEquals(dataFrame.getRow(1).getInteger("age"), loaded.getRow(1).getInteger("age"));
    }

    @Test
    public void testDataRows() {
        DataFrame dataFrame = users();

        for (DataRow row : dataFrame) {
            Assertions.assertNotNull(row.getInteger("age"));
        }

        DataRows rows = dataFrame.getRows();
        Assertions.assertEquals(24, rows.getFirst().getInteger("age"));

        dataFrame.sort("name");

        // the DataFrame was sorted after the DataRows were obtained: accessing the old rows throws
        Assertions.assertThrows(RuntimeException.class, () -> rows.getFirst().getInteger("age"));

        DataRows validRows = dataFrame.getRows();
        validRows.getFirst().getInteger("age");

        DataFrame independent = validRows.toDataFrame();
        dataFrame.sort("age");
        Assertions.assertEquals(independent.size(), dataFrame.size());
        dataFrame.getRow(0).getInteger("age"); // no exception

        DataRows changeRows = dataFrame.getRows();
        changeRows.get(1).set("name", "A");
        Assertions.assertEquals("A", dataFrame.getRow(1).getString("name"));
        changeRows.get(1).set(0, "B");
        Assertions.assertEquals("B", dataFrame.getRow(1).getString("name"));
    }

    @Test
    public void testPrimaryKeyAndMultiColumnIndex() {
        DataFrame users = users();
        users.addIndex("name-country", "name", "country");
        DataRows found = users.selectRowsByIndex("name-country", "Schmitt", "Germany");
        Assertions.assertEquals(2, found.size());
    }

    @Test
    public void testIntervalIndex() {
        DataFrame dataFrame = DataFrame.create()
                .addStringColumn("name")
                .addIntegerColumn("start")
                .addIntegerColumn("end");
        dataFrame.append("A", 1, 3);
        dataFrame.append("B", 2, 3);
        dataFrame.append("C", 4, 5);
        dataFrame.append("D", 6, 7);
        dataFrame.addIndex(IntervalIndex.create(dataFrame, "idx", "start", "end"));

        DataFrame df = dataFrame.selectByIndex("idx", 1, 3);
        Assertions.assertEquals(2, df.size());
        Assertions.assertEquals(java.util.Set.of("A", "B"),
                java.util.Set.of(df.getRow(0).getString("name"), df.getRow(1).getString("name")));

        DataFrame c = dataFrame.selectByIndex("idx", 4, 5);
        Assertions.assertEquals(1, c.size());
        Assertions.assertEquals("C", c.getRow(0).getString("name"));

        DataFrame contains = dataFrame.selectByIndex("idx", 2.5);
        Assertions.assertEquals(2, contains.size());
    }

    @Test
    public void testColumnOperations() {
        DataFrame users = users();
        Assertions.assertEquals(45, users.getIntegerColumn("age").max());

        DataFrame dataFrame = DataFrame.create().addDoubleColumn("a").addDoubleColumn("b");
        dataFrame.append(1d, 2d);
        dataFrame.append(3d, 4d);
        DoubleColumnHolder holder = new DoubleColumnHolder(dataFrame);
        holder.dc1.add(holder.dc2);
        Assertions.assertEquals(3d, holder.dc1.get(0));
        holder.dc1.multiply(2);
        Assertions.assertEquals(6d, holder.dc1.get(0));

        users.getIntegerColumn("age").map(value -> value + 2);
        Assertions.assertEquals(26, users.getRow(0).getInteger("age"));
    }

    private static class DoubleColumnHolder {
        final dev.gruen.dataframe.column.DoubleColumn dc1;
        final dev.gruen.dataframe.column.DoubleColumn dc2;

        DoubleColumnHolder(DataFrame df) {
            dc1 = df.getDoubleColumn("a");
            dc2 = df.getDoubleColumn("b");
        }
    }

    @Test
    public void testFilterWithPredicates() {
        DataFrame users = users();
        DataFrame found = users.select(FilterPredicate.and(
                FilterPredicate.gt("age", 18),
                FilterPredicate.eq("name", "Meier")));
        Assertions.assertEquals(3, found.size());

        // btwn excludes both bounds: only the ages 20, 24 and 24 remain
        users.filter(FilterPredicate.btwn("age", 18, 30));
        Assertions.assertEquals(3, users.size());
    }

    @Test
    public void testFilterWithStrings() {
        DataFrame users = users();
        Assertions.assertEquals(4, users.select("(age > 30 && name == 'Green') OR (age < 25)").size());
        Assertions.assertEquals(6, users.select("country == 'Germany'").size());
        Assertions.assertEquals(3, users.select("name ~= /[PG][a-z]+/").size());
        Assertions.assertEquals(9, users.select(".name != .country").size());
        Assertions.assertEquals(
                users.select("age > 30").size(),
                users.select(FilterPredicate.gt("age", 30)).size());
    }

    @Test
    public void testBooleanColumnFilter() {
        DataFrame df = DataFrame.create().addStringColumn("name").addBooleanColumn("selected");
        df.append("a", true);
        df.append("b", false);
        Assertions.assertEquals(1, df.select("selected == false").size());
        Assertions.assertEquals(2, df.select("(name == 'a') OR selected == false").size());
        Assertions.assertThrows(PredicateCompilerException.class, () -> df.select("!selected"));
    }

    @Test
    public void testSort() {
        DataFrame users = users();
        users.sort("age", SortColumn.Direction.Ascending);
        Assertions.assertEquals(20, users.getRow(0).getInteger("age"));

        users.sort(
                new SortColumn("name", SortColumn.Direction.Descending),
                new SortColumn("age", SortColumn.Direction.Descending));
        Assertions.assertEquals("Schmitt", users.getRow(0).getString("name"));
        Assertions.assertEquals(30, users.getRow(0).getInteger("age"));
    }

    @Test
    public void testGrouping() {
        DataFrame users = users();
        DataGrouping grouping = users.groupBy("age", "name");

        int groups = 0;
        for (DataRow row : grouping) {
            DataGroup group = grouping.getGroup(row.getIndex());
            Assertions.assertNotNull(group.getGroupDescription());
            for (DataRow groupRow : group) {
                Assertions.assertNotNull(groupRow);
            }
            groups++;
        }
        Assertions.assertTrue(groups > 0);

        DataGroup group = grouping.findByGroupValues(30, "Schmitt").getGroup();
        Assertions.assertEquals(2, group.size());
    }

    @Test
    public void testGroupAggregation() {
        DataFrame users = users();
        DataGrouping grouping = users.groupBy("name").agg("max_age", Aggregate.max("age"));
        Assertions.assertEquals(2, grouping.getHeader().size());
        Assertions.assertEquals(30, grouping.select("name == 'Schmitt'").getRow(0).getInteger("max_age"));

        grouping.agg("mean_age", Aggregate.mean("age"));
        grouping.agg("median_age", Aggregate.median("age"));
        grouping.agg("25quantile_age", Aggregate.quantile("age", 0.25));
        grouping.agg("older_30_count", Aggregate.filterCount("age > 30"));
        grouping.agg("org_percentage", group -> group.size() / users.size());
        Assertions.assertEquals(7, grouping.getHeader().size());
    }

    @Test
    public void testJoin() throws IOException {
        File a = new File(tempDir, "visits.csv");
        Files.writeString(a.toPath(), "person_id;visits\n1;5\n2;7\n");
        File b = new File(tempDir, "orders.csv");
        Files.writeString(b.toPath(), "customer_id;item\n1;book\n1;pen\n3;lamp\n");
        File u = new File(tempDir, "persons.csv");
        Files.writeString(u.toPath(), "person_id;name\n1;Max\n2;Eva\n");

        DataFrame users = DataFrame.fromCSV(u, ';', true);
        DataFrame visits = DataFrame.fromCSV(a, ';', true);
        DataFrame orders = DataFrame.fromCSV(b, ';', true);

        DataFrame visitors = users.joinLeft(visits, "person_id");
        Assertions.assertEquals(2, visitors.size());

        DataFrame userOrders = users.joinInner(orders, new JoinColumn("person_id", "customer_id"));
        Assertions.assertEquals(2, userOrders.size());
    }
}
