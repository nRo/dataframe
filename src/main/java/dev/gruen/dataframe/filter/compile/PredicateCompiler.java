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

import dev.gruen.dataframe.filter.FilterPredicate;
import dev.gruen.query.compiler.QueryCompiler;
import dev.gruen.query.compiler.QueryCompilerBuilder;
import dev.gruen.query.compiler.QueryCompilerException;
import dev.gruen.query.term.TermOperator;

/**
 * Created by Alex on 18.05.2017.
 */
public class PredicateCompiler {
    private static final TermOperator LIKE_OPERATOR = new TermOperator("LIKE", "like");

    private static final QueryCompiler<FilterPredicate> COMPILER = createCompiler();

    private static QueryCompiler<FilterPredicate> createCompiler() {
        FilterPredicateQueryCreator creator = new FilterPredicateQueryCreator();
        return QueryCompilerBuilder.createDefault(FilterPredicate.class, creator)
                .withColumnValues()
                .withTermCreator(LIKE_OPERATOR, creator::like)
                .build();
    }

    public static FilterPredicate compile(String predicateString){
        predicateString = predicateString.trim();
        if(predicateString.isEmpty()){
            return FilterPredicate.empty();
        }
        try {
            return COMPILER.compile(predicateString);
        } catch (QueryCompilerException e) {
            throw new PredicateCompilerException(e.getMessage(), e);
        }
    }

}
