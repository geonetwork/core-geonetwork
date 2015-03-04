package org.fao.geonet.kernel;

import com.google.common.collect.Maps;
import org.fao.geonet.kernel.rdf.Selectors;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.Map;

/**
 * Wraps a list of {@link org.openrdf.sesame.query.QueryResultsTable} and represents then as a single instance.
 * @author Jesse on 2/27/2015.
 */
public class AllQueryResultsTable extends QueryResultsTable {
    private static final long serialVersionUID = 1L;
    private transient Map<Thesaurus, QueryResultsTable> allResults = Maps.newHashMap();
    private int rowCount = 0;

    public AllQueryResultsTable(Map<Thesaurus, QueryResultsTable> allResults) {
        super(columnCount(allResults));
        this.allResults = allResults;

        for (QueryResultsTable allResult : allResults.values()) {
            rowCount += allResult.getRowCount();
        }
    }

    private static int columnCount(Map<Thesaurus, QueryResultsTable> allResults) {
        if (allResults.isEmpty()) {
            return 0;
        }

        return firstValue(allResults).getColumnCount();
    }

    private static QueryResultsTable firstValue(Map<Thesaurus, QueryResultsTable> allResults) {
        return allResults.values().iterator().next();
    }

    @Override
    public int getRowCount() {
        return this.rowCount;
    }

    @Override
    public String[] getColumnNames() {
        if (getColumnCount() == 0) {
            return new String[0];
        }
        return firstValue(allResults).getColumnNames();
    }

    @Override
    public String getColumnName(int column) {
        if (getColumnCount() == 0) {
            return null;
        }
        return firstValue(allResults).getColumnName(column);
    }

    @Override
    public Value getValue(int row, int column) {
        int current = 0;
        for (Map.Entry<Thesaurus, QueryResultsTable> entry : allResults.entrySet()) {
            QueryResultsTable allResult = entry.getValue();
            if (row - current < allResult.getRowCount()) {
                final Value value = allResult.getValue(row - current, column);
                if (Selectors.ID.id.equals(getColumnName(column))) {
                    final String id = value.toString();
                    return new LiteralImpl(AllThesaurus.buildKeywordUri(entry.getKey().getKey(), id));
                }
                return value;
            } else {
                current = allResult.getRowCount();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AllQueryResultsTable that = (AllQueryResultsTable) o;

        if (rowCount != that.rowCount) return false;
        if (!allResults.equals(that.allResults)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + allResults.hashCode();
        result = 31 * result + rowCount;
        return result;
    }
}
