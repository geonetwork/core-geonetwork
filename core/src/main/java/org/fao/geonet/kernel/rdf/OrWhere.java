package org.fao.geonet.kernel.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrWhere extends Where {

    private List<Where> clauses;

    public OrWhere(Where... clauses) {
        this(Arrays.asList(clauses));
    }
    public OrWhere(List<Where> newClauses) {
        this.clauses = new ArrayList<Where>(newClauses);
    }
    
    @Override
    public String getClause() {
        StringBuilder builder = new StringBuilder();
        for (Where clause : clauses) {
            if(builder.length() > 0) {
                builder.append(" OR ");
            }
            builder.append(clause.getClause());
        }
        builder.insert(0, '(');
        builder.append(')');
        return builder.toString();
    }

    @Override
    public Where or(Where other) {
        ArrayList<Where> newClauses = new ArrayList<Where>(clauses);
        if (other instanceof OrWhere) {
            OrWhere otherOr = (OrWhere) other;
            newClauses.addAll(otherOr.clauses);
        } else {
            newClauses.add(other);
        }
        return new OrWhere(newClauses);
    }
}
