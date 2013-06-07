package org.fao.geonet.services.statistics;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.List;

/**
 * @author heikki doeleman
 */
public class MostSearchedResponse {
    /**
     * TODO Javadoc.
     *
     * @param maxHits
     * @param dbms
     * @param query
     * @return
     * @throws java.sql.SQLException
     */
    public Element createResponse(int maxHits, Dbms dbms, String query) throws SQLException {
        Element response = null;
        if (maxHits < 1) {
            response = dbms.select(query);
        } else {
            @SuppressWarnings("unchecked")
            List<Element> resultSet = dbms.select(query).getChildren();
            int max = maxHits < resultSet.size() ? maxHits : resultSet.size() ;
            response = new Element(Jeeves.Elem.RESPONSE);
            for (int i = 0; i < max; i++) {
                Element el = (Element)resultSet.get(i);
                response.addContent((Element)el.clone());
            }
        }
        //System.out.println("response: " + Xml.getString(response));
        return response;

    }
}