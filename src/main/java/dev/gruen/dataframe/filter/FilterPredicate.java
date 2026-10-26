/*
 *
 *  * Copyright (c) 2019-2026 Alexander Grün
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package dev.gruen.dataframe.filter;

import dev.gruen.dataframe.DataFrameRuntimeException;
import dev.gruen.dataframe.common.Row;
import dev.gruen.dataframe.filter.compile.PredicateCompiler;
import dev.gruen.query.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Alex on 09.03.2016.
 */
public abstract class FilterPredicate implements Query {

    public static final FilterPredicate EMPTY_FILTER = empty();

    /**
     * Returns <code>true</code> if the row is valid for this predicate
     *
     * @param row tested row
     * @return <code>true</code> if the row is valid
     */
    public abstract boolean valid(Row<?, String> row);

    /**
     * Returns a string representation for this predicate
     *
     * @return string representation
     */

    public abstract String toString();

    /**
     * Creates an <code>empty</code> predicate. Always returns <code>true</code>.
     * @return empty predicate
     */
    public static FilterPredicate empty(){
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return true;
            }

            @Override
            public String toString() {
                return "";
            }
        };
    }

    /**
     * Creates a new <code>AND</code> predicate using this predicate and an input predicate
     *
     * @param p input predicate
     * @return <code>AND</code> predicate
     * @see #and(FilterPredicate, FilterPredicate)
     */
    public FilterPredicate and(FilterPredicate p) {
        return FilterPredicate.and(this, p);
    }


    /**
     * Creates a new <code>OR</code> predicate using this predicate and an input predicate
     *
     * @param p input predicate
     * @return <code>OR</code> predicate
     * @see #or(FilterPredicate, FilterPredicate)
     */
    public FilterPredicate or(FilterPredicate p) {
        return FilterPredicate.or(this, p);
    }

    /**
     * Creates a new <code>XOR</code> predicate using this predicate and an input predicate
     *
     * @param p input predicate
     * @return <code>XOR</code> predicate
     * @see #xor(FilterPredicate, FilterPredicate)
     */
    public FilterPredicate xor(FilterPredicate p) {
        return FilterPredicate.xor(this, p);
    }


    /**
     * Creates a new <code>NOR</code> predicate using this predicate and an input predicate
     *
     * @param p input predicate
     * @return <code>NOR</code> predicate
     * @see #xor(FilterPredicate, FilterPredicate)
     */
    public FilterPredicate nor(FilterPredicate p) {
        return FilterPredicate.nor(this, p);
    }

    /**
     * Negates this predicate
     *
     * @return negated predicate
     */
    public FilterPredicate neg() {
        return FilterPredicate.not(this);
    }


    /**
     * Returns the not predicate for a specified input predicate.
     * <code>return !filterPredicate.valid(row)</code>
     *
     * @param filterPredicate input predicate
     * @return Returns the negates result of the input predicate
     */
    public static FilterPredicate not(final FilterPredicate filterPredicate) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return !filterPredicate.valid(row);
            }

            @Override
            public String toString() {
                return "!(" + filterPredicate.toString() + ")";
            }
        };
    }


    /**
     * Compares two predicates and returns <code>true</code> if the results are not equal
     * <code>return p1.valid(row) != p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if one input predicates returns <code>true</code> and the other returns <code>false</code>
     */
    public static FilterPredicate ne(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return p1.valid(row) != p2.valid(row);
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") != (" + p2.toString() + ")";
            }
        };
    }

    /**
     * Compares two predicates and returns <code>true</code> if the results are equal
     * <code>return p1.valid(row) == p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if both input predicates return either <code>true</code> or <code>false</code>
     */
    public static FilterPredicate eq(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return p1.valid(row) == p2.valid(row);
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") == (" + p2.toString() + ")";
            }
        };
    }


    /**
     * Returns <code>true</code> if all input predicates return <code>true</code>.
     * <code>return p1.valid(row) AND p2.valid(row) AND ... AND pn.valid(row)</code>
     *
     * @param predicates input predicates
     * @return <code>true</code> if all input predicates return  <code>true</code>
     */
    public static FilterPredicate and(final FilterPredicate... predicates) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                for (FilterPredicate predicate : predicates) {
                    if (!predicate.valid(row)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < predicates.length; i++) {
                    sb.append("(").append(predicates[i]).append(")");
                    if (i < predicates.length - 1) {
                        sb.append(" AND ");
                    }
                }
                return sb.toString();
            }
        };
    }

    /**
     * Returns <code>true</code> if at least one input predicate returns <code>true</code>.
     * <code>return p1.valid(row) OR p2.valid(row) OR ... OR pn.valid(row)</code>
     *
     * @param predicates input predicates
     * @return <code>true</code> if at least one input predicate returns <code>true</code>
     */
    public static FilterPredicate or(final FilterPredicate... predicates) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                for (FilterPredicate predicate : predicates) {
                    if (predicate.valid(row)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < predicates.length; i++) {
                    sb.append("(").append(predicates[i]).append(")");
                    if (i < predicates.length - 1) {
                        sb.append(" OR ");
                    }
                }
                return sb.toString();
            }
        };
    }


    /**
     * Returns <code>true</code> if both input predicates return <code>true</code>.
     * <code>return p1.valid(row) AND p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if both input predicates return  <code>true</code>
     */
    public static FilterPredicate and(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return p1.valid(row) && p2.valid(row);
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") AND (" + p2.toString() + ")";
            }
        };
    }

    /**
     * Returns <code>true</code> if at least one input predicate returns <code>true</code>.
     * <code>return p1.valid(row) OR p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if at least one input predicate returns <code>true</code>
     */
    public static FilterPredicate or(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return p1.valid(row) || p2.valid(row);
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") OR (" + p2.toString() + ")";
            }
        };
    }

    /**
     * Returns <code>true</code> if one input predicate returns <code>true</code> and the other predicate returns <code>false</code>.
     * <code>return p1.valid(row) OR p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if one input predicate returns <code>true</code> and the other returns <code>false</code>
     */
    public static FilterPredicate xor(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                boolean p1v = p1.valid(row);
                boolean p2v = p2.valid(row);
                return (p1v && !p2v) || (p2v && !p1v);
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") XOR (" + p2.toString() + ")";
            }
        };
    }

    /**
     * Returns <code>true</code> if one input predicate returns <code>true</code> and the other predicate returns <code>false</code>.
     * <code>return !p1.valid(row) AND !p2.valid(row)</code>
     *
     * @param p1 first input predicate
     * @param p2 second input predicate
     * @return <code>true</code> if one input predicate returns <code>true</code> and the other returns <code>false</code>
     */
    public static FilterPredicate nor(final FilterPredicate p1, final FilterPredicate p2) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                boolean p1v = p1.valid(row);
                boolean p2v = p2.valid(row);
                return !p1v && !p2v;
            }

            @Override
            public String toString() {
                return "(" + p1.toString() + ") NOR (" + p2.toString() + ")";
            }
        };
    }

    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#NE not equals operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'not equals'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#NE
     */
    public static FilterPredicate ne(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.NE, value);
    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#NE not equals operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'not equals'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#NE
     */
    public static FilterPredicate neColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.NE, nameB);
    }

    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#EQ equals operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'equals'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#EQ
     */
    public static FilterPredicate eq(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.EQ, value);
    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#EQ equals operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'equals'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#EQ
     */
    public static FilterPredicate eqColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.EQ, nameB);
    }


    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#GT greater than operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'greater than'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#GT
     */
    public static FilterPredicate gt(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.GT, value);
    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#GT greater than operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'greater than'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#GT
     */
    public static FilterPredicate gtColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.GT, nameB);
    }


    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#LT lower than operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'lower than'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#LT
     */
    public static FilterPredicate lt(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.LT, value);

    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#LT lower than operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'lower than'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#LT
     */
    public static FilterPredicate ltColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.LT, nameB);
    }

    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#GE greater or equal operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'greater or equal'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#GE
     */
    public static FilterPredicate ge(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.GE, value);

    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#GE greater or equal operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'greater or equal'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#LE
     */
    public static FilterPredicate geColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.GE, nameB);
    }


    /**
     * Creates a {@link ComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#LE lower or equal operation}
     *
     * @param name  row column name
     * @param value value for comparison
     * @return <code>'lower or equal'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#LE
     */
    public static FilterPredicate le(final String name, final Object value) {
        return new ComparePredicate(name, ComparePredicate.Operation.LE, value);
    }

    /**
     * Creates a {@link ColumnComparePredicate} using {@link dev.gruen.dataframe.filter.ComparePredicate.Operation#LE lower or equal operation}
     *
     * @param nameA  first row column name
     * @param nameB  second row column name
     * @return <code>'lower or equal'</code> predicate
     * @see dev.gruen.dataframe.filter.ComparePredicate.Operation#LE
     */
    public static FilterPredicate leColumn(final String nameA, final String nameB) {
        return new ColumnComparePredicate(nameA, ComparePredicate.Operation.LE, nameB);
    }

    /**
     * Returns a predicate that checks whether the row column value is contained in an array of comparison values.
     * <p><code>comparison_values.contains(row.getValue(name))</code></p>
     *
     * @param name   row column name
     * @param values values for comparison
     * @return <code>'in'</code> predicate.
     */
    public static FilterPredicate in(final String name, final Object[] values) {
        return in(name, Arrays.asList(values));
    }

    /**
     * Returns a predicate that checks whether the row column value is contained in a collection of comparison values.
     * <p><code>comparison_values.contains(row.getValue(name))</code></p>
     *
     * @param name   row column name
     * @param values values for comparison
     * @return <code>'in'</code> predicate.
     */
    public static FilterPredicate in(final String name, final Collection<Object> values) {
        return in(name, new HashSet<>(values));
    }

    /**
     * Returns a predicate that checks whether the row column value is contained in a set of comparison values.
     * <p><code>comparison_values.contains(row.getValue(name))</code></p>
     *
     * @param name   row column name
     * @param values values for comparison
     * @return <code>'in'</code> predicate.
     */
    public static FilterPredicate in(final String name, final Set<Object> values) {
        return new FilterPredicate() {
            @Override
            public boolean valid(Row<?, String> row) {
                return values.contains(row.get(name));
            }

            @Override
            public String toString() {
                return name + " in " + values.toString();
            }
        };
    }

    /**
     * Returns a predicate that checks whether the row column value is between two comparison values.
     * <p><code>row.get(name) &gt; low AND row.get(name) &lt; high</code></p>
     *
     * @param name row column name
     * @param low  low value
     * @param high high value
     * @return <code>'between'</code> predicate.
     */
    public static FilterPredicate btwn(final String name, Object low, Object high) {
        return FilterPredicate.and(FilterPredicate.gt(name, low), FilterPredicate.lt(name, high));
    }
    /**
     * Returns a {@link LikePredicate} that checks whether the row column value matches a specified LIKE query.
     * <p><code>pattern.matches(row.get(name))</code></p>
     *
     * @param name    row column name
     * @param query LIKE query
     * @return <code>'LIKE'</code> predicate.
     */
    public static FilterPredicate like(final String name, Object query) {
        if (query == null) {
            throw new DataFrameRuntimeException("LIKE query is null");
        }
        return new LikePredicate(name, query.toString());
    }
    /**
     * Returns a {@link LikePredicate} that checks whether the row column value matches a specified LIKE query.
     * <p><code>pattern.matches(row.get(name))</code></p>
     *
     * @param name    row column name
     * @param query LIKE query
     * @return <code>'LIKE'</code> predicate.
     */
    public static FilterPredicate like(final String name, String query) {
        return new LikePredicate(name, query);
    }
    /**
     * Returns a {@link MatchPredicate} that checks whether the row column value matches a specified {@link Pattern}.
     * <p><code>pattern.matches(row.get(name))</code></p>
     *
     * @param name    row column name
     * @param pattern match pattern
     * @return <code>'matches'</code> predicate.
     */
    public static FilterPredicate matches(final String name, Pattern pattern) {
        return new MatchPredicate(name, pattern);
    }

    /**
     * Returns a {@link MatchPredicate} that checks whether the row column value matches a specified pattern string.
     *
     * @param name          row column name
     * @param patternString match pattern string
     * @return <code>'matches'</code> predicate.
     * @see #matches(String, Pattern)
     */
    public static FilterPredicate matches(final String name, String patternString) {
        return new MatchPredicate(name, patternString);
    }

    public static FilterPredicate compile(String predicateString){
        return PredicateCompiler.compile(predicateString);
    }

}
