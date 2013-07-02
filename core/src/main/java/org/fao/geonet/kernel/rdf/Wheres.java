package org.fao.geonet.kernel.rdf;

public abstract class Wheres {
    /**
     * Where builder methods
     */
    public static final Where ALL = new Where(){
        @Override
        public String getClause() {
            return "";
        }
        
        @Override
        public Where or(Where other) {
            return this;
        }
    };
    /**
     * Where builder methods
     */
    public static final Where NONE = new Where(){
        @Override
        public String getClause() {
            return "";
        }
        
        @Override
        public Where or(Where other) {
            return other;
        }
    };

    /**
     * Create a where clause that tries to match the id column to the provided id
     * @param id the id to find
     * @return a Where object that tries to match the id column to the provided id
     */
    public static Where ID(String id) {return like("id", id);}
    
    /**
     * Create a Like clause
     * 
     * @param columnName the column or function to match against
     * @param value the value to match
     * 
     * @return a Like clause
     */
    public static Where like(String columnName, String value) {
        return new WhereClause(columnName+" LIKE \""+value+"\"");
    }
    
    /**
     * Create a Like clause that ignores case for matching
     * 
     * @param columnName the column or function to match against
     * @param value the value to match
     * 
     * @return Create a Like clause that ignores case for matching
     */
    public static Where ilike(String columnName, String value) {
        return new WhereClause(columnName+" LIKE \""+value+"\" IGNORE CASE");
    }
}
