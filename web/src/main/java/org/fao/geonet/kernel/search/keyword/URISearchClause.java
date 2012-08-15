package org.fao.geonet.kernel.search.keyword;

import java.util.Set;

import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Element;

/**
 * A search clause for selecting based on a provided keyowrd URI
 * 
 * @author jeichar
 */
public class URISearchClause implements SearchClause {

    private String uri;

    public URISearchClause(String uri) {
        this.uri = uri;
    }

    @Override
    public Where toWhere(Set<String> langs) {
        return Wheres.ID(this.uri);
    }

    @Override
    public void addXmlParams(Element params) {
        params.addContent(new Element(XmlParams.pUri).setText(this.uri));
    }

}
