# Java DataFrame
An easy-to-use DataFrame Library for Java.


[![CI](https://github.com/nRo/dataframe/actions/workflows/ci.yml/badge.svg)](https://github.com/nRo/dataframe/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/nRo/dataframe/branch/master/graph/badge.svg)](https://codecov.io/gh/nRo/dataframe)
[![Maven Central](https://img.shields.io/maven-central/v/dev.gruen/dataframe.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/dev.gruen/dataframe)
[![Javadocs](https://javadoc.io/badge2/dev.gruen/dataframe/javadoc.svg)](https://javadoc.io/doc/dev.gruen/dataframe)
![Java 21+](https://img.shields.io/badge/Java-21%2B-blue)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.txt)

Requirements
-------
Java 21 or newer. The library is built and tested on JDK 21 and JDK 25.

If you need to run on Java 8 to 17, use the last 0.7.x release (see [CHANGELOG.md](CHANGELOG.md)).

Install
-------

Add this to your `pom.xml`:

```xml
<dependencies>
...
    <dependency>
        <groupId>dev.gruen</groupId>
        <artifactId>dataframe</artifactId>
        <version>0.8.1</version>
    </dependency>
...
</dependencies>
```

The latest published version is shown in the Maven Central badge above. The `0.8.1` release has to be published first
(see [Release](#release)); until then build the snapshot from sources.

Build
-----
To build the library from sources you need JDK 21+ and Maven 3.9+:

1) Clone github repository

    $ git clone https://github.com/nRo/dataframe.git

2) Change to the created folder and run `mvn verify` (compile, tests, coverage gate and SpotBugs).
   Use `mvn install` to install the library into your local repository.

    $ cd DataFrame
    
    $ mvn install

3) Include it by adding the following to your project's `pom.xml`:

```xml
<dependencies>
...
    <dependency>
        <groupId>dev.gruen</groupId>
        <artifactId>dataframe</artifactId>
        <version>0.8.1-SNAPSHOT</version>
    </dependency>
...
</dependencies>
```

Release
-----
Releases are published to Maven Central with the `release` profile (`mvn -Prelease deploy`), which attaches the
sources and javadoc jars, signs the artifacts with GPG and uploads them using the
[central-publishing-maven-plugin](https://central.sonatype.org/publish/publish-portal-maven/).
The GitHub Actions workflow `release.yml` runs this for tags named `v*` and needs the secrets
`CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE`.

Changelog
-----
See [CHANGELOG.md](CHANGELOG.md) for the list of changes and breaking changes of each version
(including the migration from 0.7 to 0.8: new `groupId` and package `dev.gruen`, Java 21, UTF-8 default).

Examples
-----
All examples are executed by `ReadmeExamplesTest` using the files in `src/test/resources`.

Select all users called Meier or Schmitt from Germany, group by age and add column that contains the number of users with the respective age.
Then sort by age and print
```java
URL csvUrl = new URL("https://raw.githubusercontent.com/nRo/dataframe/master/src/test/resources/users.csv");

DataFrame users = DataFrame.load(csvUrl, FileFormat.CSV);

users.select("(name == 'Schmitt' || name == 'Meier') && country == 'Germany'")
        .groupBy("age").agg("count",Aggregate.count())
        .sort("age")
        .print();

/*
    age count
    20   1
    24   2
    30   2
 */

```
Load a csv file, set a unique column as primary key and add an index for two other columns.
Select rows using the previously created index, change the values in their NAME column and join them with the original DataFrame.
```java
URL csvUrl = new URL("https://raw.githubusercontent.com/nRo/dataframe/master/src/test/resources/data_index.csv");

DataFrame dataFrame = DataFrame.load(csvUrl, FileFormat.CSV);

dataFrame.setPrimaryKey("UID");
dataFrame.addIndex("id_name_idx","ID","NAME");

DataRow row = dataFrame.selectByPrimaryKey(1);
System.out.println(row);
//1;A;1

DataFrame idxExample = dataFrame.selectByIndex("id_name_idx",3,"A");

idxExample.print();
/*
    ID	NAME	UID
    3	A	4
    3	A	8
 */
idxExample.getStringColumn("NAME").map((value -> value + "_idx_example"));
idxExample.print();
/*
    ID	NAME	UID
    3	A_idx_example	4
    3	A_idx_example	8
 */

dataFrame.joinInner(idxExample,"UID").print();
/*
    ID.A    NAME.A	UID	ID.B	NAME.B
    3   A   4	3   A_idx_example
    3   A   8	3   A_idx_example
 */

```

Usage
-----
Load DataFrame from a CSV file.
Column types will be detected automatically. (String, Double, Integer, Boolean)
```java
File file = new File("person.csv");
DataFrame users = DataFrame.fromCSV(file, ';', true);
```

Load a DataFrame with custom options and predefined column types.
```java
File file = new File("person.csv");

CSVReader csvReader = CSVReaderBuilder.create()
                .withHeader(true)
                .withHeaderPrefix("#")
                .withSeparator(';')
                .setColumnType("person_id", Integer.class)
                .setColumnType("first_name", String.class)
                .setColumnType("last_name", String.class)
                .setColumnType("age", Integer.class).build();

DataFrame users = DataFrame.load(file,csvReader);
        
System.out.println(users.getHeader());
for(DataRow row : users)
{
    System.out.println(row);
}
```
DataFrames can be written using default formats (CSV or TSV).
Additionally it is possible to set different options when writing DataFrames.
```java
dataFrame.write(file); // TSV per default

dataFrame.write(file, FileFormat.CSV);

dataFrame.writeCSV(file, ';',true); // use ';' as separator and include the header


CSVWriter csvWriter = CSVWriterBuilder.create()
                .withHeader(true)
                .withSeparator('\t')
                .useGzip(true).build();
dataFrame.write(file, csvWriter);
 ```
If a meta file is written for a DataFrame, it can simply be loaded by pointing at the DataFrame file.
The meta has the same path as the DataFrame file with '.dfm' extension
```java
File file = new File("dataFrame.csv");
dataFrame.write(file);
DataFrame loadedDataFrame = DataFrame.load(file);
```

Values within a DataFrame are accessed using DataRow objects.
If the source DataFrame changes after a DataRow object is created, the DataRow is invalidated and can no
longer be accessed.

```java

for(DataRow row : dataFrame){
    ... = row.getInteger("id");
}


DataRows rows = dataFrame.getRows();

//returns the value within the id column in the first row
rows.get(0).getInteger("id");

dataFrame.sort("name");

//The DataFrame was sorted after the DataRows were obtained.
//The first row can now differ. 
//To avoid these effects, a RuntimeException is thrown
//if a row that was created before the DataFrame is altered is accessed

rows.get(0).getInteger("id"); //throws exception

rows = dataFrame.getRows();

//rows is now valid again and rows can be accessed
rows.get(0).getInteger("id");


//DataRows can be converted to a new independent DataFrame.
//changes to the original DataFrame have no effect on the new DataFrame.
DataFrame dataFrame2 = rows.toDataFrame();

dataFrame.sort("id");

dataFrame.getRow(0).getInteger("id"); // no exception
```

DataRows can be used to change values within a DataFrame

```java
DataRows rows = dataFrame.getRows();

//sets the value in the second row in the name column to 'A'
rows.get(1).set("name","A");

//sets the value in the second row in the first column to 'A'
rows.get(1).set(0,"A");


```

Use indices for fast row access.
```java

//set the primary key of a data frame
users.setPrimaryKey("person_id");
DataRow firstUser = users.selectByPrimaryKey(1)

//add a multi-column index

users.addIndex("name-address","last_name","address");

//returns rows containing all users with the last name Smith in the Example-Street 15
DataRows user = users.selectRowsByIndex("name-address","Smith","Example-Street 15")
```
It is possible to define and use other index types.
The following example shows interval indices.
This index type requires two number columns, start and end.
The index can then be used to find rows where start and end value overlap with a region specified
by two number values. It is also possible to find rows where the region defined by start and end contains a certain value.
```java
 DataFrame dataFrame = DataFrame.create()
                .addStringColumn("name")
                .addIntegerColumn("start")
                .addIntegerColumn("end");
dataFrame.append("A",1,3);
dataFrame.append("B",2,3);
dataFrame.append("C",4,5);
dataFrame.append("D",6,7);
dataFrame.addIndex(IntervalIndex.create(dataFrame, "idx", "start", "end"));

//returns a new dataframe containing all rows where (start,end) overlaps with (1,3)
// -> A, B
DataFrame df = dataFrame.selectByIndex("idx",1,3);

//rows where (start,end) overlaps with (4,5)
// -> C
dataFrame.selectByIndex("idx",4,5);

//rows where (start,end) contains 2.5
// -> A, B
dataFrame.selectByIndex("idx",2.5);
```

Perform operations on columns.
```java
//max value of column "person_id"
users.getIntegerColumn("age").max();

DoubleColumn dc1 = ...;
DoubleColumn dc2 = ...;
//add one column to another
dc1.add(dc2);

//multiply each value with 2
dc1.multiply(2);

//Use MapFunction to convert all values in a row
dataFrame.getIntegerColumn("age").map(value -> value + 2);
```

Filter and select rows using predicates.
```java
//keep users with age between 18 and 60 (both bounds are excluded)
users.filter(FilterPredicate.btwn("age",18,60));

//find all users with age > 18 an first_name == "Max"
DataFrame foundUsers = users.select(
      FilterPredicate.and(
              FilterPredicate.gt("age",18),
              FilterPredicate.eq("first_name","Max")
));

```
Create and compile predicates from strings \
The queries are parsed by the [query-compiler](https://github.com/nRo/query-compiler) library. \
Available value comparison operations: \
```==, !=, <, <=, >, >=, ~= (regex), *= (contains), LIKE, in (..), !in (..)``` \
Available predicate operations: \
```&&, ||, NOR, XOR, !(predicate) (negates the predicate) ``` \
Operator precedence: ```AND``` binds stronger than ```XOR```, which binds stronger than ```OR``` / ```NOR```. Use brackets to group terms.

```java
//find all users that are younger than 18 or older users with first_name == "Max"
DataFrame foundUsers = users.select("(age > 18 && first_name == 'Max') OR (age < 18)");

//Boolean column filter
//Boolean columns must be compared explicitly, a single word is not a valid query
//find all users that are older than 18 or the selected column is true
DataFrame foundUsers = users.select("(age > 18) OR selected == true");

//find all users the selected column is false
DataFrame notSelected = users.select("selected == false");

//compare tow columns
//returns all rows where col1 equals col2
FilterPredicate.eqColumn("col1","col2")

//Get all users where first name does not equal the last name
//Column comparisons require '.' as prefix
DataFrame dataframe = users.select(".first_name != .last_name");


// regex filter
//find all users where the street begins with A, B or C followed by lowercase characters
DataFrame foundUsers = users.select("street ~= /[ABC][a-z]+/");
```

Sort rows by one or more columns.
```java
//sort by column "person_id" (ascending)
users.sort("person_id", SortColumn.Direction.Ascending);

//sort by "last_name" und "first_name"
users.sort(
   new SortColumn("last_name", SortColumn.Direction.Descending),
   new SortColumn("first_name", SortColumn.Direction.Descending)
);
```

Group dataframes using one or more columns.
```java

//group by "age" and "first_name"
DataGrouping grouping = users.groupBy("age","first_name");


//iterate through all found groups
for(DataRow row : grouping){
    DataGroup group = grouping.getGroup(row.getIndex());
    //print the group description (group values)
    System.out.println(group.getGroupDescription());
   
    //iterate through all rows from the respective groups
    for(DataRow groupRow : group){
            System.out.println(row);
     }
}
```
Direct access to groups in a grouping.
```java

//group by "age" and "first_name"
DataGrouping grouping = users.groupBy("age","first_name");

//Get all users that are called John and are 18 years old
DataGroup group = grouping.findByGroupValues(18, "John").getGroup();
```
It is possible to apply aggregation function to data groups.
In this example, a column "max_age" is added to the grouping DataFrame. 
This column contains the maximum value of the "age" column of the respective rows in the original DataFrame.
The resulting grouping DataFrame contains two columns: "first_name" and "max_age"
```java
DataGrouping grouping = users.groupBy("first_name").agg("max_age", Aggregate.max("age"));

//some other aggregate functions
grouping.agg("mean_age", Aggregate.mean("age"));

grouping.agg("median_age", Aggregate.median("age"));

grouping.agg("25quantile_age", Aggregate.quantile("age", 0.25));

grouping.agg("older_30_count", Aggregate.filterCount("age > 30"));

//custom aggregate function
grouping.agg("org_percentage", group -> group.size() / users.size());

```
Join two dataframes using one or more columns.
```java
//join DataFrames users and visits by columns users.person_id == visits.person_id
DataFrame visitors = users.joinLeft(visits,"person_id");

//join DataFrames users and orders by columns users.person_id == orders.customer_id
DataFrame userOrders = users.joinInner(orders,new JoinColumn("person_id","customer_id"));
```

Date and time values are stored in typed columns. The following columns are available:

| Column | Value type | Default (ISO-8601) format |
|---|---|---|
| `LocalDateColumn` | `java.time.LocalDate` | `2020-03-01` |
| `LocalTimeColumn` | `java.time.LocalTime` | `13:45:10` |
| `LocalDateTimeColumn` | `java.time.LocalDateTime` | `2020-03-01T13:45:10` |
| `InstantColumn` | `java.time.Instant` | `2020-03-01T13:45:10Z` |
| `OffsetDateTimeColumn` | `java.time.OffsetDateTime` | `2020-03-01T13:45:10+02:00` |
| `ZonedDateTimeColumn` | `java.time.ZonedDateTime` | `2020-03-01T13:45:10+01:00[Europe/Berlin]` |

Columns are added with `addLocalDateColumn(name)` (and the matching methods for the other types) and read with
`getLocalDateColumn(name)`. Sorting, grouping, joining, printing and filtering (`created > '2020-01-01'`) work like for
all other column types. Values are written to CSV and printed with the format of their column.

**Auto-detection.** If a CSV column is not declared with a type, ISO-8601 values are detected in this order:
`LocalDate`, `LocalTime`, `LocalDateTime`, `Instant` (text ending with `Z`), `OffsetDateTime` (other offsets).
`ZonedDateTime` columns are not detected automatically; declare them with `setColumnType(name, ZonedDateTime.class)`.
A column stays a `String` column if one of the sampled values can not be parsed. If a column has a format setting,
the temporal types are checked first, using that format.

**Settings per column.** The format and the time zone of a column are configured with column settings. Settings are
assigned to columns with matchers (`byName`, `byIndex`, `byValueType`, `byColumnType`, `any`, `all`, `not`).
If a column has a format setting, the format is used for detection, parsing and writing.

```java
DataFrameSettings settings = DataFrameSettings.builder()
    .add(new TemporalFormatSetting("dd.MM.yyyy HH:mm"), ColumnMatchers.byName("created"))
    .add(new TimeZoneSetting(ZoneId.of("Europe/Berlin")), ColumnMatchers.byColumnType(InstantColumn.class))
    .build();
DataFrame df = DataFrame.load(file, CSVReaderBuilder.create().withSettings(settings).build());
```

A `TimeZoneSetting` supplies the zone for text without zone information (`Instant`, `OffsetDateTime` and
`ZonedDateTime` columns). The default zone is UTC. Settings can also be changed on an existing column with
`column.getSettings().add(...)`.
