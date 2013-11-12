package org.fao.geonet.kernel.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndWhere extends Where {

    private final List<Where> clauses = new ArrayList<Where>();

    public AndWhere(Where where1, Where where2) {
        clauses.add(where1);
        clauses.add(where2);
    }
    
    public AndWhere(Where... clauses) {
        this.clauses.addAll(Arrays.asList(clauses));
    }

    @Override
    public String getClause() {
        StringBuilder builder = new StringBuilder();
        for (Where clause : clauses) {
            if(builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append(clause.getClause());
        }
        builder.insert(0, '(');
        builder.append(')');
        return builder.toString();
    }

    @Override
    public Where and(Where other) {
        return super.and(other);
    }
}
