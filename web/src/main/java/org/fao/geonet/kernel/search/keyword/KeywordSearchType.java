package org.fao.geonet.kernel.search.keyword;

import java.text.MessageFormat;

import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;

public enum KeywordSearchType {
    STARTS_WITH("{0}*"), CONTAINS("*{0}*"), MATCH("{0}");
    
    private String template;
    private KeywordSearchType(String template) {
        this.template = template;
    }
    public static KeywordSearchType parseString(String param) {
        String processed = param.toUpperCase().trim();
        if("0".equals(processed)) {
            return STARTS_WITH;
        } else if("1".equals(processed)) {
            return CONTAINS;
        } else if("2".equals(processed)) {
            return MATCH;
        } else {
            return valueOf(processed);
        }
    }

    public Where toWhere(String columnName, KeywordLabelSearchClause clause) {
        String value = MessageFormat.format(template, clause.keyword);
        if(clause.ignoreCase) {
            return Wheres.ilike(columnName, value);
        } else {
            return Wheres.like(columnName, value);
        }
    }
}