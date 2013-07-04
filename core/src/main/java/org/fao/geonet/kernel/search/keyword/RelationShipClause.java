package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Where;
import org.jdom.Element;

public class RelationShipClause implements SearchClause {

    private KeywordRelation relation;
    private String id;
    private KeywordSearchType searchType;
    private boolean ignoreCase;

    public RelationShipClause(KeywordRelation relation, String relatedId, KeywordSearchType searchType, boolean ignoreCase) {
        this.relation = relation;
        this.id = relatedId;
        this.searchType = searchType;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public Where toWhere(Set<String> langs) {
        return searchType.toWhere(relation.name, id, ignoreCase);
    }

    @Override
    public void addXmlParams(Element params) {
        throw new UnsupportedOperationException();
    }

}
