# Changelog

All notable changes to this project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.8.1]

### Breaking changes
- Filter queries (`DataFrame.select(String)`, `FilterPredicate.compile(String)`) are now parsed by the [query-compiler](https://github.com/nRo/query-compiler) library
  (`dev.gruen:query-compiler`) instead of the built-in ANTLR grammar. The ANTLR grammar, the generated parser and the visitors were removed.
  Changes in the query syntax:
  - Logical operators have a precedence now: `AND` binds stronger than `XOR`, which binds stronger than `OR` / `NOR` (left-associative). Before, all logical operators were evaluated from left to right with equal precedence. Use brackets to keep the old evaluation order.
  - Boolean column flags (`flag`, `!flag`) are no longer supported. Use `flag == true` / `flag == false`.
  - A query that is a single word is parsed as full-text search, which is not supported by DataFrame, and raises a `PredicateCompilerException`.
  - Decimal numbers with a comma (`1,5`) are no longer supported because `,` separates the values of `in (..)` lists. Use `1.5`.
- CSV columns without a declared type are now also checked for ISO-8601 date and time values (`LocalDate`, `LocalTime`, `LocalDateTime`,
  `Instant`, `OffsetDateTime`) after `Boolean`, `Integer`, `Long` and `Double`. Columns that used to be loaded as `String` columns
  (e.g. `2020-03-01` or `13:45`) now get a temporal column type. Declare the column as `String` with `setColumnType(name, String.class)`
  to keep the old behaviour.
- `csv.ColumnSettings` was renamed to `csv.CSVColumnSettings` to avoid a name clash with `settings.ColumnSettings`.

### Added
- Temporal column types: `LocalDateColumn`, `LocalTimeColumn`, `LocalDateTimeColumn`, `InstantColumn`, `OffsetDateTimeColumn` and
  `ZonedDateTimeColumn` with matching value types, `add...Column` / `get...Column` methods in `DataFrame` and `DataFrameBuilder`,
  binary and CSV support and ISO-8601 auto-detection.
- Per-column settings (`dev.gruen.dataframe.settings`): `ColumnSettings`, `DataFrameSettings`, `ColumnMatchers` and the settings
  `TemporalFormatSetting` (pattern and locale), `TimeZoneSetting` and `EncodingSetting` (charset of `StringType`).
  Settings are passed with `withSettings(DataFrameSettings)` of `CSVReaderBuilder` and are kept when columns are copied.
- Filter queries support `in (1, 2)`, `!in (1, 2)` and `*=` (contains) operators.

## [0.8.0]

### Breaking changes
- The Maven `groupId` changed from `de.unknownreality` to `dev.gruen`, and the Java packages were renamed from `de.unknownreality.dataframe` to `dev.gruen.dataframe`.
- Java 21 or newer is required (`maven.compiler.release=21`). Before, Java 8 was the baseline.
- UTF-8 is the default charset of `DataReader` and `DataWriter`. Before, the platform charset was used.
  Use `withCharset(...)` on `CSVReaderBuilder` / `CSVWriterBuilder` to read or write other encodings.
- Removed deprecated `DataFrameBuilder.createFrom(DataIterator)` and `CSVIterator.toDataFrame()`.
  Use `DataFrame.load(...)`, `DataFrame.fromCSV(...)` or `DataFrame.builder().from(iterator)`.

### Added
- `DataFrame.print(PrintStream)` and `DataFrame.print(Appendable)` plus matching `DataFrameWriter.print(...)` overloads.
- `BufferedStreamIterator(InputStream, Charset)` constructor. `BufferedStreamIterator` (and `CSVIterator`) are `AutoCloseable`.
- Gzip compressed input is detected on every binary source (file, stream, url, resource, bytes).

### Fixed
- Streams and readers were not closed if creating the iterator failed (`DataReader`, `CSVIterator`, `DataFrameMetaReader`).
- gzip files were read and written with the platform charset, `CSVWriter` ignored the writer charset for gzip output.
- `CSVWriter.write(OutputStream)` with gzip did not finish the gzip stream.
- `StringType.read(ByteBuffer)` did not read the string bytes, `StringType.read(DataInputStream)` used `read` instead of `readFully`.

## [0.7.5]
- Direct value access for `DataRow` objects. `DataRow` objects are invalidated once the source `DataFrame` changes.
- Row collections are returned as `DataRows` objects, which can be converted to a new `DataFrame`.
- Improved `groupBy`.

## [0.7.0]
- The read and write functions were rewritten. Some existing methods were removed.
- Data grouping was refactored. Aggregation functions can be applied to data groupings.
- Java 8 is required.
- Empty `DataFrame` instances are created using `DataFrame.create()`.
