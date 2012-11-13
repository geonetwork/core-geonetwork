package org.fao.geonet.kernel.search.keyword;

/**
 * Keywords can be related by ID.  This class represents that relation and has common types as static constants
 *  
 * @author jeichar
 */
public enum KeywordRelation {

    RELATED("related") {
        @Override
        public KeywordRelation opposite() {
            return this;
        }
    },
    NARROWER("narrower") {

        @Override
        public KeywordRelation opposite() {
            return BROADER;
        }
        
    },
    BROADER("broader") {

        @Override
        public KeywordRelation opposite() {
            return NARROWER;
        }
        
    };
    
    public final String name;

    private KeywordRelation(String name) {
        this.name = name;
    }
    
    public abstract KeywordRelation opposite(); 
    
    @Override
    public String toString() {
        return name;
    }
    
}
