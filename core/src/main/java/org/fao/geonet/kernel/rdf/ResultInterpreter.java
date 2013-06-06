package org.fao.geonet.kernel.rdf;

import org.fao.geonet.kernel.Thesaurus;
import org.openrdf.sesame.query.QueryResultsTable;

import com.google.common.base.Function;

/**
 * A strategy for interpreting the results of a query.  When a query is executed each row is processed by the Result Interpreter
 *  
 * @author jeichar
 *
 * @param <T> the type of results returned for each row
 */
public abstract class ResultInterpreter<T> {
    public abstract T createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex);

    public <R> ResultInterpreter<R> map(final Function<T, R> function) {
        final ResultInterpreter<T> outer = this;
        return new ResultInterpreter<R>() {
            @Override
            public R createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex) {
                return function.apply(outer.createFromRow(thesaurus, resultTable, rowIndex));
            }
        };
    }
}
