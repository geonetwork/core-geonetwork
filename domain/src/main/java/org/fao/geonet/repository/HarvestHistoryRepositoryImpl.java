package org.fao.geonet.repository;

import org.fao.geonet.domain.Constants;
import org.fao.geonet.domain.HarvestHistory;
import org.fao.geonet.domain.HarvestHistory_;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.Collection;

/**
 * Implementation for custom methods for the HarvestHistoryRepository class.
 * <p/>
 * User: Jesse
 * Date: 9/20/13
 * Time: 4:03 PM
 */
public class HarvestHistoryRepositoryImpl implements HarvestHistoryRepositoryCustom {

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
     * The major change is that this method takes the data of the "info" and "params" properties and parses then as XML and
     * replaces the corresponding elements with the parsed XML.
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
                xml = new Element("error").setText("Invalid XML harvester result: " + e.getMessage());
                e.printStackTrace();
            }
            info.removeContent();
            info.addContent(xml);

            Element params = record.getChild("params");
            try {
                xml = Xml.loadString(params.getValue(), false);
            } catch (Exception e) {
                xml = new Element("error").setText("Invalid XML harvester params: " + e.getMessage());
                e.printStackTrace();
            }
            params.removeContent();
            params.addContent(xml);
        }
        return result;

    }

    @Override
    public int deleteAllById(Collection<Integer> ids) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaDelete<HarvestHistory> delete = cb.createCriteriaDelete(HarvestHistory.class);
        final Root<HarvestHistory> root = delete.from(HarvestHistory.class);

        delete.where(root.get(HarvestHistory_.id).in(ids));

        final int deleted = _entityManager.createQuery(delete).executeUpdate();

        _entityManager.flush();
        _entityManager.clear();

        return deleted;
    }

    @Override
    public int markAllAsDeleted(@Nonnull String harvesterUuid) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaUpdate<HarvestHistory> update = cb.createCriteriaUpdate(HarvestHistory.class);
        final Root<HarvestHistory> root = update.from(HarvestHistory.class);

        update.set(root.get(HarvestHistory_.deleted_JpaWorkaround), Constants.YN_TRUE);
        update.where(cb.equal(root.get(HarvestHistory_.harvesterUuid), harvesterUuid));

        int updated = _entityManager.createQuery(update).executeUpdate();
        _entityManager.flush();
        _entityManager.clear();

        return updated;

    }
}
