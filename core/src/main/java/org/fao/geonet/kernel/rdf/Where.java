package org.fao.geonet.kernel.rdf;

public abstract class Where {
    /**
     * Get the SERQL string representing the where clause
     * 
     * @return the SERQL string representing the where clause
     */
    public abstract String getClause();

    /**
     * Create a <em>new</em> where clause that is this clause and other or'd together
     * 
     * @param other another Where to or with this one
     * @return a <em>new</em> where clause that is this clause and other or'd together
     */
    public Where or(Where other) {
        return new OrWhere(this,other);
    }
    
    /**
     * Create a <em>new</em> where clause that is this clause and other or'd together
     * 
     * @param other another Where to or with this one
     * @return a <em>new</em> where clause that is this clause and other or'd together
     */
    public Where and(Where other) {
        return new AndWhere(this,other);
    }
}
