package org.fao.geonet.kernel.rdf;

import java.util.LinkedHashMap;

import org.fao.geonet.kernel.Thesaurus;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import com.google.common.base.Function;

/**
 * Return a map of column names -> column values for each row.
 * 
 * @author jeichar
 */
public class IdentityResultInterpreter extends ResultInterpreter<LinkedHashMap<String,Value>> {

    @Override
    public LinkedHashMap<String,Value> createFromRow(Thesaurus thesaurus, QueryResultsTable resultTable, int rowIndex) {
        LinkedHashMap<String, Value> map = new LinkedHashMap<String,Value>();
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            map.put(resultTable.getColumnName(i), resultTable.getValue(rowIndex, i));
        }
        return map;
    }

    public ResultInterpreter<Value> onlyColumn(final String columnName) {
        return map(new Function<LinkedHashMap<String,Value>, Value>(){

            @Override
            public Value apply(LinkedHashMap<String, Value> input) {
                return input.get(columnName);
            }
        });
    }
    
}
