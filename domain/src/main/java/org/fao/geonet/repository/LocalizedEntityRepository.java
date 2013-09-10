package org.fao.geonet.repository;

import org.fao.geonet.domain.Localized;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Common repository interface for all Repositories that load entities that extend the Localised abstract class.
 *
 * User: Jesse
 * Date: 9/9/13
 * Time: 2:58 PM
 */
public interface LocalizedEntityRepository<T extends Localized, ID extends Serializable> {

    /**
     * Load all entities and convert each to XML of the form:
     *     <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml();

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     *     <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @param specification A specification of the criteria that must be satisfied for entity to be selected.
     * @return  all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(Specification<T> specification);

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     *     <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @param specification A specification of the criteria that must be satisfied for entity to be selected.
     * @return  all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(Specification<T> specification, Sort sort);
}
