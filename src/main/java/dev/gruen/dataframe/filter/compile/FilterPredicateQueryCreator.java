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

package dev.gruen.dataframe.filter.compile;

import dev.gruen.dataframe.Values;
import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.query.QueryCreator;
import dev.gruen.query.QueryNode;
import dev.gruen.query.term.Field;
import dev.gruen.query.term.Value;

import java.util.List;

/**
 * Creates {@link FilterPredicate}s from the terms parsed by the query compiler library.
 */
public class FilterPredicateQueryCreator implements QueryCreator<FilterPredicate> {

    @Override
    public FilterPredicate eq(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.eqColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.eq(columnName(field), convert(value));
    }

    @Override
    public FilterPredicate ne(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.neColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.ne(columnName(field), convert(value));
    }

    @Override
    public FilterPredicate lt(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.ltColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.lt(columnName(field), convert(value));
    }

    @Override
    public FilterPredicate le(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.leColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.le(columnName(field), convert(value));
    }

    @Override
    public FilterPredicate gt(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.gtColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.gt(columnName(field), convert(value));
    }

    @Override
    public FilterPredicate ge(QueryNode node, Field field, Value value) {
        if (value.isField()) {
            return FilterPredicate.geColumn(columnName(field), columnName(value.getField()));
        }
        return FilterPredicate.ge(columnName(field), convert(value));
    }

    /**
     * Creates a <code>LIKE</code> predicate. Registered as custom term operator in {@link PredicateCompiler}.
     *
     * @param node  query node
     * @param field column
     * @param value like pattern
     * @return like predicate
     */
    public FilterPredicate like(QueryNode node, Field field, Value value) {
        if (!value.isString()) {
            throw new PredicateCompilerException(String.format("LIKE requires a text value but was '%s'", value));
        }
        return FilterPredicate.like(columnName(field), value.getString());
    }

    @Override
    public FilterPredicate regex(QueryNode node, Field field, Value value) {
        if (!value.isPattern()) {
            throw new PredicateCompilerException(String.format("wrong pattern format: %s", value));
        }
        return FilterPredicate.matches(columnName(field), value.getPattern());
    }

    @Override
    public FilterPredicate text(QueryNode node, Field field, Value value) {
        return FilterPredicate.like(columnName(field), "%" + value.getString() + "%");
    }

    @Override
    public FilterPredicate in(QueryNode node, Field field, Value value) {
        Object[] values = convertList(value);
        String column = columnName(field);
        FilterPredicate[] predicates = new FilterPredicate[values.length];
        for (int i = 0; i < values.length; i++) {
            predicates[i] = FilterPredicate.eq(column, values[i]);
        }
        return FilterPredicate.or(predicates);
    }

    @Override
    public FilterPredicate notIn(QueryNode node, Field field, Value value) {
        return FilterPredicate.not(in(node, field, value));
    }

    @Override
    public FilterPredicate not(QueryNode node, FilterPredicate predicate) {
        return FilterPredicate.not(predicate);
    }

    @Override
    public FilterPredicate and(QueryNode node, FilterPredicate[] predicates) {
        return FilterPredicate.and(predicates);
    }

    @Override
    public FilterPredicate or(QueryNode node, FilterPredicate[] predicates) {
        return FilterPredicate.or(predicates);
    }

    @Override
    public FilterPredicate xor(QueryNode node, FilterPredicate[] predicates) {
        FilterPredicate result = predicates[0];
        for (int i = 1; i < predicates.length; i++) {
            result = FilterPredicate.xor(result, predicates[i]);
        }
        return result;
    }

    @Override
    public FilterPredicate nor(QueryNode node, FilterPredicate[] predicates) {
        FilterPredicate result = predicates[0];
        for (int i = 1; i < predicates.length; i++) {
            result = FilterPredicate.nor(result, predicates[i]);
        }
        return result;
    }

    @Override
    public FilterPredicate fullSearch(Value value) {
        throw new PredicateCompilerException(String.format(
                "full-text search is not supported: '%s'. Use 'column == true' for boolean columns", value));
    }

    @Override
    public FilterPredicate empty() {
        return FilterPredicate.empty();
    }

    private static Object convert(Value value) {
        if (value.isNull()) {
            return Values.NA;
        }
        return value.getValue();
    }

    private static Object[] convertList(Value value) {
        if (!value.isList()) {
            return new Object[]{convert(value)};
        }
        List<Value> values = value.getList();
        Object[] result = new Object[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = convert(values.get(i));
        }
        return result;
    }

    static String columnName(Field field) {
        String name = field.getFullPath();
        if (name.startsWith(".")) {
            name = name.substring(1);
        }
        if (name.length() >= 2) {
            char first = name.charAt(0);
            if ((first == '\'' || first == '"') && name.charAt(name.length() - 1) == first) {
                return name.substring(1, name.length() - 1);
            }
        }
        return name;
    }
}
