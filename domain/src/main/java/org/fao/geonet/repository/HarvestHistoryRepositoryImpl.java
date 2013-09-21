package org.fao.geonet.repository;

import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

/**
 * Implementation for custom methods for the HarvestHistoryRepository class.
 *
 * User: Jesse
 * Date: 9/20/13
 * Time: 4:03 PM
 */
public class HarvestHistoryRepositoryImpl {

    @PersistenceContext
    EntityManager _entityManager;

    @Nonnull
    public Element findAllAsXml() {
        return findAllAsXml(null, null);
    }

    @Nonnull
    public Element findAllAsXml(final Specification<HarvestHistory> specification) {
        return findAllAsXml(specification, null);
    }

    @Nonnull
    public Element findAllAsXml(final Sort sort) {
        return findAllAsXml(null, sort);
    }

    /**
     * This method is intended to override the findAllAsXml methods in GeonetRepositoryImpl because Harvest history needs special handling
     * of these methods.
     * <p>
     *     The major change is that this method takes the data of the "info" and "params" properties and parses then as XML and
     *     replaces the corresponding elements with the parsed XML.
     * </p>
     */
    @Nonnull
    public Element findAllAsXml(final Specification<HarvestHistory> specification, final Sort sort) {
        final Element result = GeonetRepositoryImpl.findAllAsXml(_entityManager, HarvestHistory.class, specification, sort);
        for (int i = 0; i < result.getContentSize(); i++) {
            Element record = (Element) result.getContent(i);

            Element info = record.getChild("info");
            Element xml = null;
            try {
                xml = Xml.loadString(info.getValue(), false);
            } catch (Exception e) {
                xml = new Element("error").setText("Invalid XML harvester result: "+e.getMessage());
                e.printStackTrace();
            }
            info.removeContent();
            info.addContent(xml);

            Element params = record.getChild("params");
            try {
                xml = Xml.loadString(params.getValue(), false);
            } catch (Exception e) {
                xml = new Element("error").setText("Invalid XML harvester params: "+e.getMessage());
                e.printStackTrace();
            }
            params.removeContent();
            params.addContent(xml);
        }
        return result;

    }
}
