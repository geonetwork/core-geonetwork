package org.fao.geonet.kernel.search.keyword;

/**
 * Keywords can be related by ID.  This class represents that relation and has common types as static constants
 *  
 * @author jeichar
 */
public abstract class KeywordRelation {

    public static final KeywordRelation RELATED = new KeywordRelation("related") {
        @Override
        public KeywordRelation opposite() {
            return this;
        }
    };
    public static final KeywordRelation NARROWER = new KeywordRelation("narrower") {

        @Override
        public KeywordRelation opposite() {
            return BROADER;
        }
        
    };
    public static final KeywordRelation BROADER = new KeywordRelation("broader") {

        @Override
        public KeywordRelation opposite() {
            return NARROWER;
        }
        
    };
    
    public final String name;

    public KeywordRelation(String name) {
        this.name = name;
    }
    
    public abstract KeywordRelation opposite(); 
    
    @Override
    public String toString() {
        return name;
    }
    
}
