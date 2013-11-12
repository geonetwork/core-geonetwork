package org.fao.geonet.kernel.rdf;

public class WhereClause extends Where {

    private String clause;

    public WhereClause(String string) {
        this.clause = string;
    }

    @Override
    public String getClause() {
        return clause;
    }

    @Override
    public String toString() {
        return "WhereClause [clause=" + clause + "]";
    }
    
    

}
